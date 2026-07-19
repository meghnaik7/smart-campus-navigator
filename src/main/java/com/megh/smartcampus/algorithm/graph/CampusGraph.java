package com.megh.smartcampus.algorithm.graph;

import java.util.*;

/**
 * CampusGraph — weighted directed adjacency-list graph.
 *
 * Algorithms implemented from scratch (no external library):
 *   • Dijkstra  — shortest path by total distance (metres)
 *   • BFS       — shortest path by hop count
 *   • findNearest — run Dijkstra to each candidate, keep minimum
 */
public class CampusGraph {

    // adjacencyList.get(nodeId) = all outgoing edges from that node
    private final Map<Long, List<GraphEdgeModel>> adjacencyList = new HashMap<>();
    private final Map<Long, GraphNodeModel>        nodes         = new HashMap<>();

    // ── Graph mutation ──────────────────────────────────────────────────

    public void addNode(GraphNodeModel node) {
        nodes.put(node.getId(), node);
        adjacencyList.putIfAbsent(node.getId(), new ArrayList<>());
    }

    /**
     * Adds one directed edge src→tgt.
     * If bidirectional, also adds the reverse edge tgt→src.
     */
    public void addEdge(GraphEdgeModel edge) {
        adjacencyList
            .computeIfAbsent(edge.getSource(), k -> new ArrayList<>())
            .add(edge);

        if (edge.isBidirectional()) {
            // reverse edge — bidirectional=false so we don't recurse
            adjacencyList
                .computeIfAbsent(edge.getTarget(), k -> new ArrayList<>())
                .add(new GraphEdgeModel(edge.getTarget(), edge.getSource(), edge.getWeight(), false));
        }
    }

    public void clear() {
        adjacencyList.clear();
        nodes.clear();
    }

    public int getNodeCount() { return nodes.size(); }

    public int getEdgeCount() {
        return adjacencyList.values().stream().mapToInt(List::size).sum();
    }

    public GraphNodeModel getNode(long id) { return nodes.get(id); }

    // ── Dijkstra ────────────────────────────────────────────────────────

    /**
     * Dijkstra's algorithm — finds the shortest (minimum-distance) path.
     *
     * Why a record for the PQ entry?
     *   The old code used long[] and cast the double weight to long,
     *   which truncated decimal distances and broke the priority ordering
     *   (e.g. 100.7 and 100.3 both become 100, wrong winner picked).
     *   A record preserves full double precision.
     *
     * Time complexity: O((V + E) log V)
     */
    public RouteResult dijkstra(long src, long dst) {
        if (!nodes.containsKey(src)) return new RouteResult("Source node not found: " + src);
        if (!nodes.containsKey(dst)) return new RouteResult("Destination node not found: " + dst);
        if (src == dst)              return new RouteResult(List.of(nodes.get(src)), 0.0);

        // dist[id] = shortest known distance from src to id
        Map<Long, Double> dist = new HashMap<>();
        nodes.keySet().forEach(id -> dist.put(id, Double.MAX_VALUE));
        dist.put(src, 0.0);

        // prev[id] = which node we came from on the shortest path to id
        Map<Long, Long> prev = new HashMap<>();
        prev.put(src, null);

        // Min-heap ordered by distance — uses double, not long
        PriorityQueue<QueueEntry> pq =
            new PriorityQueue<>(Comparator.comparingDouble(QueueEntry::distance));
        pq.offer(new QueueEntry(src, 0.0));

        Set<Long> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            QueueEntry current = pq.poll();
            long currentId = current.nodeId();

            if (visited.contains(currentId)) continue; // stale entry
            visited.add(currentId);

            if (currentId == dst) break; // early exit — optimal path found

            for (GraphEdgeModel edge : adjacencyList.getOrDefault(currentId, List.of())) {
                long neighbourId = edge.getTarget();
                if (visited.contains(neighbourId)) continue;

                // Relaxation step
                double newDist = dist.get(currentId) + edge.getWeight();
                if (newDist < dist.getOrDefault(neighbourId, Double.MAX_VALUE)) {
                    dist.put(neighbourId, newDist);
                    prev.put(neighbourId, currentId);
                    pq.offer(new QueueEntry(neighbourId, newDist));
                }
            }
        }

