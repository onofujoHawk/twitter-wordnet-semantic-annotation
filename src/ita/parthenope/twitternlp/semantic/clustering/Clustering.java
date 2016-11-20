/*
 * Michele Crivellari: Classificazione di HASHTAG mediante elaborazione semantica.
 */
package ita.parthenope.twitternlp.semantic.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;



/**
 * 
 * @author crivellari_michele
 *
 */
public class Clustering {
	private int Ncluster;
	private Cluster[] cluster;
	
	
	/*
	 * La classe Clustering Contiene N Cluster.
	 * Ogni Cluster ha più elementi con Nome,Colore 
	 * e indice del Cluster di Appartenenza.
	 * Clustering -> Cluster[] -> ClusterElement[]  
	 * */
	public Clustering(Vector<String> Entity,int[] V,int n)
	{
		Ncluster=n;
		cluster=new Cluster[Ncluster];
		for(int i=0;i<Ncluster;i++) {
			CalcolaElemCluster(i, Entity, V, n);
			
		}
	}
	
	public void StampaTuttiCluster()
	{
	   for(int i=0;i<Ncluster;i++)
		   cluster[i].StampaElementiCluster();
	}
	
	public void CalcolaElemCluster(int i,Vector<String> Entity,int[] V,int n)
	{
		cluster[i]=new Cluster(i, "");
		for(int j=0;j<Entity.size();j++) {
			//Se il J-esimo punto appartiene all'I-esimo Cluster.
			if(V[j]==i) {
			  //Viene aggiunto un nodo al Cluster.
			  cluster[i].AddItem(i, Entity.get(j), "black");
			}
		}

	}
	
	public Vector<ClusterElement> GetClusterElementsByIndex(int i){
		return cluster[i].GetClusterElements();
	}
	
	public Vector<ClusterElement> GetBlackClusterElementsByIndex(int i){
		return cluster[i].GetBlackClusterElements();
	}
	
	/*public Vector<ClusterElement> GetRedClusterElementsByIndex(int i){
		return cluster[i].GetRedClusterElements();
	}*/
	
	public int GetIndexClusterByName(String name){
		for(int i=0;i<Ncluster;i++)
			if(cluster[i].GetNome().equals(name))
				return i;
		return -1;
	}
	
	public void SetClusterNameByIndex(int i,String nome){
		cluster[i].SetNome(nome);
	}
	
	public void SetClusterNamesByIndex(int i, ArrayList<String> nomi){
		cluster[i].setNomi(nomi);
	}
	
	public String GetNomeClusterByIndex(int i){
		return cluster[i].GetNome();
	}
	
	public ArrayList<String> getNomiClusterByIndex(int i){
		return cluster[i].getNomi();
	}
	
	public Vector<String> GetNomiCluster(){
		Vector<String> s=new Vector<>();
		for(int i=0;i<cluster.length;i++)
			s.add(GetNomeClusterByIndex(i));
		return s;
	}

}
