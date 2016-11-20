/*
 * Michele Crivellari: Classificazione di HASHTAG mediante elaborazione semantica.
 */
package ita.parthenope.twitternlp.semantic.clustering;


/**
 * 
 * @author crivellari_michele
 *
 */
public class RFCMClustering {

	private Double[][] Max_Matrix;
	private Double[][] U,A,Uold;
	private int[] V;
	private int ncluster;
	private int n;
	private int m=2;
	
	
	public RFCMClustering(Double[][] Matrix,int n,int ncluster) {
		super();
		Max_Matrix=Matrix;
		this.n=n;
		this.ncluster=ncluster;
	}
	
	/*
	 * Algoritmo ricavato dall'articolo di ricerca pag 715:
	 * IEEE TRANSACTIONS ON FUZZY SYSTEMS, VOL. 10, NO. 6, DECEMBER 2002 713 Robust Fuzzy Clustering of Relational Data Rajesh N. Dave and Sumit Sen 
	 */
	public void CmeansRelazionale()
	{
	  int iter=0;
	  U=new Double[ncluster][n];
	  Uold=new Double[ncluster][n];
	  A=new Double[ncluster][n];
	  RandomizeMembership();
	  for(int i=0;i<ncluster;i++)
		  for(int k=0;k<n;k++)
			  ComputeA(i,k);
	  
	  do{
		  for(int k=0;k<n;k++){	 
			  for(int i=0;i<ncluster;i++){
				  ComputeA(i,k);
				  CopyU();
				  ComputeU(i,k);
			  }
		  }
		  iter++;
		  if (CheckU()==true)
			  break;
	  }while(iter<200000);
	  System.out.println("Numero di iterazioni: "+iter);
	  CalcolaClustering();
	}
	
	public void ComputeA(int i,int k)
	{
		Double top1=0.0,top2=0.0,down1=0.0,down2=0.0,sum1=0.0,sum2=0.0,sum3=0.0;
		for(int j=0;j<n;j++){
			sum1=sum1+(Math.pow(U[i][j],m)*Max_Matrix[j][k]);
			sum2=sum2+Math.pow(U[i][j],m);
		}
		for(int h=0;h<n;h++){
			for(int j=0;j<n;j++){
				sum3=sum3+( Math.pow(U[i][j],m)*Math.pow(U[i][h],m)*Max_Matrix[j][h] );
			}
		}
		top1=m*sum1;
		top2=m*sum3;
		down1=sum2;
		down2=2*Math.pow(sum2,2);
		A[i][k]=(top1/down1)-(top2/down2);
	}
	
	public void ComputeU(int i,int k)
	{
		int powm=(1/(m-1));
		Double top1=0.0,down1=0.0,sum1=0.0;
		top1=Math.pow((1/A[i][k]),powm);
		for(int w=0;w<ncluster;w++){
			sum1=sum1+Math.pow((1/A[w][k]),powm);
		}
		down1=sum1;
		U[i][k]=top1/down1;
	}
	
	public void CalcolaClustering()
	{
		V=new int[n];
		Double max=0.0;
		for(int i=0;i<n;i++){
			max=0.0;
			for(int j=0;j<ncluster;j++){
				if(max<U[j][i]){
					max=U[j][i];
					V[i]=j;
				}
					
			}
		}
	}
	
	public void CopyU()
	{
		for(int i=0;i<ncluster;i++)
			for(int j=0;j<n;j++)
				Uold[i][j]=U[i][j];
	}
	
	public boolean CheckU()
	{
		Double sum=0.0;
		Double u1 = 0.0,u2 = 0.0;
		Double epsilon=0.001;
		for(int i=0;i<ncluster;i++){
			for(int k=0;k<n;k++){
				u1=Uold[i][k];
				u2=U[i][k];
				sum+=Math.pow(u2-u1, 2);
			}
		}
		if(Double.compare(sum, epsilon)<=0)
		   return true;
		else
			return false;
	}
	
/*	public boolean CheckU(){
		int count=0;
		int tot=(ncluster*n);
		Double u1,u2;
		for(int i=0;i<ncluster;i++){
			for(int k=0;k<n;k++){
				u1=Uold[i][k];
				u2=U[i][k];

			  if(Double.compare(u1, u2)==0){
				 count++;
			  }
			}
		}
		if(count==tot)
		   return true;
		else
			return false;
	}
*/
	public void RandomizeMembership()
	{
		for(int i=0;i<ncluster;i++){
			for(int j=0;j<n;j++){
				U[i][j]=Math.random();
			}
		}
	}
	
	double arrotonda(double d, int p) 
	{
	   return Math.rint(d*Math.pow(10,p))/Math.pow(10,p);
	}
	
	public void StampaMatriceU()
	{
		System.out.println("Stampa Matrice Membership U");
	   for(int i=0;i<ncluster;i++){
		   for(int j=0;j<n;j++){
			   System.out.print(U[i][j]+" ");
		   }
		   System.out.println();
	   }	
	}
	
	public void StampaMatriceUold()
	{
		System.out.println("Stampa Matrice Membership Uold");
	   for(int i=0;i<ncluster;i++){
		   for(int j=0;j<n;j++){
			   System.out.print(Uold[i][j]+" ");
		   }
		   System.out.println();
	   }	
	}
	
	public void StampaVettoreCluster()
	{
		System.out.println("Stampa Vettore Cluster V");
			   for(int j=0;j<n;j++){
				   System.out.print(V[j]+" ");
			   }
			   System.out.println();
	}
	
	public int[] GetV(){
		return V;
	}
	
	public Double[][] GetU(){
		return U;
	}
}