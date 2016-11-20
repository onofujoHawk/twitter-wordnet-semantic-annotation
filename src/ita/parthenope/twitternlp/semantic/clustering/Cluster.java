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
public class Cluster {
	private int Index;
	private String name;
	private Vector<ClusterElement> items;
	private ArrayList<String> nomi;

	
	public Cluster(int i,String nome){
		Index=i;
		name=nome;
		items= new Vector<ClusterElement>();
		nomi= new ArrayList<String>();
	}
	
	public void AddItem(int root,String nome,String colore){
		ClusterElement c=new ClusterElement(root,nome,colore);
		items.add(c);
	}
	
	/**
	 * Vengono stampati tutti gli elementi presenti per ogni cluster.
	 * @param nome è il nome del cluster
	 */
	public void StampaElementiCluster()
	{
		System.out.println("Elementi Cluster #"+Index+" con "+items.size()+" elementi");
		for(int i=0;i<items.size();i++)
		{
			System.out.println("Nome Elemento: "+items.get(i).GetName());
		}
		System.out.println("");
	}
	
	
	public Vector<ClusterElement> GetClusterElements(){
		return items;
	}
	
	public Vector<ClusterElement> GetBlackClusterElements(){
		Vector<ClusterElement> app = new Vector<ClusterElement>();
		for(int i=0;i<items.size();i++){
			if(items.get(i).GetColor().equals("black"))
				app.add(items.get(i));
		}
		return app;
	}
	
	/*public Vector<ClusterElement> GetRedClusterElements(){
		Vector<ClusterElement> app = new Vector<ClusterElement>();
		for(int i=0;i<items.size();i++){
			if(items.get(i).GetColor().equals("red"))
				app.add(items.get(i));
		}
		return app;
	}*/
	
	public void SetNome(String nome){
		name=nome;
	}

	public String GetNome(){
		return name;
	}

	/**
	 * @return the nomi
	 */
	public ArrayList<String> getNomi() {
		return nomi;
	}

	/**
	 * @param nomi the nomi to set
	 */
	public void setNomi(ArrayList<String> nomi) {
		this.nomi = nomi;
	}
}
