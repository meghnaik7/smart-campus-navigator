package com.megh.smartcampus.algorithm.graph;

public class GraphEdgeModel {
    private final long source;
    private final long target;
    private final double weight;
    private final boolean bidirectional;

    public GraphEdgeModel(long source, long target, double weight, boolean bidirectional) {
        this.source = source; this.target = target;
        this.weight = weight; this.bidirectional = bidirectional;
    }

    public long getSource()         { return source; }
    public long getTarget()         { return target; }
    public double getWeight()       { return weight; }
    public boolean isBidirectional(){ return bidirectional; }
}
