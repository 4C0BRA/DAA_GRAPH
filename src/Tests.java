import graph.*;
import dsu.DSU;
import mst.MST;
import connectivity.Connectivity;
import shortestpath.Dijkstra;
import java.util.*;
public class Tests {
    static int checks;
    static void check(boolean value){checks++;if(!value)throw new AssertionError("Check "+checks+" failed");}
    static void topology(Graph g){
        var t=Connectivity.topological(g,new Metrics());check(t.dag());check(t.order().size()==g.n);
        int[] pos=new int[g.n];boolean[] seen=new boolean[g.n];
        for(int i=0;i<g.n;i++){int v=t.order().get(i);check(!seen[v]);seen[v]=true;pos[v]=i;}
        for(var edge:g.edges)check(pos[edge.from()]<pos[edge.to()]);
    }
    static void sccOracle(Graph g){
        boolean[][] reach=new boolean[g.n][g.n];for(int i=0;i<g.n;i++)reach[i][i]=true;
        for(var e:g.edges)reach[e.from()][e.to()]=true;
        for(int k=0;k<g.n;k++)for(int i=0;i<g.n;i++)for(int j=0;j<g.n;j++)reach[i][j]|=reach[i][k]&&reach[k][j];
        int[] id=new int[g.n];Arrays.fill(id,-1);var cs=Connectivity.scc(g,new Metrics());
        for(int k=0;k<cs.size();k++)for(int v:cs.get(k)){check(id[v]==-1);id[v]=k;}
        for(int i=0;i<g.n;i++){check(id[i]>=0);for(int j=0;j<g.n;j++)check((id[i]==id[j])==(reach[i][j]&&reach[j][i]));}
    }
    static void shortestOracle(Graph g){
        for(int source=0;source<g.n;source++){
            long[] expected=new long[g.n];Arrays.fill(expected,Dijkstra.INF);expected[source]=0;
            for(int pass=1;pass<g.n;pass++)for(var list:g.adj)for(var e:list)
                if(expected[e.from()]!=Dijkstra.INF)expected[e.to()]=Math.min(expected[e.to()],expected[e.from()]+e.weight());
            var actual=Dijkstra.run(g,source,new Metrics());check(Arrays.equals(expected,actual.distances()));
            for(int v=0;v<g.n;v++){
                var path=actual.pathTo(v);if(expected[v]==Dijkstra.INF){check(path.isEmpty());continue;}
                check(path.get(0)==source&&path.get(path.size()-1)==v);long cost=0;
                for(int i=1;i<path.size();i++){int u=path.get(i-1),to=path.get(i);long w=Long.MAX_VALUE;
                    for(var e:g.adj.get(u))if(e.to()==to)w=Math.min(w,e.weight());check(w!=Long.MAX_VALUE);cost+=w;}
                check(cost==expected[v]);
            }
        }
    }
    static void mst(Graph g,long weight,int components){
        var k=MST.kruskal(g,new Metrics());var p=MST.prim(g,new Metrics());
        check(k.weight()==weight&&p.weight()==weight);check(k.components()==components&&p.components()==components);
        check(k.edges().size()==g.n-components&&p.edges().size()==g.n-components);
    }
    public static void main(String[] args){
        var d=new DSU(6,new Metrics());check(d.union(0,1));check(d.union(2,3));check(d.union(0,2));check(!d.union(1,3));check(d.find(0)==d.find(3));check(d.find(4)!=d.find(0));
        mst(new Graph(1,false),0,1);
        mst(new Graph(4,false).add(0,1,1).add(1,2,1).add(2,3,1).add(3,0,1).add(0,2,1),3,1);
        mst(new Graph(4,false).add(0,1,2).add(2,3,3),5,2);
        mst(new Graph(3,false).add(0,1,-2).add(1,2,1).add(0,2,5),-1,1);
        Graph singleton=new Graph(1,true);topology(singleton);sccOracle(singleton);shortestOracle(singleton);
        Graph unreachable=new Graph(4,true).add(0,1,2).add(1,2,0);shortestOracle(unreachable);sccOracle(unreachable);topology(unreachable);
        Graph loops=new Graph(3,true).add(0,0,0).add(0,1,2).add(0,1,2).add(1,0,1);sccOracle(loops);shortestOracle(loops);check(!Connectivity.topological(loops,new Metrics()).dag());
        boolean rejected=false;try{Dijkstra.run(new Graph(2,true).add(0,1,-1),0,new Metrics());}catch(IllegalArgumentException e){rejected=true;}check(rejected);
        Random r=new Random(71);
        for(int round=0;round<100;round++){
            Graph g=new Graph(8,true);for(int i=0;i<24;i++)g.add(r.nextInt(8),r.nextInt(8),r.nextInt(10));
            sccOracle(g);shortestOracle(g);
            Graph dag=new Graph(8,true);for(int u=0;u<8;u++)for(int v=u+1;v<8;v++)if(r.nextBoolean())dag.add(u,v,1);topology(dag);
            Graph und=Experiments.generate(10,30,"undirected");var k=MST.kruskal(und,new Metrics());check(k.connected());check(k.weight()==MST.prim(und,new Metrics()).weight());
        }
        // Independent exhaustive MST oracle on tiny random graphs.
        for(int trial=0;trial<25;trial++){
            Graph g=new Graph(5,false);for(int u=0;u<5;u++)for(int v=u+1;v<5;v++)g.add(u,v,r.nextInt(8));
            long best=Long.MAX_VALUE;
            for(int mask=0;mask<(1<<g.edges.size());mask++)if(Integer.bitCount(mask)==4){
                DSU sets=new DSU(5,new Metrics());long weight=0;boolean tree=true;
                for(int i=0;i<g.edges.size();i++)if((mask&(1<<i))!=0){var e=g.edges.get(i);tree &= sets.union(e.from(),e.to());weight+=e.weight();}
                if(tree)best=Math.min(best,weight);
            }
            mst(g,best,1);
        }
        System.out.println("PASS: "+checks+" correctness checks (including independent oracles).");
    }
}
