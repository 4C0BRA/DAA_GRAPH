package graph;
public final class Metrics {
    public long edges, comparisons, finds, unions, heapPushes, heapPops;
    public long total(){return edges+comparisons+finds+unions+heapPushes+heapPops;}
}