        if (dist.get(dst) == Double.MAX_VALUE) {
            return new RouteResult("No path found between nodes " + src + " and " + dst);
        }
        return buildPath(prev, src, dst, dist.get(dst));
    }

    /**
     * PQ entry — record keeps nodeId and its current known distance.
     * Using a record avoids the double→long cast bug in the old long[] approach.
     */
    private record QueueEntry(long nodeId, double distance) {}

    // ── BFS ─────────────────────────────────────────────────────────────

    /**
     * BFS — finds path with fewest hops (ignores edge weights).
     * Useful for checking connectivity or when all edges are equal.
     * Time complexity: O(V + E)
     */
    public RouteResult bfs(long src, long dst) {
        if (!nodes.containsKey(src) || !nodes.containsKey(dst))
            return new RouteResult("Source or destination node not found");
        if (src == dst) return new RouteResult(List.of(nodes.get(src)), 0.0);

        Queue<Long>        queue   = new LinkedList<>();
        Set<Long>          visited = new HashSet<>();
        Map<Long, Long>    prev    = new HashMap<>();

        queue.add(src);
        visited.add(src);
        prev.put(src, null);

        while (!queue.isEmpty()) {
            long current = queue.poll();
            if (current == dst) return buildPath(prev, src, dst, -1.0);

            for (GraphEdgeModel edge : adjacencyList.getOrDefault(current, List.of())) {
                long nb = edge.getTarget();
                if (!visited.contains(nb)) {
                    visited.add(nb);
                    prev.put(nb, current);
                    queue.add(nb);
                }
            }
        }
        return new RouteResult("No path found between nodes " + src + " and " + dst);
    }

    // ── Find nearest ─────────────────────────────────────────────────────

    /**
     * Runs Dijkstra from src to every candidate and returns the closest reachable one.
     */
    public RouteResult findNearest(long src, List<Long> candidates) {
        if (candidates == null || candidates.isEmpty())
            return new RouteResult("No candidate nodes provided");

        RouteResult best      = null;
        double      bestDist  = Double.MAX_VALUE;

        for (long candidateId : candidates) {
            RouteResult result = dijkstra(src, candidateId);
            if (result.isFound() && result.getTotalMeters() < bestDist) {
                bestDist = result.getTotalMeters();
                best     = result;
            }
        }
        return best != null ? best : new RouteResult("No reachable facility found");
    }

    // ── Path reconstruction ──────────────────────────────────────────────

    /**
     * Traces back through the prev map from dst → src, then reverses the list.
     * knownDist < 0 means we should estimate from coordinates (BFS case).
     */
    private RouteResult buildPath(Map<Long, Long> prev, long src, long dst, double knownDist) {
        List<GraphNodeModel> path = new ArrayList<>();
        Long current = dst;

        while (current != null) {
            GraphNodeModel node = nodes.get(current);
            if (node == null) return new RouteResult("Path reconstruction failed — node missing");
            path.add(node);
            current = prev.get(current);
        }
        Collections.reverse(path);

        if (path.isEmpty() || path.get(0).getId() != src)
            return new RouteResult("Path reconstruction failed — start node mismatch");

        double distance = (knownDist < 0) ? estimateDistanceFromCoordinates(path) : knownDist;
        return new RouteResult(path, distance);
    }

    /**
     * Fallback distance estimation using Euclidean distance between node coordinates.
     * Only used by BFS (which does not track actual edge weights).
     */
    private double estimateDistanceFromCoordinates(List<GraphNodeModel> path) {
        double total = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            double dx = path.get(i + 1).getX() - path.get(i).getX();
            double dy = path.get(i + 1).getY() - path.get(i).getY();
            total += Math.sqrt(dx * dx + dy * dy);
        }
        return total;
    }
}
