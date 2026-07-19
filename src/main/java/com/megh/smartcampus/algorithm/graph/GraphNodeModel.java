package com.megh.smartcampus.algorithm.graph;

public class GraphNodeModel {
    private final long id;
    private final String name;
    private final double x;
    private final double y;
    private final int floor;

    public GraphNodeModel(long id, String name, double x, double y, int floor) {
        this.id = id; this.name = name; this.x = x; this.y = y; this.floor = floor;
    }

    public long getId()     { return id; }
    public String getName() { return name; }
    public double getX()    { return x; }
    public double getY()    { return y; }
    public int getFloor()   { return floor; }
}
