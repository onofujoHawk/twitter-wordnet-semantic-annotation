/*
 * Università degli Studi di Napoli Parthenope
 */
package ita.parthenope.twitternlp.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import ita.parthenope.twitternlp.babelnet.StopWordRemover;





/**
 * Funzioni e procedure utili implementate.
 * @author onofrio
 *
 */
public class Utility {
	
	
	/**
	 * Rimozione dei duplicati da List<String>.
	 * @param listWithDups
	 *            è la List<String> con elementi duplicati
	 * @return la List<String> senza duplicati
	 */
	public List<String> cancellaDuplicati(List<String> listWithDups) {
		return new ArrayList<String>(new LinkedHashSet<String>(listWithDups));
	}
	
	
	@SafeVarargs
	public static <T> List<T> concatenaListe(List<T>... collections) {
		return Arrays.stream(collections).flatMap(Collection::stream).collect(Collectors.toList()); 
    }
	
	
	public boolean mkdir(final String dir) {
		return new File((String) dir).mkdir();
	}
	
	
	/**
	 * Effettua la scrittura bufferizzata sul file di output.
	 * @param writer
	 *            la scrittura bufferizzata sul file
	 * @param container
	 *            contiene gli elementi da scrivere
	 * @throws Exception
	 */
	public <E> void scrivi(BufferedWriter writer, List<E> container) throws Exception 
	{
		for (E elem : container) 
		{
        	writer.write((String) elem+System.getProperty("line.separator"));
        	writer.flush();
        }
	}
	
	
	/**
	 * Effettua la lettura bufferizzata del file.
	 * @param reader
	 *            lettura bufferizzata del file
	 * @param set
	 *            contiene gli elementi letti
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public <E> void leggi(BufferedReader reader, HashSet<E> set) throws Exception {
		E strLine = null;
		
		while ((strLine = (E) reader.readLine()) != null) {
			set.add(strLine);
		}
	}
	
	
	/**
	 * Apertura in modalità INPUT od OUTPUT di un file di testo.
	 * @param filename
	 *            il nome del file
	 * @param mode
	 *            la modalità di apertura del file
	 * @param isFile
	 *            è o non è un file
	 * @return il file aperto in lettura o scrittura
	 */
	@SuppressWarnings("unchecked")
	public <T> T apriFile(String filename, String mode, boolean isFile) 
	{
		if (isFile) 
		{
			if (filename != null) {
				if (mode.equals("input")) {
					FileInputStream fInstream = null;
					try {
						fInstream = new FileInputStream(filename);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
			        DataInputStream inStream = new DataInputStream(fInstream);
			        BufferedReader reader = null;
					try {
						reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
					} catch (UnsupportedEncodingException uex) {
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, uex);
					}
			        
			        return (T) reader;

				} else if (mode.equals("output")) {
					FileWriter outStream = null;
					try {
						outStream = new FileWriter(filename);
					} 
					catch (IOException e) {
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
					}
			    	BufferedWriter writer = new BufferedWriter(outStream);
			    	
			    	return (T) writer;
				}
			} 
			else {
				System.err.println("ERRORE: "+ filename +" NON TROVATO");
			}
		}
		
		return null;
    }
	
	
	public boolean cancellaElementiVuoti(List<String> voidElems) {
		return voidElems.removeAll(Arrays.asList(Collections.singleton(null), Collections.singleton("")));
	}
	
	
	public void cancellaKeyword(List<String> list, final String word) 
	{
		String capitalized=new StopWordRemover("resources/stopwords_en.txt").capitalizeFirstLetter(word.toLowerCase());
		list.removeAll(Collections.singleton(word));
 		list.removeAll(Collections.singleton(word.toLowerCase()));
 		list.removeAll(Collections.singleton(word.toUpperCase()));
 		list.removeAll(Collections.singleton(capitalized));
 		
	}
	
	
	// Altra tecnica per il conteggio degli iperonimi estratti
    public void getWords(String[] array, HashMap<String, Integer> hmaps) 
    {
    	for (int i = 0; i < array.length; i++) 
    	{
    		
    		String currentWord = array[i];
            Integer frequency = hmaps.get(currentWord); 
            if (frequency == null) 
            {
                frequency = 0; 
            }  
            
            hmaps.put(currentWord, frequency + 1); 
        }
    }
    
    
	/**
	 * Funzione di arrotondamento di un numero decimale.
	 * @param d
	 *            il numero da arrotondare
	 * @param p
	 *            il valore di arrotondamento, espresso in cifre dopo la virgola
	 * @return la funzione arrotondata
	 */
    public double arrotonda(double d, int p) {
		return Math.rint(d*Math.pow(10,p))/Math.pow(10,p);
	}
    
    
}
