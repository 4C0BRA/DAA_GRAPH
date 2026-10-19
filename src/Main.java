import graph.*;
import mst.MST;
import connectivity.Connectivity;
import shortestpath.Dijkstra;
public class Main {
    public static void main(String[] args){
        Graph city=new Graph(5,false).add(0,1,4).add(0,2,2).add(1,2,1).add(1,3,5).add(2,3,8).add(3,4,2);
        var k=MST.kruskal(city,new Metrics());var p=MST.prim(city,new Metrics());
        System.out.println("Kruskal: "+k+"\nPrim: "+p);
        var paths=Dijkstra.run(city,0,new Metrics());
        for(int v=0;v<city.n;v++)System.out.println("0 -> "+v+": distance="+paths.distances()[v]+", path="+paths.pathTo(v));
        Graph roads=new Graph(5,true).add(0,1,1).add(1,2,1).add(2,0,1).add(2,3,1).add(3,4,1);
        System.out.println("SCCs: "+Connectivity.scc(roads,new Metrics()));
        System.out.println("Cyclic topology: "+Connectivity.topological(roads,new Metrics()));
        Graph dag=new Graph(4,true).add(0,1,1).add(0,2,1).add(1,3,1).add(2,3,1);
        System.out.println("DAG topology: "+Connectivity.topological(dag,new Metrics()));
    }
}
