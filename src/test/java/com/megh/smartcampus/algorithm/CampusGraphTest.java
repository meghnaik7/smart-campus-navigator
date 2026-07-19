package com.megh.smartcampus.algorithm;

import com.megh.smartcampus.algorithm.graph.CampusGraph;
import com.megh.smartcampus.algorithm.graph.GraphEdgeModel;
import com.megh.smartcampus.algorithm.graph.GraphNodeModel;
import com.megh.smartcampus.algorithm.graph.RouteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the custom Dijkstra implementation.
 *
 * These are plain Java tests — no Spring context needed.
 * Graph is built fresh before each test so tests are independent.
 *
 * Interview talking point:
 *   "I test the algorithm in isolation, not through the HTTP layer,
 *    so failures are localised and fast."
 */
@DisplayName("CampusGraph — Dijkstra Algorithm Tests")
class CampusGraphTest {

    /*
     * Test graph layout:
     *
     *  1 --[10m]--> 2 --[20m]--> 3
     *  |                         ^
     *  +--------[50m]------------+
     *
     * Shortest path 1->3:  1->2->3  = 30m  (not the direct 50m edge)
     */
    private CampusGraph graph;

    @BeforeEach
    void setUp() {
        graph = new CampusGraph();

        // Nodes: id, name, x, y, floor
        graph.addNode(new GraphNodeModel(1, "Main Gate",   0, 0, 0));
        graph.addNode(new GraphNodeModel(2, "Junction A",  5, 0, 0));
        graph.addNode(new GraphNodeModel(3, "Library",    10, 0, 0));
        graph.addNode(new GraphNodeModel(4, "Isolated",   20, 0, 0));   // no edges

        // Edges: src, tgt, weight, bidirectional
        graph.addEdge(new GraphEdgeModel(1, 2, 10.0, true));
        graph.addEdge(new GraphEdgeModel(2, 3, 20.0, true));
        graph.addEdge(new GraphEdgeModel(1, 3, 50.0, true));   // longer direct path
    }

    // ── Happy paths ───────────────────────────────────────────────────

    @Test
    @DisplayName("Dijkstra finds the shortest path by weight, not by hop count")
    void dijkstra_findsShortestRoute() {
        RouteResult result = graph.dijkstra(1, 3);

        assertThat(result.isFound()).isTrue();
        // Optimal route goes via node 2 (10+20=30) not the direct edge (50)
        assertThat(result.getTotalMeters()).isEqualTo(30.0);
        assertThat(result.getPath()).extracting(GraphNodeModel::getId)
            .containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("Walking time is correctly estimated at 80 m/min")
    void dijkstra_estimatesWalkingTime() {
        RouteResult result = graph.dijkstra(1, 3);
        // 30m / 80 m/min = 0.375 min → ceil = 1 minute
        assertThat(result.getEstimatedMinutes()).isEqualTo(1);
    }

    @Test
    @DisplayName("Source == destination returns a single-node path with zero distance")
    void dijkstra_sameSourceAndDestination() {
        RouteResult result = graph.dijkstra(1, 1);

        assertThat(result.isFound()).isTrue();
        assertThat(result.getTotalMeters()).isEqualTo(0.0);
        assertThat(result.getPath()).hasSize(1);
        assertThat(result.getPath().get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Bidirectional edge allows travel in both directions")
    void dijkstra_bidirectionalEdgeWorksInReverse() {
        // Node 3 -> Node 1 should be reachable because edges are bidirectional
        RouteResult result = graph.dijkstra(3, 1);

        assertThat(result.isFound()).isTrue();
        assertThat(result.getTotalMeters()).isEqualTo(30.0);
    }

    // ── No-path cases ─────────────────────────────────────────────────

    @Test
    @DisplayName("Returns not-found when destination is unreachable (isolated node)")
    void dijkstra_noPathToIsolatedNode() {
        RouteResult result = graph.dijkstra(1, 4);

        assertThat(result.isFound()).isFalse();
        assertThat(result.getMessage()).contains("No path found");
    }

    @Test
    @DisplayName("Returns not-found when source node does not exist in graph")
    void dijkstra_unknownSourceNode() {
        RouteResult result = graph.dijkstra(999, 3);

        assertThat(result.isFound()).isFalse();
        assertThat(result.getMessage()).containsIgnoringCase("not found");
    }

    @Test
    @DisplayName("Returns not-found when destination node does not exist in graph")
    void dijkstra_unknownDestinationNode() {
        RouteResult result = graph.dijkstra(1, 999);

        assertThat(result.isFound()).isFalse();
        assertThat(result.getMessage()).containsIgnoringCase("not found");
    }

    // ── Precision check ───────────────────────────────────────────────

    @Test
    @DisplayName("Dijkstra preserves double precision — does not truncate decimal weights")
    void dijkstra_preservesDoublePrecision() {
        // Add two nodes with fractional-metre distances
        graph.addNode(new GraphNodeModel(10, "A", 0, 5, 0));
        graph.addNode(new GraphNodeModel(11, "B", 5, 5, 0));
        graph.addNode(new GraphNodeModel(12, "C", 10, 5, 0));
        // 10.7 + 10.7 = 21.4 — would lose precision if cast to long
        graph.addEdge(new GraphEdgeModel(10, 11, 10.7, false));
        graph.addEdge(new GraphEdgeModel(11, 12, 10.7, false));

        RouteResult result = graph.dijkstra(10, 12);

        assertThat(result.isFound()).isTrue();
        // Verify no truncation: should be 21.4, not 21.0 or 20.0
        assertThat(result.getTotalMeters()).isEqualTo(21.4);
    }

    // ── findNearest ───────────────────────────────────────────────────

    @Test
    @DisplayName("findNearest returns the closest reachable candidate")
    void findNearest_returnsClosestCandidate() {
        // From node 1, candidates are nodes 2 (10m away) and 3 (30m away)
        RouteResult result = graph.findNearest(1, List.of(2L, 3L));

        assertThat(result.isFound()).isTrue();
        // Node 2 is closer (10m vs 30m)
        assertThat(result.getPath().get(result.getPath().size() - 1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findNearest returns not-found when no candidates are reachable")
    void findNearest_noReachableCandidates() {
        RouteResult result = graph.findNearest(1, List.of(4L));  // node 4 is isolated

        assertThat(result.isFound()).isFalse();
    }
}
