/*
 * Università degli Studi di Napoli Parthenope
 */
package ita.parthenope.twitternlp.babelnet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.jlt.util.Language;
import ita.parthenope.twitternlp.utils.Utility;




/**
 * Viene effettuata la Word Sense Disambiguation attraverso Babelfy API.
 * @author onofrio
 *
 */
public class Disambiguation extends BabelfyParameter {
	
	Babelfy bfy;

	//Annotazione Semantica disambiguata
	private HashMap<Integer, ArrayList<SemanticAnnotation>> bfyAnnotations;
	private List<String> entity_babelfy;
	
	//Risultato
	private StringBuilder bfyRisultato;
	private AtomicInteger key;
	private Utility utility;
	
	
	public Disambiguation() {
		super();
		setBfyAnnotations(new HashMap<Integer, ArrayList<SemanticAnnotation>>());
		setKey(new AtomicInteger(new Integer(0)));
		this.entity_babelfy = new ArrayList<String>();
		this.utility = new Utility();
		this.bfyRisultato = new StringBuilder();
	}
	
	
	/**
	 * Disambiguazione della parola chiave di ricerca.
	 * @param parola
	 *            la parola chiave
	 * @return l'identificativo
	 */
	public String disambiguaParolaChiave(String parola)
	{
		String identificativo="";
		List<SemanticAnnotation> annotations=new ArrayList<SemanticAnnotation>();
		
		try {
			bp = setParametriBabelfy();
			this.bfy = new Babelfy(bp);
			System.out.println("Babelfy getInstance OK\n");
		} catch(Exception E) {
			System.err.println("ERRORE Babelfy getInstance\n");
			E.printStackTrace();
		}

		if (parola.equals("Trump"))
		{
			/*
			 * Disambiguato come Donald Trump (e.g. Trump è anche un gioco di carte)
			 */
			parola = "donald_trump";
			annotations = bfy.babelfy(parola, Language.EN);
		} else 
			if (parola.equals("ISIS")) {
				/*
				 * Disambiguato come Stato Islamico (e.g. ISIS è anche una divinita' egizia)
				 */
				parola = "islamic_state";
				annotations = bfy.babelfy(parola, Language.EN);
		} else {
			annotations = bfy.babelfy(parola, Language.EN);
		}
		
		//bfyAnnotations is the result of Babelfy.babelfy() call
		for (SemanticAnnotation annotation : annotations)
		{
		    //splitting the input text using the CharOffsetFragment start and end anchors
		    String frammento = parola.substring(annotation.getCharOffsetFragment().getStart(),
		        annotation.getCharOffsetFragment().getEnd() + 1);
		    System.out.println(frammento + "\t" + annotation.getBabelSynsetID());
		    System.out.println("\t" + annotation.getSource());
		    
		    identificativo = annotation.getBabelSynsetID();
		}
		
		return identificativo;
	}
	
	
	/**
	 * Babelfy disambigua la conversazione di Twitter restituendo le named
	 * entity, e si occupa anche di un eventuale entity linking. Il risultato
	 * della disambiguazione è scritto in un file di testo.
	 * @param conversazione_twitter
	 *            Stringa contenente la conversazione presa da Twitter
	 * @throws IOException
	 * @throws RuntimeException
	 */
	public void disambiguazioneConBabelfy(final String conversazione_twitter) throws IOException, RuntimeException {
		List<String> conversazione = new ArrayList<String>();
		conversazione.add(conversazione_twitter);
		
		try {
			bp = setParametriBabelfy();
			this.bfy = new Babelfy(bp);
			System.out.println("Babelfy getInstance OK\n");
		} catch(Exception E) {
			System.err.println("ERRORE Babelfy getInstance\n");
			E.printStackTrace();
		}
		
		// QUESTO FILE CONTERRA' IL RISULTATO DELLA DISAMBIGUAZIONE
		File file = new File("resources/testo_disambiguato.txt");
		if (!file.exists()) 
		{
			boolean leak = file.createNewFile();
			if (!leak) {
				System.err.println("ERRORE: Impossibile aprire o creare il file");
			}
		}

		BufferedWriter output = utility.apriFile("resources/testo_disambiguato.txt", "output", file.isFile());
		try {
			for (String object : conversazione) {
				
				ArrayList<SemanticAnnotation> mapValue = new ArrayList<SemanticAnnotation>();
				// bfyAnnotations è il risultato della chiamata a Babelfy.babelfy()
				mapValue.addAll(bfy.babelfy(object, Language.EN));
				this.bfyAnnotations.put(key.incrementAndGet(), mapValue);
			}
		} catch (RuntimeException re) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, re);
		}
		
		Iterator<Integer> it = bfyAnnotations.keySet().iterator();
		while (it.hasNext()) 
		{
			for (String oggetto : conversazione) {
        		Integer keys = it.next();
        		ArrayList<SemanticAnnotation> values = bfyAnnotations.get(keys);
        		for (SemanticAnnotation annotation : values) 
        		{
	        		// DIVIDO IL TESTO DI INPUT TRAMITE LE ANCORE DI INIZIO E FINE DI CharOffsetFragment
					String fragment = oggetto.substring(annotation.getCharOffsetFragment().getStart(), annotation.getCharOffsetFragment().getEnd()+1);
					try {
						stampaEntityBabelfy(fragment, annotation);
						entity_babelfy.add(fragment);
						
					} catch (Exception E) {
						E.printStackTrace();
					}
					// SCRITTURA NEL FILE
					String built_rsult = costruisciRisultatoBabelfy(fragment, annotation);
					output.write(built_rsult + System.getProperty("line.separator"));
					output.flush();
	        	}
        	}
        }
		output.close();
	}
	
	
	private StringBuilder cleanStringBuilder(StringBuilder sb) {
		return sb = new StringBuilder();
	}
	
	
	static private <E> void stampaEntityBabelfy(E frag, SemanticAnnotation ann) {
		if (frag != null && ann != null) {
			System.out.println( "FRAGMENT - SYNSET ID:" );
			System.out.println(frag + "\t" + ann.getBabelSynsetID());
			System.out.println( "\tBABELNET URL:" );
			System.out.println("\t" + ann.getBabelNetURL());
			System.out.println( "\tDBPEDIA URL:" );
			System.out.println("\t" + ann.getDBpediaURL());
			System.out.println( "\tSORGENTE:" );
			System.out.println("\t" + ann.getSource());
		} 
		else {
		
			System.err.println( "ATTENZIONE: Il fragment e la SemanticAnnotation sono entrambi nulli" );
			System.exit(1);
		}
		
	}
	
	
	/**
	 * Costruisce il risultato ottenuto con Babelfy unendo il frammento
	 * disambiguato con la sua annotazione semantica corrispondente.
	 * @param frammenti
	 *            annotazione semantica disambiguata
	 * @param annotazione
	 *            annotazione
	 * @return la stringa costruita
	 */
	public <E> String costruisciRisultatoBabelfy(E frammenti, E annotazione) 
	{
		String ris="";
		try {
			ris=bfyRisultato.append(((SemanticAnnotation) annotazione).getBabelSynsetID()).append("\t").append(frammenti).toString().trim();
			this.bfyRisultato = cleanStringBuilder(bfyRisultato);
		} 
		catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
		}
		
		return ris.equals("") ? "" : ris;
	}

	
	/**
	 * @return the bfy
	 */
	public Babelfy getBfy() {
		return bfy;
	}

	
	/**
	 * @param bfy the bfy to set
	 */
	public void setBfy(Babelfy bfy) {
		this.bfy = bfy;
	}

	
	/**
	 * @return the bfyAnnotations
	 */
	public HashMap<Integer, ArrayList<SemanticAnnotation>> getBfyAnnotations() {
		return bfyAnnotations;
	}

	
	/**
	 * @param bfyAnnotations the bfyAnnotations to set
	 */
	public void setBfyAnnotations(HashMap<Integer, ArrayList<SemanticAnnotation>> bfyAnnotations) {
		this.bfyAnnotations = bfyAnnotations;
	}

	
	/**
	 * @return the key
	 */
	public AtomicInteger getKey() {
		return key;
	}

	
	/**
	 * @param key the key to set
	 */
	public void setKey(AtomicInteger key) {
		this.key = key;
	}

	
	/**
	 * @return the bfyRisultato
	 */
	public StringBuilder getBfyRisultato() {
		return bfyRisultato;
	}

	
	/**
	 * @param bfyRisultato the bfyRisultato to set
	 */
	public void setBfyRisultato(StringBuilder bfyRisultato) {
		this.bfyRisultato = bfyRisultato;
	}
	

	
}

