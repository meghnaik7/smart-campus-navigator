package com.megh.smartcampus.algorithm.graph;

import java.util.List;

public class RouteResult {
    private final List<GraphNodeModel> path;
    private final double totalMeters;
    private final int estimatedMinutes;
    private final boolean found;
    private final String message;

    public RouteResult(List<GraphNodeModel> path, double totalMeters) {
        this.path = path;
        this.totalMeters = totalMeters;
        this.estimatedMinutes = (int) Math.ceil(totalMeters / 80.0);
        this.found = !path.isEmpty();
        this.message = found ? "Route found" : "No path found";
    }

    public RouteResult(String error) {
        this.path = List.of(); this.totalMeters = 0;
        this.estimatedMinutes = 0; this.found = false; this.message = error;
    }

    public List<GraphNodeModel> getPath()    { return path; }
    public double getTotalMeters()           { return totalMeters; }
    public int getEstimatedMinutes()         { return estimatedMinutes; }
    public boolean isFound()                 { return found; }
    public String getMessage()               { return message; }
}
