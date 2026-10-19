package dsu;
import graph.Metrics;
public final class DSU {
    private final int[] parent,size;
    private final Metrics metrics;
    public DSU(int n,Metrics metrics){
        parent=new int[n]; size=new int[n]; this.metrics=metrics;
        for(int i=0;i<n;i++){parent[i]=i;size[i]=1;}
    }
    public int find(int x){
        metrics.finds++;
        while(parent[x]!=x){parent[x]=parent[parent[x]];x=parent[x];}
        return x;
    }
    public boolean union(int a,int b){
        metrics.unions++; a=find(a);b=find(b);
        if(a==b)return false;
        if(size[a]<size[b]){int t=a;a=b;b=t;}
        parent[b]=a;size[a]+=size[b];return true;
    }
}
