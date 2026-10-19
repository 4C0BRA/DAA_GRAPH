package connectivity;
import java.util.*;
import graph.*;
public final class Connectivity {
    public record Topology(boolean dag,List<Integer> order){}
    public static Topology topological(Graph g,Metrics m){
        g.requireDirected(true);int[] degree=new int[g.n];
        for(var e:g.edges){m.edges++;degree[e.to()]++;}
        var q=new ArrayDeque<Integer>();for(int i=0;i<g.n;i++)if(degree[i]==0)q.add(i);
        var order=new ArrayList<Integer>();
        while(!q.isEmpty()){int u=q.remove();order.add(u);
            for(var e:g.adj.get(u)){m.edges++;if(--degree[e.to()]==0)q.add(e.to());}}
        boolean dag=order.size()==g.n;
        return new Topology(dag,dag?List.copyOf(order):List.of());
    }
    // Iterative Kosaraju: explicit DFS frames avoid recursion depth limits.
    public static List<List<Integer>> scc(Graph g,Metrics m){
        g.requireDirected(true);boolean[] seen=new boolean[g.n];int[] next=new int[g.n];
        var finish=new ArrayList<Integer>();var stack=new ArrayDeque<Integer>();
        for(int root=0;root<g.n;root++)if(!seen[root]){
            seen[root]=true;stack.push(root);
            while(!stack.isEmpty()){
                int u=stack.peek();
                if(next[u]<g.adj.get(u).size()){
                    int v=g.adj.get(u).get(next[u]++).to();m.edges++;
                    if(!seen[v]){seen[v]=true;stack.push(v);}
                }else{finish.add(stack.pop());}
            }
        }
        var reverse=new ArrayList<List<Integer>>();for(int i=0;i<g.n;i++)reverse.add(new ArrayList<>());
        for(var e:g.edges){m.edges++;reverse.get(e.to()).add(e.from());}
        Arrays.fill(seen,false);var components=new ArrayList<List<Integer>>();
        for(int i=finish.size()-1;i>=0;i--){int root=finish.get(i);if(seen[root])continue;
            var component=new ArrayList<Integer>();seen[root]=true;stack.push(root);
            while(!stack.isEmpty()){int u=stack.pop();component.add(u);
                for(int v:reverse.get(u)){m.edges++;if(!seen[v]){seen[v]=true;stack.push(v);}}}
            components.add(List.copyOf(component));
        }
        return List.copyOf(components);
    }
}
