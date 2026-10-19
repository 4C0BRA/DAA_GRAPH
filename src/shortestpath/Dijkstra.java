package shortestpath;
import java.util.*;
import graph.*;
public final class Dijkstra {
    public static final long INF=Long.MAX_VALUE;
    private record Entry(int vertex,long distance){}
    public record Result(long[] distances,int[] parent){
        public List<Integer> pathTo(int v){
            if(distances[v]==INF)return List.of();
            var path=new ArrayList<Integer>();for(int u=v;u!=-1;u=parent[u])path.add(u);
            Collections.reverse(path);return path;
        }
    }
    public static Result run(Graph g,int source,Metrics m){
        if(source<0||source>=g.n)throw new IllegalArgumentException("Invalid source");
        for(var e:g.edges){m.edges++;if(e.weight()<0)throw new IllegalArgumentException("Negative weight");}
        long[] distance=new long[g.n];Arrays.fill(distance,INF);int[] parent=new int[g.n];Arrays.fill(parent,-1);
        var q=new PriorityQueue<Entry>((a,b)->{m.comparisons++;return Long.compare(a.distance(),b.distance());});
        distance[source]=0;q.add(new Entry(source,0));m.heapPushes++;
        while(!q.isEmpty()){
            var item=q.remove();m.heapPops++;int u=item.vertex();if(item.distance()!=distance[u])continue;
            for(var e:g.adj.get(u)){m.edges++;
                if(distance[u]>Long.MAX_VALUE-e.weight())throw new ArithmeticException("Distance overflow");
                long candidate=distance[u]+e.weight();
                if(candidate==INF)throw new ArithmeticException("Distance collides with infinity sentinel");
                if(candidate<distance[e.to()]){distance[e.to()]=candidate;parent[e.to()]=u;q.add(new Entry(e.to(),candidate));m.heapPushes++;}
            }
        }
        return new Result(distance,parent);
    }
}
