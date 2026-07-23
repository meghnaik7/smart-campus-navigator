package com.megh.smartcampus.algorithm.graph;

import com.megh.smartcampus.repository.GraphEdgeRepository;
import com.megh.smartcampus.repository.GraphNodeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * GraphService - loads DB nodes/edges into the in-memory CampusGraph.
 * Uses ContextRefreshedEvent (not @PostConstruct) so @Transactional works.
 */
@Service
@RequiredArgsConstructor
public class GraphService {

    private static final Logger log = LoggerFactory.getLogger(GraphService.class);
    private final GraphNodeRepository nodeRepo;
    private final GraphEdgeRepository edgeRepo;
    private final CampusGraph graph = new CampusGraph();

    @EventListener(ContextRefreshedEvent.class)
    @Transactional(readOnly = true)
    public void onContextRefreshed() {
        build();
    }

    @Transactional(readOnly = true)
    public void reload() { build(); }

    private void build() {
        graph.clear();
        nodeRepo.findAllActive().forEach(n ->
            graph.addNode(new GraphNodeModel(n.getId(), n.getName(),
                n.getCoordinateX(), n.getCoordinateY(),
                n.getFloor() != null ? n.getFloor() : 0)));
        edgeRepo.findAllActiveWithNodes().forEach(e ->
            graph.addEdge(new GraphEdgeModel(
                e.getSourceNode().getId(), e.getTargetNode().getId(),
                e.getDistanceMeters(), e.isBidirectional())));
        log.info("Graph built: {} nodes, {} edges", graph.getNodeCount(), graph.getEdgeCount());
    }

    public RouteResult dijkstra(long src, long dst)   { return graph.dijkstra(src, dst); }
    public RouteResult nearest(long src, List<Long> c) { return graph.findNearest(src, c); }

    public String stats() {
        return "Nodes: " + graph.getNodeCount() + ", Edges: " + graph.getEdgeCount();
    }
}
