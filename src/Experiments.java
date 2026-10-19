import graph.*;
import mst.MST;
import connectivity.Connectivity;
import shortestpath.Dijkstra;
import java.util.*;
import java.nio.file.*;
import java.io.*;
public class Experiments {
    public static final long SEED=20261019L;
    private static volatile long sink;
    public static Graph generate(int n,int count,String type){
        boolean directed=!type.equals("undirected");Graph g=new Graph(n,directed);
        Random random=new Random(SEED+31L*n+type.hashCode());Set<Long> used=new HashSet<>();
        // A chain guarantees connectivity for MST and reachability from zero.
        for(int i=1;i<n;i++)add(g,used,i-1,i,1+random.nextInt(100));
        if(type.equals("cyclic"))add(g,used,n-1,0,1+random.nextInt(100));
        while(g.edges.size()<count){
            int u=random.nextInt(n),v=random.nextInt(n);if(u==v)continue;
            if((type.equals("dag")||!directed)&&u>v){int t=u;u=v;v=t;}
            add(g,used,u,v,1+random.nextInt(100));
        }
        return g;
    }
    private static void add(Graph g,Set<Long> used,int u,int v,long w){
        long key=((long)u<<32)|v;if(used.add(key))g.add(u,v,w);
    }
    static long run(String name,Graph g,Metrics m){
        return switch(name){
            case "Kruskal" -> MST.kruskal(g,m).weight();
            case "Prim" -> MST.prim(g,m).weight();
            case "SCC" -> Connectivity.scc(g,m).size();
            case "Topological-DAG","Topological-cycle" -> Connectivity.topological(g,m).order().size();
            case "Dijkstra" -> Arrays.hashCode(Dijkstra.run(g,0,m).distances());
            default -> throw new IllegalArgumentException(name);
        };
    }
    public static void main(String[] args)throws Exception{
        Files.createDirectories(Path.of("results/tables"));
        try(var raw=new PrintWriter("results/tables/raw.csv");var summary=new PrintWriter("results/tables/summary.csv")){
            raw.println("algorithm,V,E,trial,nanoseconds,edge_examinations,comparisons,finds,unions,heap_pushes,heap_pops,total_operations");
            summary.println("algorithm,V,E,average_ns,min_ns,max_ns,edge_examinations,comparisons,finds,unions,heap_pushes,heap_pops,total_operations");
            int[][] sizes={{10,30},{100,500},{1000,5000}};
            for(int[] size:sizes){
                int n=size[0],e=size[1];Graph und=generate(n,e,"undirected"),dag=generate(n,e,"dag"),cyc=generate(n,e,"cyclic");
                if(MST.kruskal(und,new Metrics()).weight()!=MST.prim(und,new Metrics()).weight())throw new AssertionError("MST mismatch");
                for(String name:List.of("Kruskal","Prim","SCC","Topological-DAG","Topological-cycle","Dijkstra")){
                    Graph g=switch(name){case "Kruskal","Prim"->und;case "Topological-DAG"->dag;default->cyc;};
                    for(int warm=0;warm<100;warm++)sink=run(name,g,new Metrics());
                    long sum=0,min=Long.MAX_VALUE,max=0;Metrics last=null;
                    for(int trial=1;trial<=5;trial++){
                        Metrics m=new Metrics();long start=System.nanoTime();long answer=run(name,g,m);long elapsed=System.nanoTime()-start;sink=answer;
                        sum+=elapsed;min=Math.min(min,elapsed);max=Math.max(max,elapsed);last=m;
                        raw.printf(Locale.ROOT,"%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%n",name,n,e,trial,elapsed,m.edges,m.comparisons,m.finds,m.unions,m.heapPushes,m.heapPops,m.total());
                    }
                    summary.printf(Locale.ROOT,"%s,%d,%d,%.1f,%d,%d,%d,%d,%d,%d,%d,%d,%d%n",name,n,e,sum/5.0,min,max,last.edges,last.comparisons,last.finds,last.unions,last.heapPushes,last.heapPops,last.total());
                }
            }
        }
        Files.writeString(Path.of("results/tables/environment.txt"),"Java: "+System.getProperty("java.version")+"\nVM: "+System.getProperty("java.vm.name")+"\nOS: "+System.getProperty("os.name")+" "+System.getProperty("os.arch")+"\nSeed: "+SEED+"\nWarmups: 100; measured trials: 5\n");
        System.out.println("Saved raw trials, summary and environment to results/tables.");
    }
}
