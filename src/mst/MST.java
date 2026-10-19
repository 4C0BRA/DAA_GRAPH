package mst;
import java.util.*;
import graph.*;
import dsu.DSU;
public final class MST {
    public record Result(long weight,List<Graph.Edge> edges,int components){
        public boolean connected(){return components==1;}
    }
    public static Result kruskal(Graph g,Metrics m){
        g.requireDirected(false);
        var sorted=new ArrayList<>(g.edges);
        sorted.sort((a,b)->{m.comparisons++;return Long.compare(a.weight(),b.weight());});
        DSU d=new DSU(g.n,m);var chosen=new ArrayList<Graph.Edge>();long weight=0;int c=g.n;
        for(var e:sorted){m.edges++;if(d.union(e.from(),e.to())){
            chosen.add(e);weight=Math.addExact(weight,e.weight());c--;if(c==1)break;
        }}
        return new Result(weight,List.copyOf(chosen),c);
    }
    public static Result prim(Graph g,Metrics m){
        g.requireDirected(false);
        var q=new PriorityQueue<Graph.Edge>((a,b)->{m.comparisons++;return Long.compare(a.weight(),b.weight());});
        boolean[] seen=new boolean[g.n];var chosen=new ArrayList<Graph.Edge>();long weight=0;int c=0;
        for(int root=0;root<g.n;root++)if(!seen[root]){
            c++;seen[root]=true;
            for(var e:g.adj.get(root)){m.edges++;if(!seen[e.to()]){q.add(e);m.heapPushes++;}}
            while(!q.isEmpty()){
                var e=q.remove();m.heapPops++;if(seen[e.to()])continue;
                seen[e.to()]=true;chosen.add(e);weight=Math.addExact(weight,e.weight());
                for(var next:g.adj.get(e.to())){m.edges++;if(!seen[next.to()]){q.add(next);m.heapPushes++;}}
            }
        }
        return new Result(weight,List.copyOf(chosen),c);
    }
}
