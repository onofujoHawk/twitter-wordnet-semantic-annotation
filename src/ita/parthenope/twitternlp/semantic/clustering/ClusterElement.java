/*
 * Michele Crivellari: Classificazione di HASHTAG mediante elaborazione semantica.
 */
package ita.parthenope.twitternlp.semantic.clustering;


/**
 * 
 * @author crivellari_michele
 *
 */
public class ClusterElement {
	private int ParentCluster;
	private String name;
	private String Color;

	
	public ClusterElement() {
		super();
	}
	
	public ClusterElement(int root,String nome,String colore){
		ParentCluster=root;
		name=nome;
		Color=colore;
		
	}
	
	public void SetClusterElement(int root,String nome,String colore){
		ParentCluster=root;
		name=nome;
		Color=colore;
		
	}
	
	public String GetName(){
		return name;
	}
	
	public String GetColor(){
		return Color;
	}
	
	public int GetParent(){
		return ParentCluster;
	}

	public void SetName(String nome){
		name=nome;
	}
	
	public void Setcolor(String color){
		Color=color;
	}
	
	public void SetParent(int root){
		ParentCluster=root;
	}
	
	public void StampaElemento(){
	  System.out.println("Nome= "+name+" Colore= "+Color+" Cluster= "+ParentCluster);	
	}
	
	
}
