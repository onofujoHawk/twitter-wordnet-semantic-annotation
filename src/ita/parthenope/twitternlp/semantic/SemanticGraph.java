/*
 * Università degli Studi di Napoli Parthenope
 */
package ita.parthenope.twitternlp.semantic;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.xml.sax.SAXException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.ibm.icu.text.SimpleDateFormat;

import edu.smu.tspell.wordnet.WordNetException;
import ita.parthenope.twitternlp.babelnet.Disambiguation;
import ita.parthenope.twitternlp.babelnet.EnglishStemmer;
import ita.parthenope.twitternlp.babelnet.StopWordRemover;
import ita.parthenope.twitternlp.babelnet.wordnet.WordNetDictionary;
import ita.parthenope.twitternlp.ner.StanfordNER;
import ita.parthenope.twitternlp.oracle.TweetService;
import ita.parthenope.twitternlp.textrank.TextRank;
import ita.parthenope.twitternlp.textrank.TextRankCategory;
import ita.parthenope.twitternlp.utils.StyleImporter;
import ita.parthenope.twitternlp.utils.Utility;
import net.didion.jwnl.JWNLException;




/**
 * Costruzione e visualizzazione della Mappa Semantica.
 * @author onofrio
 *
 */
public class SemanticGraph implements ViewerListener {
	
	//TextRank
	private final String lang_code = "en";
    private final String res_path = "res";
    private boolean use_wordnet=true;
    String glossa_chiave="";
    
    //Conversazione di Twitter
    private ArrayList<String> hashner;
    private ArrayList<String> iperonimi_cluster;
    HashMap<String,HashMap<String,Double>> hashmap_centroidi_overlap;
    
	private Vector<String> parole_chiavi;
	private Vector<String> hashtags;
	private List<String> iperonimi_di_wordnet;
	private List<String> parole_significative_textrank;
	private List<String> ner_entita;
	private Vector<String> categorie_di_wikipedia;
	private List<String> categorie_wikipedia_textrank;
	//NER
	private TweetService tw;
	private TextRank tr;
	private TextRankCategory trc;
	private StanfordNER stanford_ner;
	private Disambiguation bfy_disamb;
	private Utility utils;
	private WordNetDictionary wn;
	
