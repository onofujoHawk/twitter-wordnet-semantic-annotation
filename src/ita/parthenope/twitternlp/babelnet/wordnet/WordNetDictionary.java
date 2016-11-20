/*
 * Università degli Studi di Napoli Parthenope
 */
package ita.parthenope.twitternlp.babelnet.wordnet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import it.uniroma1.lcl.babelnet.BabelSynsetType;
import it.uniroma1.lcl.babelnet.data.BabelCategory;
import it.uniroma1.lcl.babelnet.data.BabelGloss;
import it.uniroma1.lcl.jlt.util.Language;
import ita.parthenope.twitternlp.semantic.SimilarityMeasures;
import ita.parthenope.twitternlp.utils.Utility;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.Word;





/**
 * Estrazione degli iperonimi dal database di WordNet 2.1
 * attraverso API per la ricerca.
 * @author onofrio
 *
 */
public class WordNetDictionary extends SimilarityMeasures {

	BabelNet bn;
	
	//Glosse
	private List<String> glosse_estratte;
	private Map<String, Integer> sortedMap;
	private List<String> iperonimiFrequenti;
	//Dizionario di WordNet
	private ArrayList<String> hash_ner;
	private Dictionary dictionary;
	String dizionario;
	URL url;
	
	private Utility utility=new Utility();
	//Categorie Wikipedia
	private Vector<String> named_entity_wikipedia;
	private Vector<String> categorie_di_wikipedia_finale;


	public WordNetDictionary() {
		super();
		setSortedMap(new HashMap<>());
		setIperonimiFrequenti(new ArrayList<>());
		glosse_estratte=new ArrayList<String>();
		hash_ner=new ArrayList<String>();
		named_entity_wikipedia=new Vector<String>();
		categorie_di_wikipedia_finale=new Vector<String>();

		try {
			this.bn = BabelNet.getInstance();
			System.out.println("BabelNet getInstance OK\n");
		} catch (Exception e) {
			System.err.println("ERRORE BabelNet getInstance\n");
			e.printStackTrace();
		}
		
		/*
		 * Bootstrap dizionario WordNet 2.1 per JWI.
		 */
		try {
			initJWIDictionary();
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
		}
	}

	
	/**
	 * Viene estratta la glossa per un dato BabelSynset definito in input.
	 * @param entita
	 *            l'entita disambiguata da Babelfy
	 * @param id
	 *            identificativo del synset
	 * @throws RuntimeException
	 */
	public void babelGloss(String entita, final String id) throws RuntimeException
	{
		/*
		 * PER OGNI SYNSET ESTRAGGO LA SUA GLOSSA, CHE CORRISPONDE ALLA DEFINIZIONE IN BABELNET.
		 */
		List<BabelSynset> synsets=
				bn.getSynsets( entita, Language.EN );
		if ( !glosse_estratte.isEmpty() )
		{
			glosse_estratte.clear();
		}
		for ( BabelSynset synset : synsets )
		{
			if ( synset.getId().getID().equals(id) )
			{
				//Significa che il synset è giusto e corrisponde a quelli prodotti dalla disambiguazione
				System.out.println("TROVATO IL SYNSET CON ID: "+synset.getId().getID()+ "... ESTRAZIONE DELLE BABEL_GLOSS");
				List<BabelGloss> babel_gloss=synset.getGlosses(Language.EN);
				for ( BabelGloss glossa:babel_gloss )
				{
					System.out.println("BN::Glossa: "+glossa.getGloss().toLowerCase());
					this.glosse_estratte.add( glossa.getGloss().toLowerCase() );
				}
				break;
			}
		}
	
	}
	
	
	private void initJWIDictionary() {
		this.dizionario = new StringBuilder().append(dir_diz).append("/dict").toString();
		try {
            url = new URL("file", null, dizionario);
        }
        catch( MalformedURLException malformedurlexception ) {
        	System.err.println("ERROR: could not find: "+dir_diz+"/dict");
            malformedurlexception.printStackTrace();
        }

		//Apertura dizionario di WordNet 2.1
		dictionary = new Dictionary(url);
		if( !dictionary.isOpen() )
			dictionary.open();
	}


