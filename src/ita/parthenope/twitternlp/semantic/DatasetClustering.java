/*
 * Università degli Studi di Napoli Parthenope.
 */
package ita.parthenope.twitternlp.semantic;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import com.ibm.icu.text.SimpleDateFormat;

import customedu.sussex.nlp.jws.ICFinder;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSense;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetID;
import it.uniroma1.lcl.babelnet.data.BabelGloss;
import it.uniroma1.lcl.jlt.util.Language;
import ita.parthenope.twitternlp.babelnet.Disambiguation;
import ita.parthenope.twitternlp.babelnet.wordnet.WordNetDictionary;
import ita.parthenope.twitternlp.semantic.clustering.ClusterElement;
import ita.parthenope.twitternlp.semantic.clustering.Clustering;
import ita.parthenope.twitternlp.semantic.clustering.RFCMClustering;
import ita.parthenope.twitternlp.utils.Utility;
import net.didion.jwnl.JWNLException;




/**
 * Relational Fuzzy C-Means clustering applicato alle Stanford NER Named Entity.
 * @author onofrio
 *
 */
public class DatasetClustering extends SimilarityMeasures
{
	//Matrice di Similarita
	private Double[][] Max_Matrix;
	private int ncluster;
	//Classe dizionario di Wordnet
	private Dictionary dictionary;
	String glossa_chiave="";
	//POS parola Chiave
	String POS_chiave="";

	private int n=0;
	private int[] V;
	private String dizionario;
	private Vector<String> entita_filtrate=new Vector<String>();
	HashMap<String, String> concetti_filtrati=new HashMap<String,String>();
	private Vector<String> entita_da_clusterizzare=new Vector<String>();
	//Parole disambiguate 
	HashMap<String,String> concetti_disambiguati=new HashMap<String,String>();
	private URL url = null;
	//Directory di WordNet
	private final static String dir_diz ="C:/Program Files/WordNet";
	
	RFCMClustering rfcm;
	private Clustering clustering;
	//Part of Speech Tagging
	private Vector<String> PartOfSpeech=new Vector<String>();
	private Double[][] U;
	WordNetDictionary wnet = new WordNetDictionary();
	String chiave = null;
	Integer senso_chiave = -1;

	
	public DatasetClustering(int ncluster) {
		super();
		this.ncluster=ncluster;
		dizionario = (new StringBuilder()).append(dir_diz).append("/dict").toString();
		try {
            url = new URL("file", null, dizionario);
        }
        catch(MalformedURLException malformedurlexception) {
            malformedurlexception.printStackTrace();
        }
		
		//Apertura dizionario di WordNet 2.1 per JWI 
		dictionary = new Dictionary(url);
		if(!dictionary.isOpen())
			dictionary.open();   
		
	}
	
	
	/**
	 * Setting del dataset da cui produrre i clusters. Saranno clusterizzate le
	 * named entity prodotte dal NER.
	 * @param da_clusterizzare
	 *            il dataset da clusterizzare
	 */
	public void setDataset(Vector<String> da_clusterizzare) {
      this.entita_da_clusterizzare=da_clusterizzare;
    }
	
	
	/**
	 * Setting del dataset da cui produrre i clusters. Saranno clusterizzati i
	 * concetti disambiguati da Babelfy.
	 * @param concetti_disambiguati
	 *            le parole disambiguate insieme agli id
	 * @param chiave
	 *            la parola chiave
	 */
	public void setDataset(HashMap<String,String> concetti_disambiguati, final String chiave) {
		this.concetti_disambiguati=concetti_disambiguati;
		this.chiave=chiave;
	}
	
	
	public void beginClustering() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
		Iterator<Entry<String, String>> it = concetti_disambiguati.entrySet().iterator();
		System.out.println("Inizio Riconoscimento parole disambiguate su WordNet: " +sdf.format(System.currentTimeMillis()) );
		entita_filtrate.clear();
		
	    while (it.hasNext()) {
	        Map.Entry<String,String> pair = (Map.Entry<String,String>)it.next();
	        String id = pair.getKey().toString();
	        String termine = pair.getValue().toString();

	        System.out.println("Ricerco "+id+"-"+termine+" su WordNet");
	        String mypos=id.substring(id.length()-1);
	        termine=wnet.lin_stemmer(termine.toLowerCase(), mypos);
	        //Se ho riconosciuto il termine su WordNet, lo aggiungo alla lista dei termini da clusterizzare.
	        if (!termine.equals("")) {
	        	concetti_filtrati.put(termine, mypos);
	        }
	    }
	    System.out.println("Fine Riconoscimento parole su WordNet: "+ sdf.format(System.currentTimeMillis()) );
	    
