/*
 * Università degli Studi di Napoli Parthenope
 */
package ita.parthenope.twitternlp.babelnet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;





/**
 * Rimozione delle stopwords dalle conversazioni Twitter.
 * @author onofrio
 *
 */
public class StopWordRemover {
	
	private Set<String> stopwords;

	
	/**
	 * Costruttore di classe, permette di definire la lista delle stopword da
	 * caricare.
	 * @param nomeFile
	 *            nome del file che contiene la lista di stopword.
	 */
	public StopWordRemover(final String nomeFile) {
		super();
		this.stopwords = new HashSet<String>();
		createHashSet(nomeFile);
	}

	
	//CARICA LA LISTA DELLE STOPWORD IN UN SET, EVITA ANCHE EVENTUALI DOPPIONI.
	private void createHashSet(String nomeFile) {
		try {
			BufferedReader fileRead = new BufferedReader(new FileReader(nomeFile));
			String item;
			while (fileRead.ready()) {
				item = fileRead.readLine();
				item = item.toLowerCase().trim();
				stopwords.add(item);
			}
			
			fileRead.close();

		}
		catch (IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
		}
	}

	
	/**
	 * Elimina dalla stringa presa in input le stopword, segni di punteggiatura,
	 * spazi in eccesso.
	 * @param token
	 *            stringa da cui eliminare le stopword, la punteggiatura, spazi
	 *            in eccesso.
	 * @return out stringa elaborata.
	 */
	public String stopWordsClear(String token)
	{
		String out="";
		token = token.replaceAll("(?!['’])\\p{Punct}", " ");
		String[] words = token.split("\\s");
		for (int i = 0; i<words.length; i++) {
			String wordCompare = words[i].trim().toLowerCase();
			if(this.stopwords.contains(wordCompare)) {
				words[i] = "";
			}
			if (!(words[i].equalsIgnoreCase(""))) {
				out = out.concat(words[i]+" ");
			}
		}
		int finalws = out.length()-1;
		if(!out.isEmpty())
			out = out.substring(0, finalws);
		
		return !out.isEmpty() ? out : "";
	}
	
	
	/**
	 * Rende maiuscola la prima lettera di una stringa
	 * @param testo_lower
	 *            stringa da capitalizzare
	 * @return la stringa capitalizzata
	 */
	public String capitalizeFirstLetter(String testo_lower) {
	    if (testo_lower == null || testo_lower.length() == 0) {
	        return testo_lower;
	    }
	    
	    return testo_lower.substring(0, 1).toUpperCase() + testo_lower.substring(1);
	}


}