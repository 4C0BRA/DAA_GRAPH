package graph;
import java.util.*;
public final class Graph {
    public record Edge(int from, int to, long weight) {}
    public final int n;
    public final boolean directed;
    public final List<Edge> edges = new ArrayList<>();
    public final List<List<Edge>> adj = new ArrayList<>();
    public Graph(int n, boolean directed) {
        if(n < 1) throw new IllegalArgumentException("At least one vertex required");
        this.n=n; this.directed=directed;
        for(int i=0;i<n;i++) adj.add(new ArrayList<>());
    }
    public Graph add(int u,int v,long w) {
        if(u<0||v<0||u>=n||v>=n) throw new IllegalArgumentException("Invalid vertex");
        Edge e=new Edge(u,v,w); edges.add(e); adj.get(u).add(e);
        if(!directed) adj.get(v).add(new Edge(v,u,w));
        return this;
    }
    public void requireDirected(boolean value) {
        if(directed!=value) throw new IllegalArgumentException("Wrong graph type");
    }
}