	    this.n=concetti_filtrati.size();
		stampaConcettiDaClusterizzare();
		System.out.println();
		System.out.println("Numero di dati Riconosciuti: "+n+", Totale: "+concetti_disambiguati.size());
		
		System.out.println();
		creazioneMatriceSimilaritaConcetti();
		
		/* Calcola la Matrice di Distanza => 1 - MatriceSimilarita */
		matriceDistanza();
		
		System.out.println("Fine Costruzione Matrice Similarita: "+ sdf.format(System.currentTimeMillis()) );
		
		
		/* RFCMclustering a una classe che organizza i concetti filtrati in cluster 
		 * usando la matrice di distanza. 
		 * Il numero di Cluster pua essere scelto arbitrariamente  
		 */
        System.out.println("\nINIZIO RFCM CLUSTERING ...");
		rfcm=new RFCMClustering(Max_Matrix, concetti_filtrati.size(), ncluster);
		rfcm.CmeansRelazionale();
		rfcm.StampaVettoreCluster();
		
        setU(rfcm.GetU());
		this.V=new int[n];
		V=rfcm.GetV();
		System.out.println();
		
		/* La classe Clustering inserisce i Concetti nei Cluster calcolati 
		 * assegnando il colore nero ai termini riconosciute da WordNet */
		for (Map.Entry<String, String> entry : concetti_filtrati.entrySet())
		{
			entita_filtrate.add(entry.getKey());
		}
		this.clustering=new Clustering(entita_filtrate, V, ncluster);
		clustering.StampaTuttiCluster();
		System.out.println("FINE RFCM CLUSTERING: "+ sdf.format(System.currentTimeMillis()) +"\n" );
		stampaMatrice();
		System.out.println();
	}
	
	
	/**
	 * Data una parola chiave di ricerca, viene estratta la sua glossa, cioè
	 * una piccola descrizione del significato di quella parola. Se la parola non 
	 * viene riconosciuta in WordNet, la sua main gloss viene presa da quelle di BabelNet.
	 */
	public void babelGlossParolaChiave() 
	{
		Disambiguation bfy = new Disambiguation();
		BabelNet bn = BabelNet.getInstance();
		System.out.println();
		String iSynsetGloss="";
		IIndexWord idxWordChiave=dictionary.getIndexWord( chiave, POS.NOUN );
		
		if ( idxWordChiave != null ) {
			IWordID iChiaveID = idxWordChiave.getWordIDs().get(0);
			ISynset iSynset = dictionary.getWord(iChiaveID).getSynset();
			iSynsetGloss = iSynset.getGloss().toLowerCase();
		}
		String identificativo = bfy.disambiguaParolaChiave(chiave);
		BabelSynset synset = bn.getSynset( new BabelSynsetID(identificativo) );
		List<BabelSense> babelSenses = synset.getSenses(Language.EN);
		String senso="";
		for ( BabelSense sense : babelSenses )
		{
			if ( sense.toString().startsWith("WIKI:EN:") ) {
				senso=sense.toString();
				senso=senso.replace("WIKI:EN:", "").trim();
				senso_chiave = sense.getSenseNumber();
                break;
			} else
				if ( sense.toString().startsWith("WN:EN:") ) {
					senso=sense.toString();
					senso=senso.replace("WN:EN:", "").trim();
					senso_chiave = sense.getSenseNumber();
					break;
				}
		}
		BabelGloss mainGloss = synset.getMainGloss(Language.EN);
		glossa_chiave = mainGloss.getGloss();
		POS_chiave = synset.getPOS().getWordNetPOS().toString();
		if ( iSynsetGloss
				.equalsIgnoreCase(glossa_chiave) ) {
			this.glossa_chiave = iSynsetGloss;
		}
		System.out.println( "Glossa principale della PAROLA CHIAVE "+chiave+":  "+glossa_chiave+"  POS: "+this.POS_chiave.toUpperCase() );
		
	}
	
	
	public void iniziaClustering() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
		System.out.println("Inizio pulizia dei Duplicati: " +sdf.format(System.currentTimeMillis()) );
		
		//Stemming delle entità del NER
		for (int i=0;i<entita_da_clusterizzare.size();i++) {
			GetStems(entita_da_clusterizzare.get(i));
		}
		
		rimozioneDuplicati();
		System.out.println("Fine pulizia dei Duplicati: "+ sdf.format(System.currentTimeMillis()) );
		
		
		this.n=entita_filtrate.size();
		stampaDatiDaClusterizzare();
		System.out.println();
		System.out.println("Numero di dati Puliti: "+n+", Totale: "+entita_da_clusterizzare.size());
		
		System.out.println();
		creazioneMatriceSimilarita();
		
		/* Calcola la Matrice di Distanza => 1 - MatriceSimilarita */
		matriceDistanza();
		
		System.out.println("Fine Costruzione Matrice Similarita: "+ sdf.format(System.currentTimeMillis()) );
		
		
		/* RFCMclustering a una classe che organizza le entità filtrate in cluster 
		 * usando la matrice di distanza. 
		 * Il numero di Cluster pua essere scelto arbitrariamente  
		 */
        System.out.println("\nINIZIO RFCM CLUSTERING...");
		rfcm=new RFCMClustering(Max_Matrix, entita_filtrate.size(), ncluster);
		rfcm.CmeansRelazionale();
		rfcm.StampaVettoreCluster();
		
        setU(rfcm.GetU());
		this.V=new int[n];
		V=rfcm.GetV();
		System.out.println();
		
		/* La classe Clustering inserisce le Entity nei Cluster calcolati 
		 * assegnando il colore nero alle entità riconosciute da WordNet */
		this.clustering=new Clustering(entita_filtrate, V, ncluster);
		clustering.StampaTuttiCluster();
		System.out.println("FINE RFCM CLUSTERING: "+ sdf.format(System.currentTimeMillis()) +"\n" );
		stampaMatrice();
		System.out.println();
	}
	
	
	/**
	 * Sono calcolati i nomi di ogni singolo Cluster mediante Lesk gloss overlap
	 * tra il significato del i-esimo elemento del Cluster e il significato
	 * della parola chiave in esame. Il termine che presenta un valore di
	 * overlap massimo diverrà automaticamente il nome del Cluster.
	 * @param hashmap_centroidi
	 *            HashMap<String> contenente il centroide e tutti gli oggetti ad
	 *            esso associati con i valori di overlap
	 * @throws FileNotFoundException
	 * @throws JWNLException
	 */
	public void calcolaNomiPerGlossOverlaps(HashMap<String, HashMap<String,Double>> hashmap_centroidi) 
			throws FileNotFoundException, JWNLException
	{
		System.out.println( "Determinazione nomi dei Cluster calcolando Gloss Overlap mediante misura di Lesk" );
		LinkedHashSet<String> agglomerazione = new LinkedHashSet<String>();
		babelGlossParolaChiave();
		for ( int idx_cluster=0; idx_cluster<ncluster;idx_cluster++ )
		{
			Vector<ClusterElement> aggregazione=clustering.GetBlackClusterElementsByIndex(idx_cluster);
			System.out.print("ELEMENTI DEL CLUSTER #"+idx_cluster+" = {");
			for ( Iterator<ClusterElement> iterator = aggregazione.iterator(); iterator.hasNext(); ) 
			{
				System.out.print(iterator.next().GetName());
				if ( iterator.hasNext() ) {
					System.out.print(", ");
				}
			}
			System.out.println("}");
		}

		/* 
		 * A questo punto si calcola il nome da assegnare ad ogni Cluster 
		 * attraverso misura di similarità di Lesk
		 */
		calcolaNomiClusterByLesk();
		for ( int i=0;i<ncluster;i++ )
			System.out.println("Il cluster #"+i+" ha nome: "+clustering.GetNomeClusterByIndex(i));
		
		for ( int i=0; i<ncluster; i++ )
		{
			String centroide = clustering.GetNomeClusterByIndex(i);
			if ( StringUtils.isNotBlank(centroide) ) 
			{
				HashMap<String,Double> mappa_agglomerato = new HashMap<String,Double>();
				Vector<ClusterElement> agglomerato_cluster = clustering.GetBlackClusterElementsByIndex(i);
				for ( Iterator<ClusterElement> iterator = agglomerato_cluster.iterator(); iterator.hasNext(); ) {
					agglomerazione.add( iterator.next().GetName() );
				}
				/*
				 * Riapplico Lesk per potermi salvare i valori di overlap
				 * in una HashMap insieme al termine corrispondente
				 */
				for ( String oggetto : agglomerazione )
				{
					String POS = getPOSFromConcetto(oggetto);
					double score = 0.0;
					if ( POS.startsWith("n") && POS_chiave.equalsIgnoreCase("NOUN") )
					{
						score = AdaptedLeskCustom(oggetto, chiave.toLowerCase(), glossa_chiave, "n");
					}
					else if ( POS.startsWith("v") && POS_chiave.equalsIgnoreCase("VERB") )
					{
						score = AdaptedLeskCustom(oggetto, chiave.toLowerCase(), glossa_chiave, "v");
					}
					if ( score != 0.0 )
						mappa_agglomerato.put(oggetto, score);
				}
				agglomerazione.clear();
				hashmap_centroidi.put( centroide, mappa_agglomerato );
			}
		}
		
		
	}
	
	
	protected void calcolaNomiClusterByLesk()
	{
		Vector<ClusterElement> items=new Vector<ClusterElement>();
		for (int i_cluster=0; i_cluster<ncluster; i_cluster++) 
		{
			items=clustering.GetBlackClusterElementsByIndex(i_cluster);
			System.out.println("\rProcesso gli elementi del Cluster #"+i_cluster+"\r");
			
			Double max=0.0;
			String lemma_max="";
			String pos_max="";
			Double overlapscore=0.0;
			
			for (ClusterElement parola : items) 
			{
				String termine = parola.GetName();
				String POS = getPOSFromConcetto(termine);
				if (POS.startsWith("n") && POS_chiave.equalsIgnoreCase("NOUN"))
				{
					overlapscore = AdaptedLeskCustom(termine, chiave.toLowerCase(), glossa_chiave, "n");
				}
				else if (POS.startsWith("v") && POS_chiave.equalsIgnoreCase("VERB"))
				{
					overlapscore = AdaptedLeskCustom(termine, chiave.toLowerCase(), glossa_chiave, "v");
				}
				if (max < overlapscore)
				{
					max = overlapscore;
					lemma_max = termine;
					pos_max = this.POS_chiave;
				}
			}
			if (max != 0.0)
			{
				System.out.println("LESK MAX OVERLAP: "+max+", ASSOCIATO A: "+lemma_max+", SENSO = {"+lesk.signmax+"}, POS: "+pos_max);
		    	IIndexWord idxWord = null;
		    	if (pos_max.equalsIgnoreCase("NOUN")) 
		    		idxWord = dictionary.getIndexWord(lemma_max, POS.NOUN);
		    	else if (pos_max.equalsIgnoreCase("VERB"))
		    		idxWord = dictionary.getIndexWord(lemma_max, POS.VERB);
		    	
		    	IWordID parolaID = idxWord.getWordIDs().get(lesk.signmax-1);
				IWord parola = dictionary.getWord(parolaID);
				ISynset synset = parola.getSynset();
				System.out.println("\nSynset: "+synset+"\nLemma: "+parola.getLemma());

				List<ISynsetID> hyperonyms = synset
						.getRelatedSynsets(Pointer.HYPERNYM);

				if (!hyperonyms.isEmpty())
				{
					//Solo il primo iperonimo diretto
					ISynsetID SID=hyperonyms.get(0);
					max = new Utility().arrotonda(max, 4);
					String iperonimo = dictionary.getSynset(SID).getWord(1).getLemma().toLowerCase();
					System.out.println("TERMINE: "+lemma_max+"  IPERONIMO: "+iperonimo+ "  MAX SIMILARITY: "+max+"  POS: "+SID.getPOS().toString().toUpperCase()+"\n");
					clustering.SetClusterNameByIndex(i_cluster, iperonimo);
				}
				else {
					System.err.println("WARNING: Nessun iperonimo estratto per il Synset: "+parola.getLemma()+".\n");
				}
			}
			//Vado ad azzerare il valore di max
			lesk.azzera_max();
		}
		
		
	}
	
	
	public void calcolaNomiPerAntenatoComune() 
	{
		Vector<ClusterElement>items=new Vector<ClusterElement>();
		ArrayList<String> antenati_in_comune=new ArrayList<String>();
		for (int i_cluster=0; i_cluster<ncluster; i_cluster++) 
		{
			//Prendo gli elementi di ogni singolo Cluster
			items=clustering.GetBlackClusterElementsByIndex(i_cluster);

			//Scorriamo e confrontiamo ogni oggetto del cluster i-esimo
			for ( int idx1=0; idx1<items.size()-1; idx1++ ) 
			{
				for ( int idx2=idx1+1; idx2<items.size(); idx2++ )
				{
					try {
						String concetto1=items.get(idx1).GetName();
						String concetto2=items.get(idx2).GetName();
						
						if ( getPOSFromEntity(concetto1).equals(getPOSFromEntity(concetto2)) ) 
						{
							String POS = getPOSFromEntity(concetto1);
							wnet.lowestCommonAncestor( concetto1, concetto2, POS, antenati_in_comune );
						}
					} 
					catch (FileNotFoundException | JWNLException e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println("Totale antenati in comune: "+antenati_in_comune.size()+"  Totale oggetti presenti nel Cluster #"+i_cluster+": "+items.size());
			System.out.print("Nomi da assegnare al Cluster #"+i_cluster+" = {");
			for ( Iterator<String> iterator = antenati_in_comune.iterator(); iterator.hasNext(); ) 
			{
				System.out.print(iterator.next());
				if ( iterator.hasNext() ) {
					System.out.print(", ");
				}
			}
			System.out.println("}");
			clustering.SetClusterNamesByIndex(i_cluster, antenati_in_comune);
		}
	}
	
	
	/**
	 * Calcola i nomi da assegnare ai cluster mediante Iperonimo diretto. Ad
	 * ogni elemento del cluster i-esimo viene ricavato il suo iperonimo
	 * diretto. Successivamente se il valore di un iperonimo si presenta più
	 * volte esso sarà automaticamente il nome scelto per il cluster, altrimenti
	 * il nome verrà scelto calcolando la massima similarità tra gli elementi
	 * attraverso misura di Lin.
	 * @param nomi_dei_cluster_scelti
	 *            lista dei nomi assegnati ad ogni cluster
	 */
	public void calcolaNomiPerIperonimiDiretti(ArrayList<String> nomi_dei_cluster_scelti) 
	{
		/* A questo punto si calcola il nome da assegnare ad ogni Cluster */
		System.out.println("Determinazione nomi dei Cluster calcolando Iperonimo più FREQUENTE");
		calcolaNomiPerCluster();
		for (int i=0;i<ncluster;i++)
		{
			System.out.println("Il cluster #"+i+" ha nome: "+clustering.GetNomeClusterByIndex(i));
			if (StringUtils.isNotBlank(clustering.GetNomeClusterByIndex(i))) {
				nomi_dei_cluster_scelti.add(clustering.GetNomeClusterByIndex(i));
			}
		}
		
	}
	
	
	/*
	 * La matrice distanza viene creata invertendo i valori della 
	 * matrice di similarita ottenendo quindi nella diagonale valore "0"
	 * */
	public void matriceDistanza() {
		for (int i=0;i<n;i++) {
			for (int j=0;j<n;j++) {
				Max_Matrix[i][j]=(1-Max_Matrix[i][j]);
				Max_Matrix[i][j]=new Utility().arrotonda(Max_Matrix[i][j], 4);
			}
		}
	}
	
	
	public void creazioneMatriceSimilaritaConcetti() 
	{
		 this.Max_Matrix=new Double[n][n];
		 Iterator<Entry<String, String>> it = concetti_filtrati.entrySet().iterator();
		 Vector<String> concetti = new Vector<String>(0);
		 System.out.println("Creazione della Matrice di Similarità per Concetti");
		 while (it.hasNext()) 
		 {
		        Map.Entry<String,String> pair = (Map.Entry<String,String>)it.next();
		        String concetto = pair.getKey().toString();
		        concetti.add(concetto);
		 }
		 for (int i=0;i<n;i++)
		 {
			 for (int j=i+1;j<n;j++)
			 {
				 System.out.println("ANALISI DELLA COPPIA (" +concetti.get(i)+", "+concetti.get(j)+")");
				 if (getPOSFromConcetto(concetti.get(i)).equals(getPOSFromConcetto(concetti.get(j)))==true) 
				 {
					 Max_Matrix[i][j]=Lin(concetti.get(i), concetti.get(j), getPOSFromConcetto(concetti.get(i)));
					 Max_Matrix[i][j]=new Utility().arrotonda(Max_Matrix[i][j], 4);
				 } 
				 else 
				 {
					 Max_Matrix[i][j]=0.0;
				 }
				 Max_Matrix[j][i]= Max_Matrix[i][j];
			 }
			 Max_Matrix[i][i]= 1.0; 
		 }
	}
	
	
	public void creazioneMatriceSimilarita() {
		 this.Max_Matrix=new Double[n][n];
		 System.out.println("Creazione della Matrice di Similarità");
		 for (int i=0;i<n;i++) {
			 for (int j=i+1;j<n;j++) {
				 System.out.println("ANALISI DELLA COPPIA (" +entita_filtrate.get(i)+", "+entita_filtrate.get(j)+")");
				 if (PartOfSpeech.get(i).equals(PartOfSpeech.get(j))==true) {
					 Max_Matrix[i][j]=Lin(entita_filtrate.get(i), entita_filtrate.get(j), PartOfSpeech.get(i));
					 Max_Matrix[i][j]=new Utility().arrotonda(Max_Matrix[i][j], 4);
				 } else {
					 Max_Matrix[i][j]=0.0;
				 }
				
				 Max_Matrix[j][i]= Max_Matrix[i][j];
			}
			 Max_Matrix[i][i]= 1.0; 
		 }
                 
	}
	
	
	/*
	 * Dalle Entity Filtrate si ricava i nomi dei Cluster utilizzando 
	 * gli Iperonimi "hypernyms" di Wordnet.
	 * Dall'albero degli Iperonimi di una Entity si estrae la radice(iperonimo)
	 * e si calcola all'interno di un cluster quale Iperonimo è più frequente.
	 * Nel caso in cui tutti gli Iperonimi hanno stessa frequenza si procede
	 * a valutare qual'è la massima similarità tra gli Ipernomi presenti 
	 * nello stesso cluster assegnando al cluster il nome dell'Iperonimo con massimo
	 * valore di similarità.
	 * Iperonimia: getIpernomiDiretti(String parola,String mypos)
	 * Rate Iperonimia: calcolaNomeByMaxRate(Vector<String> iperonimi,int index)
	 * */
	private void calcolaNomiPerCluster() {
		Vector<ClusterElement>items=new Vector<ClusterElement>();
		Vector<String> iperonimi=new Vector<String>();
		Vector<String> POSiperonimi=new Vector<String>();
		for (int i=0;i<ncluster;i++) {
			//Prendo gli elementi di ogni singolo Cluster
			items=clustering.GetBlackClusterElementsByIndex(i);
			for (int j=0;j<items.size();j++) {
				try {
					//Vector<String> ipers=new Vector<String>(0);
					//ipers=iperonimiDirettiOgniSenso(items.get(j).GetName(), getPOSFromEntity(items.get(j).GetName()), POSiperonimi);
					String ipers=iperonimiDiretti(items.get(j).GetName(),getPOSFromEntity(items.get(j).GetName()),POSiperonimi);
					if (!ipers.isEmpty()) {
						iperonimi.add(ipers);
					}
				} catch (JWNLException e) {
					Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
				}
			}
			calcolaNomeByMaxRate(iperonimi, POSiperonimi, i);
			iperonimi.clear();
			POSiperonimi.clear();
			items.clear();
		}
	}
	
	
	/*
	 * Calcola la frequenza più alta di Iperonimi di un Cluster
	 * Calcola la similarità se la frequenza degli iperonomi è 1
	 * */
	public void calcolaNomeByMaxRate(Vector<String> iperonimi,Vector<String> POSiperonimi,int index) 
	{
		Vector<Integer> Rate=new Vector<Integer>();
		int max=0;
		int imax=0;
		System.out.print("\nIPERONIMI TRA CUI SCEGLIERE IL NOME DEL CLUSTER #"+index+": ");
		for ( int i=0;i<iperonimi.size();i++ ) {
			System.out.print(iperonimi.get(i)+", ");
		}
		System.out.println("");

		for ( int i=0;i<iperonimi.size();i++ ) {
			Rate.add(0);
		}
		for ( int j=0;j<iperonimi.size();j++ ) {
			for ( int k=j;k<iperonimi.size();k++ ) {
				if ( iperonimi.get(j).equals(iperonimi.get(k)) ) 
					Rate.set(j, Rate.get(j)+1);
			}
		}
		
		System.out.println("Possibili nomi per il Cluster");
        for ( int i=0;i<Rate.size();i++ )        
        {
            System.out.println("NOME: "+iperonimi.elementAt(i)+"  FREQUENZA: "+Rate.elementAt(i));
        }
		
		for ( int i=0;i<Rate.size();i++ ) {
			if ( max<=Rate.get(i) ) {
				max=Rate.get(i);
				imax=i;
			}	
		}
		if ( max>1 ) {
			//Trovato un Iperonimo con rank massimo
			System.out.println("NOME CON RANK MASSIMO: "+iperonimi.get(imax)+"  INDICE: "+imax+"  RATE: "+max);
			clustering.SetClusterNameByIndex(index, iperonimi.get(imax));
		}
		else if ( iperonimi.size() == 1 ) {
			//Se ho un unico Iperonimo, esso sarà il nome che verrà dato al Cluster
			System.out.println("UNICO ELEMENTO CLUSTER CON NOME: "+iperonimi.get(imax)+"  RATE: "+max);
			clustering.SetClusterNameByIndex(index, iperonimi.get(imax));
		}
		else {
			//Calcola la Similarità tra Iperonimi se la loro frequenza è pari a 1
			nomeClusterBySimilarity(iperonimi,POSiperonimi,index);
		}
	}
	
	
	public String iperonimiDiretti(String parola,String mypos,Vector<String>POSiperonimi) throws JWNLException
	{
		String Iperonimo="";
		IIndexWord idxWord;
		
	    //Ottengo il Synset per il termine
		if (mypos.equals("n")) {
			idxWord = dictionary.getIndexWord(parola, POS.NOUN);
		}
		else if (mypos.equals("v")) {
			idxWord = dictionary.getIndexWord(parola, POS.VERB);
		} 
		else {
			return Iperonimo;
		}
		if (idxWord != null) 
		{
			//Solo il primo significato estratto
			IWordID wordID = idxWord.getWordIDs().get(0);
			IWord word = dictionary.getWord(wordID);
	        ISynset synset = word.getSynset();
	        
	        //Ottengo gli iperonimi per il Synset
	        List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);
	       
	        //Stampo ogni iperonimo trovato
	        List<IWord> words;
	        
	        for(ISynsetID sid : hypernyms) 
	        {
	            words = dictionary.getSynset(sid).getWords();
	            for(Iterator<IWord> iterator = words.iterator(); iterator.hasNext();) 
	            {
	            	Iperonimo=iterator.next().getLemma().toString();
	            	if(sid.getPOS().toString().equals("noun")) {
	            		POSiperonimi.addElement("n");
	            	}
	            	else if(sid.getPOS().toString().equals("verb")) {
	            		POSiperonimi.addElement("v");
	            	}
	            	//Restituisce il primo iperonimo trovato per quel termine
	            	System.out.println("TERMINE: "+parola+"  POS: "+sid.getPOS()+"  IPERONIMO: "+Iperonimo);
	            	return Iperonimo;
	            }
	    	}
		}
		return Iperonimo;
	}
	
	
	/**
	 * Iperonimo diretto estratto per ogni senso che la @parola può assumere.
	 * @param parola
	 *            è la parola da cui estrarre l'iperonimo
	 * @param mypos
	 *            la POS della parola associata
	 * @return Iperonimo l'iperonimo diretto estratto
	 * @throws JWNLException
	 */
	public Vector<String> iperonimiDirettiOgniSenso(String parola,String mypos,Vector<String>POSiperonimi) throws JWNLException {
		Vector<String> Iperonimi=new Vector<String>(0);
		IIndexWord idxWord;
		
	    //Ottengo il Synset per il termine
		if (mypos.equals("n")) {
			idxWord = dictionary.getIndexWord(parola, POS.NOUN);
		} else if (mypos.equals("v")) {
			idxWord = dictionary.getIndexWord(parola, POS.VERB);
		} else {
			return Iperonimi;
		}
		if (idxWord != null) {
			//Sono considerati tutti i significati del termine
			List<IWordID> wordIDs = idxWord.getWordIDs();
			int Senso=1;
			
			//Per ogni significato
			for (IWordID wordID : wordIDs) {
				IWord word = dictionary.getWord(wordID);
		        ISynset synset = word.getSynset();
		        
		        //Ottengo gli iperonimi per il Synset
		        List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);

		        if (!hypernyms.isEmpty()) {
		        	ISynsetID sid=hypernyms.get(0);
		        	String Iperonimo = dictionary.getSynset(sid).getWord(1).getLemma().toLowerCase();
					System.out.println("Significato #"+Senso+"  TERMINE: "+parola+"  POS: "+sid.getPOS()+"  IPERONIMO: "+Iperonimo);
					if (sid.getPOS().toString().equals("noun")) {
	            		POSiperonimi.addElement("n");
	            	}
	            	else if (sid.getPOS().toString().equals("verb")) {
	            		POSiperonimi.addElement("v");
	            	}
					Senso++;
					Iperonimi.add(Iperonimo);
		        }
			}
			return Iperonimi;
		}
		return Iperonimi;
	}
	
	
	/*
	 * Assegna il nome al cluster in base alla massima similarita tra iperonomi
	 * */
	public void nomeClusterBySimilarity(Vector<String> iperonimi,Vector<String> POSiperonimi,int index) {
		Double max=0.0;
		int imax=0;
		Double similarity=0.0;
		for (int i=0;i<iperonimi.size()-1;i++)
		{
			for (int j=i+1;j<iperonimi.size();j++)
			{
				if (POSiperonimi.get(i).equals(POSiperonimi.get(j)) && !iperonimi.get(i).isEmpty() && !iperonimi.get(j).isEmpty())
				{
					// Similarity misurata attraverso la misura di Lin
					similarity=Lin(iperonimi.get(i), iperonimi.get(j), POSiperonimi.get(i));
					System.out.println("Similarità di Lin per \""+iperonimi.get(i)+"\" POS: "+POSiperonimi.get(i)+" misurato con \""+iperonimi.get(j)+"\" POS: "+POSiperonimi.get(j)+" = "+similarity);
					if (max<similarity) {
						max=similarity;
						imax=i;
					}
				}
			}
		}
		if(max==0) {
			for (int i=0;i<iperonimi.size();i++) {
				if (!iperonimi.get(i).isEmpty()) {
					System.out.println("SCELTO COME ELEMENTO PER IL CLUSTER #"+index+": "+iperonimi.get(imax));
					clustering.SetClusterNameByIndex(index, iperonimi.get(imax));
					break;
				}
			}
		}
		else {
			System.out.println("SCELTO COME ELEMENTO PER IL CLUSTER #"+index+": "+iperonimi.get(imax));
			clustering.SetClusterNameByIndex(index, iperonimi.get(imax));
		}
	}
	
	
	/*
	 * Questo metodo effettua la fase di stemming che e' un processo 
	 * che elimina la parte finale delle parole nel tentativo di arrivare 
	 * alla loro forma base Es. car,cars,car's->car.
	 * Inoltre in questa fase si ricava se l'entita è un nome o un verbo
	 * e se e'riconosciuta da Wordnet oppure no.
	 * */
	public String GetStems(final String entita) 
	{
        String nomecor = "ic-semcor.dat";
        String semcor_dir ="C:/Users/onofr/Desktop/workspace/Twitter-NLP-Thesis/";	   
        System.out.println("STEMMING IN CORSO PER "+entita+".");
        ICFinder ICfinder_cor= new ICFinder (semcor_dir + nomecor);
        this.lin = new customedu.sussex.nlp.jws.Lin (dictionary, ICfinder_cor);
        
		List<String> wordRoots1 = lin.stemmer.findStems(entita, POS.NOUN);
		if (wordRoots1.isEmpty()==true)
		{
			wordRoots1 = lin.stemmer.findStems(entita, POS.VERB);
			if (wordRoots1.isEmpty()==true)
			{
				//Entità non riconosciuta dal dizionario
				System.out.flush();
				System.err.println("NON riconosciuto/a da Wordnet: "+entita);
				return "";
			} 
			else {
				System.out.println("Riconosciuto/a da Wordnet: "+wordRoots1.get(0)+" POS: VERB");
				entita_filtrate.add(wordRoots1.get(0));
				PartOfSpeech.add("v");
			}
		} 
		else {
			System.out.println("Riconosciuto/a da Wordnet: "+wordRoots1.get(0)+" POS: NOUN");
			entita_filtrate.add(wordRoots1.get(0));
			PartOfSpeech.add("n");
		}
		return wordRoots1.get(0);
	}
	
	
	/*
	 * Rimozione di duplicati dalle entità del NER filtrate
	 */
	void rimozioneDuplicati() {
		Collection<String> noDup = new LinkedHashSet<String>(entita_filtrate);
		entita_filtrate.clear();
		this.entita_filtrate.addAll(noDup);
	}
	
	
	private void stampaMatrice() {
		System.out.println("Stampa Matrice di Similarità");
		for(int i=0;i<n;i++) {
			for(int j=0;j<n;j++) {
				System.out.print(Max_Matrix[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	
	/*
	 * Ritorna la POS associata alla Entity
	 */
	private String getPOSFromEntity(String entita) {
		for(int i=0;i<entita_filtrate.size();i++)
			if(entita_filtrate.get(i).equals(entita))
				return this.PartOfSpeech.get(i);
		return "";
	}
	
	
	/*
	 * Ritorna la POS associata al concetto disambiguato
	 */
	private String getPOSFromConcetto(String concetto) {
		return concetti_filtrati.get(concetto);
	}
	
	
	private void stampaDatiDaClusterizzare() {
        System.out.println("\nStampa dei dati Filtrati:");
        for(int i=0;i<n;i++) {
            System.out.println(entita_filtrate.get(i));
        }
	}
	
	
	private void stampaConcettiDaClusterizzare() {
		System.out.println("\nStampa dei concetti Filtrati:");
		Iterator<Entry<String, String>> it = concetti_filtrati.entrySet().iterator();
		while (it.hasNext()) {
	        Map.Entry<String,String> pair = (Map.Entry<String,String>)it.next();
	        String concetto = pair.getKey().toString();
	        String pos = pair.getValue().toString();
            System.out.println(concetto+":"+pos);
        }
	}
	
	
	public Double[][] getU() {
		return U;
	}

	
	public void setU(Double[][] u) {
		U = u;
	}

	
	public int[] getV() {
		return V;
	}

	
	public void setV(int[] v) {
		V = v;
	}


}