	//Graphstream
	protected View view = null;
	private Graph grafo_originale = new SingleGraph("GRAFO ORIGINALE");
	String stylesheet;
	protected boolean loop;
	protected ViewerPipe fromViewer;
	
	
	/**
	 * Costruttore di default della classe SemanticGraph.
	 */
	public SemanticGraph() {
		super();
		loop=true;
		hashtags=new Vector<String>();
		parole_significative_textrank=new ArrayList<String>();
		parole_chiavi=new Vector<String>();
		
		iperonimi_cluster=new ArrayList<String>();
		hashner=new ArrayList<String>();
		ner_entita=new ArrayList<String>();
		iperonimi_di_wordnet=new ArrayList<String>();
		categorie_di_wikipedia=new Vector<String>();
		hashmap_centroidi_overlap=new HashMap<String, HashMap<String,Double>>();
		
		categorie_wikipedia_textrank=new ArrayList<String>();
		setStylesheet();
		try {
			new StopWordRemover("resources/stopwords_en.txt");
		} catch (Exception exc) 
		{
			System.err.println(exc.getMessage());
		}
		
		utils = new Utility();
		tw=new TweetService();
		try {
			tr=new TextRank(res_path, lang_code);
			trc=new TextRankCategory(res_path, lang_code);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	HashMap<Integer,String> hashmap_con_conversazioni;
	Multimap<Integer,String> nodeMultimap = ArrayListMultimap.create();
	
	
	@SuppressWarnings("static-access")
	public void disegnaGrafo() throws IOException, ConfigurationException, InterruptedException, JWNLException 
	{
		hashmap_con_conversazioni = new HashMap<Integer,String>();
		HashMap<String, LinkedHashSet<String>> hmap_ner;
		String conversazione_corrente="";
		
		/*
		 * SETTAGGIO DELLA PAROLA CHIAVE DI RICERCA.
		 */
		parole_chiavi.add("Terrorism");
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
		final long startTime = System.currentTimeMillis();
		System.out.println("Inizio costruzione del Grafo Semantico: " +sdf.format(startTime));
		
		
		try {
			/*
			 *  RICOSTRUZIONE DELLE CONVERSAZIONI DI TWITTER.
			 */
			conversazione_corrente = tw.ricostruisciConversazioneTwitter(parole_chiavi, hashtags, hashmap_con_conversazioni);
			if (StringUtils.isBlank(conversazione_corrente) && hashmap_con_conversazioni.isEmpty()) 
			{
				System.err.println("ERRORE: NESSUN TWEET RACCOLTO PER LA PAROLA CHIAVE "+parole_chiavi.get(0)+"... TERMINO");
				System.exit(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		/*
		 *  ESTRAZIONE DEGLI HASHTAG ASSOCIATI ALLE CONVERSAZIONI.
		 */
		List<String> hash=new ArrayList<String>(hashtags);
		hash = utils.cancellaDuplicati(hash);
		hashtags.removeAllElements();
		for (String elemento : hash) {
			this.hashtags.addElement(elemento);
		}
		System.out.println("Conversazione: "+conversazione_corrente);
		System.out.println("Hashtags Associati: "+hashtags);
		System.out.println();
		
		
		/*
		 *  PAROLE SIGNIFICATIVE DELLA CONVERSAZIONE CON IL TEXTRANK.
		 */
		this.use_wordnet = use_wordnet && ("en".equals(lang_code));
		try {
			tr.prepCall(conversazione_corrente, use_wordnet);
			tr.doTextRanking(conversazione_corrente);
			parole_significative_textrank.addAll(tr.getKeyWord());
			List<String> vector = new ArrayList<String>();
			vector.addAll(parole_significative_textrank);
			parole_significative_textrank.clear();
			for (String parola:vector) {
				if (parola.contains(" ")) {
					String array[] = parola.split(" ");
					for (String word:array)
						parole_significative_textrank.add(word);
				} else {
					parole_significative_textrank.add(parola);
				}
			}
			//Stemming delle parole significative estratte col TextRank
			List<String> parole_stemming = new ArrayList<String>(parole_significative_textrank);
			parole_significative_textrank.clear();
			for (int idx_stem=0; idx_stem < parole_stemming.size(); idx_stem++) 
			{
				String da_fare_stemming = parole_stemming.get(idx_stem);
				String purificata = "";
				EnglishStemmer stemmer = new EnglishStemmer();
				if (da_fare_stemming.endsWith("ies")) 
				{
					purificata = stemmer.stem(da_fare_stemming);
					int index = purificata.lastIndexOf("i");
					purificata = purificata.substring(0, index) + "y" + purificata.substring(index+"i".length());
					parole_significative_textrank.add(purificata);
				}
				else if (da_fare_stemming.endsWith("ing")) 
				{
					purificata = stemmer.stem(da_fare_stemming);
					parole_significative_textrank.add(purificata);
				} 
				else if (da_fare_stemming.endsWith("ed")) 
				{
					purificata = stemmer.stem(da_fare_stemming);
					parole_significative_textrank.add(purificata);
				} 
				else if (da_fare_stemming.endsWith("fully") && da_fare_stemming.length() > 3) 
				{
					purificata = stemmer.stem(da_fare_stemming);
					parole_significative_textrank.add(purificata);
				} 
				else if (da_fare_stemming.endsWith("es")) 
				{
					purificata = stemmer.stem(da_fare_stemming);
					parole_significative_textrank.add(purificata);
				}
				else if (da_fare_stemming.endsWith("ness")) 
				{
					purificata = stemmer.stem(da_fare_stemming);
					parole_significative_textrank.add(purificata);
				}
				else if (da_fare_stemming.endsWith("ful")) 
				{
					purificata = stemmer.stem(da_fare_stemming);
					parole_significative_textrank.add(purificata);
				}
				else if (da_fare_stemming.endsWith("alism")) 
				{
					purificata = stemmer.stem(da_fare_stemming);
					parole_significative_textrank.add(purificata);
				}
				else {
					parole_significative_textrank.add(parole_stemming.get(idx_stem));
				}
			}
			this.parole_significative_textrank = utils.cancellaDuplicati(parole_significative_textrank);
			System.out.println("Parole significative dal Textrank: "+parole_significative_textrank);
	        System.out.println();
	        tr.reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		
		/*
		 *  VIENE EFFETTUATA LA DISAMGIBUAZIONE DELLA CONVERSAZIONE CON BABELFY
		 *  DOPO AVER DISAMBIGUATO IL TESTO, VERRANNO ESTRATTI GLI IPERONIMI
		 *  E LE WIKIPEDIA CATEGORY DELLE NAMED ENTITY, SE QUESTE COINCIDONO CON QUELLE ESTRATTE DAL NER.
		 */
		try {
			bfy_disamb = new Disambiguation();
			bfy_disamb.disambiguazioneConBabelfy(conversazione_corrente);
		} catch (IOException e) 
		{
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
		}
		
		System.out.println();
		
		
		/*
		 *  SUCCESSIVAMENTE ALLA DISAMBIGUAZIONE DELLA CONVERSAZIONE, VENGONO ESTRATTI GLI IPERONIMI
		 *  RICORRENTI E LE CATEGORIE DI WIKIPEDIA, SE UNA ENTITA' RICONOSCIUTA DA BABELFY VIENE
		 *  RICONOSCIUTA ANCHE NEL NER.
		 */
		wn = new WordNetDictionary();
		try 
		{
			this.iperonimi_di_wordnet=wn.iperonimiWordNetJWI(hashner);
			System.out.println();
			
			
			/*
			 *  NAMED ENTITY RECOGNIZER.
			 */
			stanford_ner = new StanfordNER(conversazione_corrente);
			hmap_ner = new LinkedHashMap<String,LinkedHashSet<String>>();
			try {
				hmap_ner=stanford_ner.identifyNER(conversazione_corrente,"C:/Users/onofr/Desktop/workspace/Twitter-NLP-Thesis/classifiers2014/english.conll.4class.distsim.crf.ser.gz",this.hashner);
				for (Map.Entry<String,LinkedHashSet<String>> entry : hmap_ner.entrySet()) 
				{
					LinkedHashSet<String> linkedhashset = new LinkedHashSet<String>(entry.getValue());
					for (String element : linkedhashset) {
						ner_entita.add(element);
					}
				}
			} catch (ClassCastException | ClassNotFoundException | ParserConfigurationException | SAXException | IOException e) 
			{ 
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
			}
			System.out.println("Named Entity riconosciute dal NER: "+ ner_entita);
			//wn.categorieWikipedia(ner_entita, categorie_di_wikipedia);
			
		} catch (WordNetException | IOException e) 
		{
			e.printStackTrace();
		}
		
		System.out.println(); 
		
		
		/*
		 * FILTRAGGIO NAMED ENTITY TRAMITE RFCM CLUSTERING.
		 */
		int ncluster=5;
		DatasetClustering clust=new DatasetClustering(ncluster);
		Vector<String> entita_da_clusterizzare=new Vector<String>(ner_entita);
		clust.setDataset(entita_da_clusterizzare);
		clust.iniziaClustering();
		clust.calcolaNomiPerIperonimiDiretti(iperonimi_cluster);
		System.out.println();
		
		
		/*
		 * CLUSTERING DELL'ELENCO DI PAROLE DISAMBIGUATE CONNESSE ALLA PAROLA CHIAVE IN ESAME
		 * ATTRAVERSO RFCM CLUSTERING, ED ASSEGNAZIONE DI UN NOME AD OGNI CLUSTER PRENDENDO 
		 * IL CONCETTO CON MASSIMA SIMILARITA' CON LA GLOSSA DELLA PAROLA CHIAVE, MEDIANTE MISURA DI LESK.
		 */
		int overlap_cluster=10;
		clust=new DatasetClustering(overlap_cluster);
		BufferedReader br = utils.apriFile("resources/testo_disambiguato.txt", "input", true);
		String strLine=null;
		HashMap<String,String> concetti_disambiguati = new HashMap<String,String>();
		while ((strLine=br.readLine()) != null)
		{
			int index = strLine.indexOf("\t");
			String id = strLine.substring(0, index);
			String termine = strLine.substring(index + "\t".length());
			if (!concetti_disambiguati.containsKey(id) && !concetti_disambiguati.containsValue(termine))
			{
				concetti_disambiguati.put(id, termine);
			}
		}
		clust.setDataset(concetti_disambiguati, parole_chiavi.elementAt(0));
		clust.beginClustering();
		clust.calcolaNomiPerGlossOverlaps(hashmap_centroidi_overlap);
		Iterator<Entry<String, HashMap<String,Double>>> it = hashmap_centroidi_overlap.entrySet().iterator();
		
		
		/*
		 *  ESTRAZIONE DELLE CATEGORIE PIU' SIGNIFICATIVE DI WIKIPEDIA CON TEXTRANK.
		 */
		/*if (categorie_di_wikipedia.size()!=0)
        {
			StringBuilder info_categorie = new StringBuilder();
			for (int i=0; i<categorie_di_wikipedia.size(); i+=1) {
				info_categorie=info_categorie.append(" ").append(categorie_di_wikipedia.get(i));
			}
			try {
				trc.prepCall(info_categorie.toString().replaceAll("_"," "), use_wordnet);
				trc.doCategoryRanking(word_rem.stopWordsClear(info_categorie.toString().replaceAll("_"," ")));
			} catch (Exception e) {
				e.printStackTrace();
			}
			         
			categorie_wikipedia_textrank.addAll(trc.getKeyWord());
			System.out.println("Parole textrank categorie: "+categorie_wikipedia_textrank);
			trc.reset();
					
        }*/

		
		List<String> stubDataForKumo = new ArrayList<String>(0);
		List<String> allDataForKumo = new ArrayList<String>(0);
		stubDataForKumo.addAll(iperonimi_cluster);
		stubDataForKumo.addAll(iperonimi_di_wordnet);
		stubDataForKumo.addAll(parole_significative_textrank);
		stubDataForKumo.addAll(hashtags);
	    while (it.hasNext()) 
	    {
	    	 Map.Entry<String, HashMap<String,Double>> pair = (Map.Entry<String, HashMap<String,Double>>)it.next();
	         String centroide = (String) pair.getKey();
	         stubDataForKumo.add(centroide);
	    }
		for (String oggetto : stubDataForKumo) 
		{
			if (!oggetto.equalsIgnoreCase(parole_chiavi.elementAt(0)))
			{
				allDataForKumo.add(oggetto);
			}
		}
		
		//Collection<String> allDataNoDups = new LinkedHashSet<String>(allDataForKumo);
		
		File file = new File("resources/kumo_cloud.txt");
		if (!file.exists())
		{
			boolean leak = file.createNewFile();
			if (!leak) {
				System.err.println("ERRORE: Impossibile aprire o creare il file");
			}
		} 
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			utils.scrivi(writer, allDataForKumo);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
		System.out.println();
		
		
		/*
		 *  COSTRUZIONE DI UNA WORD CLOUD DALLE SEQUENZE DI PAROLE OTTENUTE
		 *  POSSIBILI TIPI DI WORD CLOUD: circular,image,rectangle,layered,polarity;
		 *  
		 *  INFO. Per una WordCloud scritta su immagine --> kumo.setPercorsoImmagine(String percorsoImmagine);
		 *  Oppure utilizzare il costruttore appositamente scritto.
		 */
		CloudOfWords kumo = new CloudOfWords();
		kumo.setInputFile("resources/kumo_cloud.txt");
		kumo.setDimensione(new Dimension(500,500));
		kumo.setParolaChiave(parole_chiavi.elementAt(0));
		kumo.setTipologia("circular");
		kumo.buildCloud();
		
		System.out.println("Ho impiegato "+(System.currentTimeMillis()-startTime)+"ms per la costruzione!");
		
		
		/*
		 *  COSTRUZIONE DELLA MAPPA SEMANTICA DELLA CONVERSAZIONE CORRENTE.
		 */
		costruisciMappaSemantica(parole_chiavi.elementAt(0));
		
	}
	
	
	/**
	 * Costruisce il grafo semantico per la parola chiave corrente. Dopo aver
	 * raccolto tutti i dati, GraphStream costruisce il grafo semantico
	 * connettendo tutti i nodi alla parola chiave di ricerca, ed eventualmente
	 * se due o più nodi appartengono alla stessa conversazione li unisce con un
	 * arco.
	 * @param parola_chiave_estratta
	 *            parola chiave corrente
	 * @throws InterruptedException
	 */
	public void costruisciMappaSemantica(String parola_chiave_estratta) throws InterruptedException 
	{
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		final String dir="grafo-semantico";
		utils.mkdir(dir);
		
		Graph sottografo=new SingleGraph("SOTTOGRAFO"); 
		String parola_chiave_in_esame=parola_chiave_estratta;
		
		/*
		 *  INIZIA A COSTRUIRE IL SOTTO-GRAFO AGGIUNGENDO LA PAROLA CHIAVE.
		 */
        aggiungiNodo(sottografo,parola_chiave_in_esame,"nodo_chiave",false);
        
        /*
         *  E AGGIUNGE GLI HASHTAGS ASSOCIATI.
         */
        aggiungiNodiAlGrafo(sottografo,new ArrayList<String>(hashtags),"nodo_hashtag",false);
        
        fondiGrafi(grafo_originale,sottografo);
        collegaTuttiNodiAllaRadice(sottografo, parola_chiave_in_esame, "nodo_chiave");
        
        aggiungiNodiAlGrafo(grafo_originale,new ArrayList<String>(iperonimi_cluster),"nodo_ner",false);
        aggiungiNodiAlGrafo(grafo_originale,new ArrayList<String>(parole_significative_textrank),"nodo_textrank",false);
        aggiungiNodiAlGrafo(grafo_originale,new ArrayList<String>(iperonimi_di_wordnet),"nodo_iperonimo",false);
        
        ArrayList<String> iperonimi_cluster_overlap = new ArrayList<String>(0);
        for (Map.Entry<String, HashMap<String,Double>> entry : hashmap_centroidi_overlap.entrySet())
        	iperonimi_cluster_overlap.add(entry.getKey());
        aggiungiNodiAlGrafo(grafo_originale,iperonimi_cluster_overlap,"nodo_overlap",false);
        
        collegaTuttiNodiAllaRadice(grafo_originale, parola_chiave_in_esame, "nodo_chiave");
        
        for (Node nodo : grafo_originale.getEachNode())
        {
        	if (nodo.getAttribute("ui.class").equals("nodo_hashtag"))
    		{
        		associaNodoAllaConversazione(nodo);
    		}
    		else if (nodo.getAttribute("ui.class").equals("nodo_textrank")) 
    		{
    			associaNodoAllaConversazione(nodo);
    		}
    		else if (nodo.getAttribute("ui.class").equals("nodo_ner"))
    		{
    			associaNodoAllaConversazione(nodo);
    		}
    		else if (nodo.getAttribute("ui.class").equals("nodo_iperonimo"))
    		{
    			associaNodoAllaConversazione(nodo);
    		}
    		else if (nodo.getAttribute("ui.class").equals("nodo_antenato"))
    		{
    			associaNodoAllaConversazione(nodo);
    		}
    	}
        for (Integer key : nodeMultimap.keySet())
        {
        	//Ottengo i valori di ogni key
        	Vector<String> nodeValues = new Vector<String>();
        	nodeValues.addAll(nodeMultimap.get(key));
        	if (nodeValues.size() > 1)
        	{
        		for (int i = 0; i < nodeValues.size()-1; i++)
        		{
        			for (int j = i+1; j < nodeValues.size(); j++)
        			{
        				//Tutti quei nodi che appartengono alla stessa conversazione sono interconnessi
        				String inizio = nodeValues.get(i);
        				String fine = nodeValues.get(j);
        				aggiungiArco(grafo_originale,inizio,fine,"arco_semplice",false);
        			}
        		}
        	}
        }
        grafo_originale.addAttribute("ui.title", "GraphStream: Semantic Graph");
        grafo_originale.addAttribute("ui.stylesheet", this.stylesheet);
        grafo_originale.addAttribute("ui.antialias");
        grafo_originale.addAttribute("ui.quality");
        Viewer viewer = grafo_originale.display();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.EXIT);
        grafo_originale.addAttribute("ui.screenshot", dir+"/graphstream_semantic_map_"+parola_chiave_in_esame.toLowerCase()+".png");
        fromViewer = viewer.newViewerPipe();
        fromViewer.addViewerListener(this);
        fromViewer.addSink(grafo_originale);
        while (loop) 
        {
            fromViewer.pump();
            Thread.sleep(100);
        }
        
	}
	

	private void collegaTuttiNodiAllaRadice(Graph grafo, String radice,String categoria_radice) {
        aggiungiNodo(grafo,radice,categoria_radice,false);
        for (org.graphstream.graph.Node nodo: grafo.getEachNode())
        {
            if (!nodo.getId().toLowerCase().equals(radice)) {
                aggiungiArco(grafo,radice.toLowerCase(),nodo.getId().toLowerCase(),"",false);
            }
        }
    }
	
	
	/**
	 * Effettua il merge tra il sottografo ed il grafo principale.
	 * @param destinazione
	 *            il grafo principale
	 * @param grafo_da_aggiungere
	 *            il sottografo da unire
	 */
	private void fondiGrafi(Graph destinazione, Graph grafo_da_aggiungere) 
	{
        for (Node nodo : grafo_da_aggiungere.getEachNode()) {
            if (destinazione.getNode(nodo.getId())!=null && grafo_da_aggiungere.getNode(nodo.getId()).getAttribute("ui.class").equals("nodo_chiave"))
                sostituisciTipoNodo(destinazione,nodo.getId(),"nodo_chiave");
            else    
                aggiungiNodo(destinazione,nodo.getId(),(String)nodo.getAttribute("ui.class"),false);
        }
        
        for (Edge arco : grafo_da_aggiungere.getEachEdge()) {
            aggiungiArco(destinazione,arco.getNode0().getId(),arco.getNode1().getId(), (String) arco.getAttribute("ui.class"),false);
        }
        
    }
	
	
	/**
	 * Questo metodo si occupa di aggiungere un nodo al grafo, se non presente.
	 * Se @param sovrascrivi è impostato a true, viene sovrascritto il nodo
	 * corrente.
	 * @param grafo
	 *            il grafo a cui aggiungere il nodo
	 * @param nome_nodo
	 *            il nome del nodo da aggiungere
	 * @param categoria
	 *            il tipo di nodo
	 * @param sovrascrivi
	 *            valore booleano
	 */
	void aggiungiNodo(Graph grafo, final String nome_nodo, final String categoria, boolean sovrascrivi)
    {
	     if (grafo.getNode(nome_nodo.toLowerCase())==null)
	        {
	            Node nodo_nuovo=grafo.addNode(nome_nodo.toLowerCase());
	             nodo_nuovo.addAttribute("ui.label", nome_nodo);
	             nodo_nuovo.setAttribute("ui.class", categoria);
	        }
	        else
	            if (sovrascrivi) 
	                sostituisciTipoNodo(grafo,nome_nodo.toLowerCase(),categoria);
	            
    }
	
	
	private void aggiungiNodiAlGrafo(Graph ritorno, ArrayList<String> nomi_nodi, String categoria, boolean sovrascrivi) 
	{

		for (String nome_nodo: nomi_nodi)
        {
            aggiungiNodo(ritorno, nome_nodo, categoria, sovrascrivi);
        }
        
    }
	
	
	static void sostituisciTipoNodo(Graph grafo, String id_nodo, final String nuova_categoria) {
		 if (grafo.getNode(id_nodo)!=null) 
			 grafo.getNode(id_nodo).setAttribute("ui.class",nuova_categoria);
    }
	
	
	/**
	 * Si occupa di unire con un arco due nodi del grafo se questi non sono
	 * ancora stati congiunti.
	 * @param grafo
	 *            il grafo corrente
	 * @param inizio
	 *            il nodo di partenza
	 * @param fine
	 *            il nodo di arrivo
	 * @param tipo_arco
	 *            il tipo di arco
	 * @param sostituisci
	 *            valore booleano
	 */
	void aggiungiArco(Graph grafo,String inizio,String fine, String tipo_arco,boolean sostituisci) {
		if (!cercaArco(grafo,inizio,fine) && grafo.getNode(inizio.toLowerCase())!=grafo.getNode(fine.toLowerCase()))
        {
            Edge e=grafo.addEdge("arco_"+inizio.toLowerCase()+"_"+fine.toLowerCase(),inizio.toLowerCase(),fine.toLowerCase());
            if (!tipo_arco.equals("")) {
                e.setAttribute("ui.class", tipo_arco);
            }
            else {
                e.setAttribute("ui.class","arco_semplice");
            }
        }
        else if (cercaArco(grafo,inizio,fine) && sostituisci) {
         Edge e=grafo.getEdge("arco_"+inizio.toLowerCase()+"_"+fine.toLowerCase());
         if (!tipo_arco.equals(""))
            e.setAttribute("ui.class", tipo_arco);
         else
            e.setAttribute("ui.class","arco_semplice");
        }
	}
	
	
	static public boolean cercaArco(Graph grafo,String inizio,String fine) {
		 boolean arco_trovato=false;
		 for (org.graphstream.graph.Edge e:grafo.getEachEdge())
		 {
			 org.graphstream.graph.Node nodo_zero=e.getNode0();
			 org.graphstream.graph.Node nodo_uno=e.getNode1();
			 
			 if (nodo_zero.getId().equalsIgnoreCase(inizio)&& nodo_uno.getId().equalsIgnoreCase(fine)) {
				 arco_trovato=true;
				 break;
			 }
			 
			 if (nodo_zero.getId().equalsIgnoreCase(fine)&& nodo_uno.getId().equalsIgnoreCase(inizio)) {
				 arco_trovato=true;
				 break;
			 }
		 }
		 return arco_trovato;
	}
	
	
	/**
	 * Vengono connessi con un arco i nodi del grafo appartenenti alla stessa
	 * conversazione.
	 * @param nodo
	 *            il nodo corrente
	 * @return valore booleano associato/non-associato
	 */
	public boolean associaNodoAllaConversazione(Node nodo) 
	{
		boolean associato = false;
		for (Entry<Integer, String> entry : hashmap_con_conversazioni.entrySet())
    	{
			String[] corpus = StringUtils.split(entry.getValue());
			for (String word : corpus)
			{
				if (word.trim().equalsIgnoreCase(nodo.getId()))
				{
					nodeMultimap.put(entry.getKey(), nodo.getId());
					associato = true;
					return associato;
				}
			}
    	}
		return associato;
	}
	
	
	private void setStylesheet() {
		this.stylesheet = StyleImporter.getStyle("style.css");
	}
	
	
	@Override
	public void buttonPushed(String id) 
	{
		System.out.println("Button pushed on node "+id+".");
		Node nodo = grafo_originale.getNode(id);
		boolean isConnectedToChiave = nodo.hasEdgeBetween(parole_chiavi.elementAt(0).toLowerCase());
		if ( nodo.getAttribute("ui.class").equals("nodo_overlap") && isConnectedToChiave )
		{
			int edgeCount=0;
			for ( Edge edge : nodo.getEachEdge() )
			{
				edgeCount++;
			}
			if ( edgeCount > 1 )
			{
				/*
				 * Il nodo cliccato è gia espanso
				 */
				richiudiNodo(nodo);
			} 
			else
			{
				/*
				 * Espandiamo il nodo cliccato
				 */
				espandiNodo(nodo);
			}
		}
	}
	
	
	/**
	 * Espande il nodo cliccato, mostrando gli oggetti ad esso associati.
	 * @param nodo_cliccato
	 *            il nodo cliccato
	 */
	protected void espandiNodo(Node nodo_cliccato) 
	{
		ArrayList<String> nodi_overlap = new ArrayList<String>(0);
		for (Map.Entry<String, HashMap<String, Double>> entry : hashmap_centroidi_overlap.entrySet())
			nodi_overlap.add(entry.getKey());
		if (nodi_overlap.contains(nodo_cliccato.getId())) 
		{
			Node nodo_radice = grafo_originale.getNode(nodo_cliccato.getId().toLowerCase());
			HashMap<String, Double> nodi_da_aggiungere = new HashMap<String, Double>(0);
			Vector<String> nodi_gia_presenti = new Vector<String>();
			nodi_da_aggiungere.putAll(hashmap_centroidi_overlap.get(nodo_cliccato.getId()));
			// Estraggo i nodi già presenti nel grafo semantico
			for (Node nodi_grafo : grafo_originale.getEachNode())
			{
				nodi_gia_presenti.add(nodi_grafo.getId().toLowerCase());
			}
			for (Map.Entry<String, Double> entry : nodi_da_aggiungere.entrySet()) 
			{
				//Se il nodo non è presente in quelli gia visualizzati
				if (!nodi_gia_presenti.contains(entry.getKey().toLowerCase()))
				{
					aggiungiNodo(grafo_originale, entry.getKey()+":"+entry.getValue(), "nodo_overlap", false);
					aggiungiArco(grafo_originale, entry.getKey()+":"+entry.getValue(), nodo_radice.getId(), "arco_semplice", false);
				}
			}
		}
		System.out.println("Node "+nodo_cliccato+" expanded.");
	}
	
	
	/**
	 * Il nodo è stato già espanso, viene richiuso.
	 * @param nodo_cliccato
	 *            il nodo cliccato
	 */
	protected void richiudiNodo(Node nodo_cliccato)
	{
		ArrayList<String> nodi_overlap = new ArrayList<String>(0);
		for (Map.Entry<String, HashMap<String, Double>> entry : hashmap_centroidi_overlap.entrySet())
			nodi_overlap.add(entry.getKey());
		if ( nodi_overlap.contains(nodo_cliccato.getId()) ) 
		{
			HashMap<String,Double> nodi_da_aggiungere = new HashMap<String,Double>(0);
			ArrayList<String> nodi_visualizzati = new ArrayList<String>(0);
			nodi_da_aggiungere.putAll(hashmap_centroidi_overlap.get(nodo_cliccato.getId()));
			//Estraggo i nodi visualizzati dopo l'espansione
			for (Map.Entry<String,Double> entry : nodi_da_aggiungere.entrySet())
	    	{
				String nodo_similarita = entry.getKey()+":"+entry.getValue();
				nodi_visualizzati.add(nodo_similarita);
	    	}
			for (int i=0; i<nodi_visualizzati.size(); i++)
			{
				boolean trovato = cercaArco(grafo_originale, nodi_visualizzati.get(i), nodo_cliccato.getId());
				Node nodo_visualizzato = grafo_originale.getNode(nodi_visualizzati.get(i)); 
				//Se ho trovato un arco ed il nodo è visualizzato
				if ( trovato && nodo_visualizzato.getAttribute("ui.class").equals("nodo_overlap") )
				{
					grafo_originale.removeEdge(nodi_visualizzati.get(i), nodo_cliccato.getId());
					grafo_originale.removeNode(nodi_visualizzati.get(i));
				}
			}
			System.out.println("Node "+nodo_cliccato+" closed.");
		}
	}
	

	@Override
	public void buttonReleased(String id) 
	{
		fromViewer.pump();
	}


	@Override
	public void viewClosed(String id) 
	{
		loop = false;
	}
	


}