	/**
	 * Inizializza il dizionario WordNet 2.1 della libreria JWNL.
	 * @throws FileNotFoundException
	 */
	public void initJWNLDictionary() throws FileNotFoundException {
		File file = new File("config/jwnl_properties.xml");
		try {
		    if( !file.exists() || file.isDirectory() ) {
		      throw new RuntimeException("ERROR: couldn't find JWNL xml properties file: " + file.getName());
		    }
			JWNL.initialize( new FileInputStream(file) );
			System.out.println("WordNet initialized from: "+file.getName());
		}
		catch( Exception ex ) {
			System.err.println("ERROR: could not find: "+file.getAbsolutePath());
			ex.printStackTrace();
		}
	}


	/**
	 * Ottengo l'iperonimo più frequente estratto dal dizionario WordNet 2.1 Se
	 * la BabelGloss della parola confrontata è uguale alla sua definizione in
	 * WordNet allora viene estratto l'iperonimo diretto.
	 * @param hash_ner
	 *            List<String> di nomi per lo Stanford NER
	 * @return una List<String> contenente gli iperonimi più frequenti
	 * @throws IOException
	 */
	public List<String> iperonimiWordNetJWI(List<String> hash_ner) throws IOException
	{
		List<String> hypernymsList = new ArrayList<String>();
		BufferedReader br = utility.apriFile("resources/testo_disambiguato.txt", "input", true);
		String strLine = null;
		while ((strLine = br.readLine()) != null)
		{
			IIndexWord idxWord=null;
			int index = strLine.indexOf("\t");
			String id = strLine.substring(0, index);
			String term = strLine.substring(index + "\t".length());
			System.out.println("Cerco "+id+"-"+term+" su WordNet...");
			
			if (id.endsWith("n")) {
				idxWord = dictionary.getIndexWord(term.toLowerCase(), POS.NOUN);
			}
			else if (id.endsWith("v") && !term.equals("")) {
				idxWord = dictionary.getIndexWord(term.toLowerCase(), POS.VERB);
			}

			if (idxWord != null)
			{
				System.out.println("Riconosciuto su Wordnet: "+term+"  POS: "+idxWord.getPOS());
				babelGloss(term, id);
				List<IWordID> wordIDs = idxWord.getWordIDs();

				for (IWordID wordID : wordIDs)
				{
					IWord word = dictionary.getWord(wordID);
					System.out.println("WN::Glossa: "+word.getSynset().getGloss());

					if (glosse_estratte.contains(word.getSynset().getGloss()))
					{
						//Estraggo il synset
						ISynset synset = word.getSynset();

						//Ottengo gli iperonimi
						List<ISynsetID> hypernyms = synset
								.getRelatedSynsets(Pointer.HYPERNYM);

						if(hypernyms.size() > 0)
		                {
							//Prendo l'iperonimo di primo livello che mi restituisce JWI
							ISynsetID sid=hypernyms.get(0);
							String hypernym = dictionary.getSynset(sid).getWord(1).getLemma().toLowerCase();
							System.out.println("TERMINE: "+term+"  POS: "+sid.getPOS()+"  IPERONIMO: "+hypernym);
							hypernymsList.add(hypernym);
		                }
					}
				}
			}
			System.out.println();
		}
		//Abbiamo terminato... vado a cercare gli iperonimi più frequenti
		if (hypernymsList.size() > 0) {
			contaIperonimiAndGetMax(hypernymsList);
			hash_ner.addAll(this.hash_ner);
		}
		else {
			System.err.println("ATTENZIONE: Nessun iperonimo estratto da WordNet 3.0");
		}
		return (!iperonimiFrequenti.isEmpty()) ? iperonimiFrequenti : null;
	}
	
	
	public void iperonimiAllSenses(ArrayList<String> iperonimi) throws IOException, RuntimeException {
		List<String> hypernymsList = new ArrayList<String>();
		BufferedReader br = utility.apriFile("resources/testo_disambiguato.txt", "input", true);
		String strLine = null;
		while ((strLine = br.readLine()) != null)
		{
			IIndexWord idxWord=null;

			int index = strLine.indexOf("\t");
			String id = strLine.substring(0, index);
			String term = strLine.substring(index + "\t".length());
			System.out.println("Cerco "+id+"-"+term+" su WordNet...");
			
			if (id.endsWith("n")) {
				idxWord = dictionary.getIndexWord(term.toLowerCase(), POS.NOUN);
			}
			else if (id.endsWith("v") && !term.equals("")) {
				idxWord = dictionary.getIndexWord(term.toLowerCase(), POS.VERB);
			}

			if (idxWord != null)
			{
				System.out.println("Riconosciuto su Wordnet: "+term+"  POS: "+idxWord.getPOS());
				babelGloss(term, id);
				List<IWordID> wordIDs = idxWord.getWordIDs();

				for (IWordID wordID : wordIDs)
				{
					IWord word = dictionary.getWord(wordID);
					System.out.println("WN::Glossa: "+word.getSynset().getGloss());

					if (glosse_estratte.contains(word.getSynset().getGloss()))
					{
						//Estraggo il synset
						ISynset synset = word.getSynset();

						//Ottengo gli iperonimi
						List<ISynsetID> hypernyms = synset
								.getRelatedSynsets(Pointer.HYPERNYM);

						if(hypernyms.size() > 0)
		                {
							List<IWord> words;
							for(ISynsetID sid : hypernyms)
							{
								words = dictionary.getSynset(sid).getWords();
								for(Iterator<IWord> i = words.iterator(); i.hasNext();)
								{
									String hypernym = i.next().getLemma().toLowerCase();
									//Salvo gli iperonimi diretti che mi restituisce JWI
									System.out.println("TERMINE: "+term+"  POS: "+sid.getPOS()+"  IPERONIMO: "+hypernym);
									hypernymsList.add(hypernym);
								}
							}
		                }
					}
				}
			}
			System.out.println();
		}
		if (hypernymsList.size() > 0) {
			contaIperonimiAndGetMax(hypernymsList);
			iperonimi.addAll(this.iperonimiFrequenti);
		}
		else {
			System.err.println("ATTENZIONE: Nessun iperonimo estratto da WordNet 3.0");
		}
	}
		
	
	/**
	 * Lowest common ancestor estratto dal confronto di due iperonimi.
	 * @param concetto1
	 *            il primo iperonimo
	 * @param concetto2
	 *            il secondo iperonimo
	 * @param POS
	 *            la POS corrispondente ai due termini
	 * @param antenato_in_comune
	 *            lista contenente gli antenati comuni
	 * @throws JWNLException
	 * @throws FileNotFoundException
	 */
	public void lowestCommonAncestor(final String concetto1, final String concetto2, String POS, ArrayList<String> antenato_in_comune)
			throws JWNLException, FileNotFoundException
	{
		IndexWord idxWord1=null;
		IndexWord idxWord2=null;
		initJWNLDictionary();
		
		if ( POS.equals("n") ) {
			idxWord1 = net.didion.jwnl.dictionary.Dictionary.getInstance()
					.lookupIndexWord(net.didion.jwnl.data.POS.NOUN, concetto1);
			idxWord2 = net.didion.jwnl.dictionary.Dictionary.getInstance()
					.lookupIndexWord(net.didion.jwnl.data.POS.NOUN, concetto2);
		} 
		else 
			if ( POS.equals("v") ) {
				idxWord1 = net.didion.jwnl.dictionary.Dictionary.getInstance()
					.lookupIndexWord(net.didion.jwnl.data.POS.VERB, concetto1);
				idxWord2 = net.didion.jwnl.dictionary.Dictionary.getInstance()
						.lookupIndexWord(net.didion.jwnl.data.POS.NOUN, concetto2);
		} 
		
		if ( idxWord1 != null && idxWord2 != null ) 
		{
			//Caricamento dei synsets
			net.didion.jwnl.data.Synset[] idxWordSenses1 = idxWord1.getSenses();
			net.didion.jwnl.data.Synset[] idxWordSenses2 = idxWord2.getSenses();
			
			//Estraiamo la catena d'iperonimi di tutti i significati della prima e seconda word
			for ( int idx1=0; idx1<idxWordSenses1.length; idx1++ )
			{
				for ( int idx2=0; idx2<idxWordSenses2.length; idx2++ )
				{
					Vector<String> iperonimi1 = new Vector<String>(0);
					Vector<String> iperonimi2 = new Vector<String>(0);
					
					WordNetDictionary.hypernymChain( idxWordSenses1[idx1], iperonimi1 );
					if ( iperonimi1.size() > 0 )
					{
						System.out.println("SIGNIFICATO #"+idx1+" PER: "+idxWord1.getLemma());
						System.out.println("Lemma: " + idxWord1.getLemma()+ "POS: "+idxWord1.getPOS().getLabel());
						System.out.print("Iperonimi = {");
						for ( Iterator<String> iterator = iperonimi1.iterator(); iterator.hasNext(); ) {
							System.out.print(iterator.next());
							if (iterator.hasNext()) {
								System.out.print(", ");
							}
						}
						System.out.println("}");
					}
					
					WordNetDictionary.hypernymChain( idxWordSenses2[idx2], iperonimi2 );
					if ( iperonimi2.size() > 0 )
					{
						System.out.println("SIGNIFICATO #"+idx2+" PER: "+idxWord2.getLemma());
						System.out.println("Lemma: " + idxWord2.getLemma()+" POS: "+idxWord2.getPOS().getLabel());
						System.out.print("Iperonimi = {");
						for ( Iterator<String> iterator = iperonimi2.iterator(); iterator.hasNext(); ) {
							System.out.print(iterator.next());
							if (iterator.hasNext()) {
								System.out.print(", ");
							}
						}
						System.out.println("}");
					}
					
					//Ricerchiamo il minimo antenato comune (lcs) alle due word
					String radice_generica="";
					if ( iperonimi1.size() > 4 )
					{
						//Da non oltrepassare il livello 4 di profondità dell'albero degli iperonimi
						int profondita = 4;
						radice_generica = iperonimi1.get(iperonimi1.size()-profondita);
					} else 
						if ( iperonimi1.size() == 1 ) {
							radice_generica = iperonimi1.get(0);
					}
					
					Breakpoint:
					for ( int idx_elenco1=0; idx_elenco1<iperonimi1.size(); idx_elenco1++ )
					{
						for ( int idx_elenco2=0; idx_elenco2<iperonimi2.size(); idx_elenco2++ )
						{
							System.out.println("Confronto dell'iperonimo \""+iperonimi1.get(idx_elenco1)+"\" con \""+iperonimi2.get(idx_elenco2)+"\"");
							ArrayList<String> restanti_iperonimi1 = new ArrayList<String>(0);
							ArrayList<String> restanti_iperonimi2 = new ArrayList<String>(0);
							
							//I due concetti sono simili?
							if ( iperonimi1.get(idx_elenco1).equals(iperonimi2.get(idx_elenco2)) )
							{
								int idx_iperonimo = iperonimi1.indexOf(iperonimi1.get(idx_elenco1));
								int idx_radice_generica = iperonimi1.indexOf(radice_generica);
								
								if ( idx_iperonimo < idx_radice_generica ) 
								{
									System.out.println("\rTROVATO MINIMO IPERONIMO COMUNE (LCA) TRA \""+concetto1+"\" E \""+concetto2+"\" = "+iperonimi1.get(idx_elenco1)+" <-> "+iperonimi2.get(idx_elenco2)+".\r");
									
									//Salvataggio dei restanti iperonimi a partire dal lcs-1 a 0
									for ( int idx_restanti1=0; idx_restanti1<idx_elenco1; idx_restanti1++ ) {
										restanti_iperonimi1.add(iperonimi1.get(idx_restanti1));
									} 
									for ( int idx_restanti2=0; idx_restanti2<idx_elenco2; idx_restanti2++ ) {
										restanti_iperonimi2.add(iperonimi2.get(idx_restanti2));
									}
									
									if (iperonimi1.get(idx_elenco1).equals(iperonimi2.get(idx_elenco2)))
											antenato_in_comune.add(iperonimi1.get(idx_elenco1));
											
									break Breakpoint;
								}
							}
						}
					}
				}
				System.out.println();
			}
		}
		
	}
	
	
	/**
	 * Viene fatto lo stemming del termine, inoltre in questa fase si ricava se
	 * l'entita è un nome o un verbo e se e'riconosciuta da WordNet oppure no.
	 * @param termine
	 *            la parola da riconoscere su WordNet
	 * @param pos
	 *            part of speech del termine
	 * @param PartOfSpeech
	 *            Vector<String> contenente la POS tag della parola
	 * @return il termine riconosciuto
	 */
	public String lin_stemmer(String termine, final String pos) 
	{
        final String nomecor = "ic-semcor.dat";
        final String semcor_dir ="C:/Users/onofr/Desktop/workspace/Twitter-NLP-Thesis/";	
        System.out.println("STEMMING IN CORSO PER "+termine+".");
        ICFinder ICfinder_cor= new ICFinder (semcor_dir + nomecor);
        this.lin = new customedu.sussex.nlp.jws.Lin (dictionary, ICfinder_cor);
        List<String> wordRoots1 = new ArrayList<String>(0);
        if (pos.equalsIgnoreCase("n")) 
        {
        	wordRoots1 = lin.stemmer.findStems(termine, POS.NOUN);
        	if (wordRoots1.isEmpty()==true) 
        	{
        		//Entità non riconosciuta dal dizionario
				System.out.flush();
				System.err.println("NON riconosciuto/a da WordNet: "+termine);
        		return"";
        	}
        	System.out.println("Riconosciuto/a da WordNet: "+wordRoots1.get(0)+" POS: NOUN");
        } 
        else if (pos.equalsIgnoreCase("v"))
        {
        	wordRoots1 = lin.stemmer.findStems(termine, POS.VERB);
        	if (wordRoots1.isEmpty()==true) 
        	{
        		//Entità non riconosciuta dal dizionario
				System.out.flush();
				System.err.println("NON riconosciuto/a da WordNet: "+termine);
        		return"";
        	}
        	System.out.println("Riconosciuto/a da WordNet: "+wordRoots1.get(0)+" POS: VERB");
        } else 
        {
        	return "";
        }
		return wordRoots1.get(0);
	}

	
	/**
	 * Sono estratte le categorie di Wikipedia dai synset: se un entità del NER
	 * è presente anche in quelle disambiguate da Babelfy, andiamo ad estrarre
	 * le categorie di Wikipedia del synset.
	 * @param entita_del_ner
	 *            lista di entity del NER
	 * @param categorie_da_wikipedia
	 *            vector contenente le categorie estratte per synset
	 * @throws IOException
	 */
	public void categorieWikipedia(List<String> entita_del_ner, Vector<String> categorie_da_wikipedia) throws IOException 
	{
		BufferedReader br = new Utility().apriFile("resources/testo_disambiguato.txt", "input", true);
		String strLine = "";
		
		while ( (strLine = br.readLine()) != null )
		{
			int index = strLine.indexOf("\t");
			String id = strLine.substring(0, index); 
			String term = strLine.substring(index + "\t".length());
			System.out.println("Ho estratto "+id+" "+term+" da Babelfy...");
			
			if( confrontaEntityNERvsBabelfy(term,entita_del_ner) ) 
			{
				// RICAVIAMO I SYNSET DALLA NAMED ENTITY DISAMBIGUATA
				List<BabelSynset> synsets_termine = bn.getSynsets( term, Language.EN );
				boolean voce_wikipedia_trovata = false;
				
				for( BabelSynset synset : synsets_termine ) 
				{
					if ( synset.getId().getID().equals(id) ) 
					{
						 System.out.println("\nSto processando: "+synset);
						 String categoria="";
						 List<BabelSense> babel_senses;

						 // IL SYNSET CORRENTE CONTIENE WIKI
						 if ( synset.toString(Language.EN).contains("WIKI") || synset.getSynsetType()==BabelSynsetType.NAMED_ENTITY )
						 {
							 System.out.println("TROVATO UNA NAMED ENTITY: "+synset);
							 babel_senses=synset.getSenses(Language.EN);
							 System.out.println("BabelSense per "+synset+": "+babel_senses);

							 for ( BabelSense sense:babel_senses )
							 {
								 if ( sense.toString().startsWith("WIKI:EN:") ) {
									 voce_wikipedia_trovata=true;
									 categoria=sense.toString();
			                         break;
								 }
							 }
						 } else if ( synset.getSynsetType()!=BabelSynsetType.NAMED_ENTITY )
						 {
							 System.out.println("TROVATA UNA ENTITY: "+synset);
							 babel_senses=synset.getSenses(Language.EN);
							 System.out.println("BabelSense per "+synset+": "+babel_senses);

							 for ( BabelSense sense:babel_senses )
							 {
								 if ( sense.toString().startsWith("WIKI:EN:") ) {
									 voce_wikipedia_trovata=true;
									 categoria=sense.toString();
			                         break;
								 }
							 }
						 }
						 if( categoria.equals("") ) {
							 categoria=synset.toString(Language.EN);
							 System.err.println("ERRORE: Il synset non contiene riferimenti a Wikipedia");
							 voce_wikipedia_trovata=false;
						 }
						 // SE HO TROVATO UN TERMINE IN WIKIPEDIA, ESTRAGGO LE CATEGORIE CON synset.getCategories()
						 if( voce_wikipedia_trovata && categoria.startsWith("WIKI:EN:") )
						 {
							 if( !categoria.contains(",") ) {
			                        categoria=categoria.replaceAll("WIKI:EN:","");
			                        categoria=categoria.replaceAll("_"," ");
							 }
							 this.named_entity_wikipedia.add(categoria);
				             System.out.println("PAGINA WIKIPEDIA: "+categoria);

				             List<BabelCategory> bc = synset.getCategories(Language.EN);
				             if ( bc.isEmpty() ) {
				            	 System.err.println("Categorie non trovate per "+categoria+", salvataggio annullato");
				             }
				             for ( BabelCategory cg:bc ) {
				            	 System.out.println("Categoria di "+categoria+": "+cg.getCategory());
				            	 this.categorie_di_wikipedia_finale.add( cg.getCategory() );
				             }
				             System.out.println("CATEGORY trovate: "+categorie_di_wikipedia_finale);
						 }
						 voce_wikipedia_trovata=false;
					 }
				}
			}
		}
		// TUTTE LE CATEGORIE TROVATE
		categorie_da_wikipedia.addAll( categorie_di_wikipedia_finale );
	}


	protected boolean confrontaEntityNERvsBabelfy(String termine, List<String> entityNER) {
		boolean isContained = false;
		for (String NER : entityNER) {
			if (NER.equalsIgnoreCase(termine)) {
				isContained = true;
				return isContained;
			}
		}
		return isContained;
	}


	/**
	 * Get the chain of parents from the given synset to the top of the wordnet
	 * hierarchy.
	 * @return the chain of parents.
	 * @throws JWNLException
	 * @throws FileNotFoundException
	 */
	static public List<net.didion.jwnl.data.Synset> hypernymChain(net.didion.jwnl.data.Synset synset,
			Vector<String> iperonimi) throws FileNotFoundException, JWNLException {
		List<net.didion.jwnl.data.Synset> history = new ArrayList<net.didion.jwnl.data.Synset>();
		history.add(synset);
		return hypernymChain(synset, history, iperonimi);
	}


	/**
	 * Get the chain of parents from the given synset to the top of the wordnet
	 * hierarchy. Checks for loops (they exist!).
	 * @param synset
	 *            Child synset of which we want the parent chain.
	 * @param history
	 *            The list of synsets we've already traversed.
	 * @return The hypernym chain.
	 */
	static public List<net.didion.jwnl.data.Synset> hypernymChain(net.didion.jwnl.data.Synset synset,
			List<net.didion.jwnl.data.Synset> history, Vector<String> iperonimi) {
		List<net.didion.jwnl.data.Synset> chain = new ArrayList<net.didion.jwnl.data.Synset>();

		net.didion.jwnl.data.Pointer[] links = synset.getPointers();
		if (links != null) {
			for (net.didion.jwnl.data.Pointer link : links) {
				// HYPERNYM is the type of link in WordNet. However, they also
				// have an "instance hypernym"
				// which does not have a PointerType type in their API, yet
				// appears in their database.
				// This is a hack that checks the key "@i" is an instance, "@"
				// is standard hypernym.
				// if( link.getType() == PointerType.HYPERNYM ) {
				if (link.getType() != null && link.getType().getKey().charAt(0) == '@') {
					try {
						net.didion.jwnl.data.Synset target = link.getTargetSynset();
						net.didion.jwnl.data.Word[] hyperonyms = link.getTargetSynset().getWords();
						for (Word hyper : hyperonyms) {
							String hyperonym = hyper.getLemma().toLowerCase();
							iperonimi.addElement(hyperonym);
						}
						if (!history.contains(target)) {
							history.add(target);
							chain.add(target);
							List<net.didion.jwnl.data.Synset> upperChain = hypernymChain(target, history, iperonimi);
							if (upperChain != null) {
								chain.addAll(upperChain);
							}
							// ** There's only one parent per synset, right?
							return chain;
						} else
							System.out.println("Wordnet found loop at " + synset + "\nhistory=" + history);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		return null;
	}


	/**
	 * Data una List<String> di elementi, questo metodo si occupa di contare gli
	 * elementi e di restituire l'elemento più frequente. Ogni termine ed il suo
	 * valore di frequenza viene salvato temporaneamente in una HashMap<String,
	 * Integer>.
	 * @param elements
	 *            List<String> di elementi
	 */
	protected void contaIperonimiAndGetMax(List<String> elements)
	{
		ArrayList<String> words = new ArrayList<>();
	    Map<String,Integer> hypernymMap = new HashMap<>();
	    Entry<String,Integer> maxEntry = null;

	    for (int i = 0 ; i < elements.size(); i++)
	    {
			String elem = elements.get(i);
			words.add(elem);
		}

	    for (String w : words) {
	        Integer n = hypernymMap.get(w);
	        n = (n == null) ? 1 : ++n;
	        hypernymMap.put(w, n);
	    }


	    this.sortedMap=ordinaHashMapReverse(hypernymMap);

		for (Entry<String,Integer> entry : sortedMap.entrySet())
		{

		    if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
		        maxEntry = entry;

		    }
		}

		stampaIperonimiMap(sortedMap);


		// VENGONO CONTATI GLI IPERONIMI E SALVATI QUELLI PIU' RICORRENTI
		int maxValueInMap=(Collections.max(this.sortedMap.values()));  // This will return max value in the Hashmap
		System.out.println("\nGLI IPERONIMI PIU' FREQUENTI:");
        for (Entry<String, Integer> entry : sortedMap.entrySet()) {  // Iterate through hashmap
            if (entry.getValue()==maxValueInMap)
            {
                System.out.println(entry.getKey());     // Print the key with max value
                iperonimiFrequenti.add(entry.getKey().toLowerCase());
                hash_ner.add(entry.getKey());
            }
        }

	}


	static public void stampaIperonimiMap(Map<String, Integer> maps) {
		int value;
		System.out.println("Tutti gli iperonimi di WordNet 3.0 trovati:");
        for (Iterator<Entry<String, Integer>> iterator = maps.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Integer> entry = iterator.next();
			value = (int) entry.getValue();

            if (value > 1)
                System.out.println("CHIAVE : " + entry.getKey() + " VALORE : " + value);

		}
    }


	/**
	 * Ordinamento in senso decrescente degli elementi di una HashMap.
	 * @param mappa
	 *            la HashMap da ordinare
	 * @return la mappa ordinata in senso decrescente
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String,Integer> ordinaHashMapReverse(Map<String, Integer> mappa)
	{
		List list = new LinkedList(mappa.entrySet());
		Collections.sort(list, (o1, o2) -> ((Comparable) ((Map.Entry) (o2)).getValue())
		        .compareTo(((Map.Entry) (o1)).getValue()));

		Map<String,Integer> sortedMap = new LinkedHashMap<String,Integer>();
		for (Iterator<?> it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put((String)entry.getKey(), (Integer)entry.getValue());
		}

		return sortedMap;
	}


	/**
	 * @return the sortedMap
	 */
	public Map<String, Integer> getSortedMap() {
		return sortedMap;
	}


	/**
	 * @param sortedMap the sortedMap to set
	 */
	public void setSortedMap(Map<String, Integer> sortedMap) {
		this.sortedMap = sortedMap;
	}


	/**
	 * @return the iperonimiFrequenti
	 */
	public List<String> getIperonimiFrequenti() {
		return iperonimiFrequenti;
	}


	/**
	 * @param iperonimiFrequenti the iperonimiFrequenti to set
	 */
	public void setIperonimiFrequenti(List<String> iperonimiFrequenti) {
		this.iperonimiFrequenti = iperonimiFrequenti;
	}


	/**
	 * @return the bn
	 */
	public BabelNet getBn() {
		return bn;
	}


	/**
	 * @param bn the bn to set
	 */
	public void setBn(BabelNet bn) {
		this.bn = bn;
	}



}
