/*
 * Università degli Studi di Napoli Parthenope
 */
package ita.parthenope.twitternlp.ner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;




/**
 * Named Entity Recognition attraverso Stanford NER.
 * @author onofrio
 *
 */
public class StanfordNER {

	//Classificatore
	private String serializedClassifier2016; 
	private String serializedClassifier2014;
	private AbstractSequenceClassifier<CoreLabel> classifier2016;
	private AbstractSequenceClassifier<CoreLabel> classifier2014;
	//Conversazione
	private String conversazione;
	
	
	public StanfordNER(String conversazione) 
	{
		super();
		this.serializedClassifier2016="C:/Users/onofr/Desktop/workspace/Twitter-NLP-Thesis/classifiers2016/ner-eng-ie.crf-4-conll-distsim.ser.gz"; 
		this.serializedClassifier2014="C:/Users/onofr/Desktop/workspace/Twitter-NLP-Thesis/classifiers2014/english.conll.4class.distsim.crf.ser.gz";
		this.conversazione=conversazione;
	}
	
	
	public List<String> ner() throws ParserConfigurationException, SAXException, IOException, ClassCastException, ClassNotFoundException {
		classifier2016 = CRFClassifier.getClassifierNoExceptions(serializedClassifier2016);
		String risultato_ner = classifier2016.classifyToString(this.conversazione); 
		final String riconosciute = depuraNamedEntityRiconosciute(risultato_ner);
		ArrayList<String> ner_recognized=new ArrayList<String>();
		tokenizza(riconosciute, ner_recognized);
		return ner_recognized;
	}
	
	
	/**
	 * Named Entity Recognizer - Stanford University 2014
	 * @param entita_riconosciute
	 *            output contenete le named entity riconosciute
	 * @param hash
	 *            parsing dell'iperonimo estratto con WordNet
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ClassCastException
	 * @throws ClassNotFoundException
	 */
	public void ner(List<String> entita_riconosciute, ArrayList<String> hash) throws ParserConfigurationException, SAXException, IOException, ClassCastException, ClassNotFoundException
    {
		classifier2014 = CRFClassifier.getClassifier(serializedClassifier2014);
        List<Triple<String, Integer, Integer>> tripla_ner=classifier2014.classifyToCharacterOffsets(conversazione);
        boolean hash_ner = false;
        
        if ( tripla_ner != null | !tripla_ner.isEmpty() ) {
        	 for( Triple<String, Integer, Integer>cco:tripla_ner )
             {
                 System.out.println(cco.second+" "+cco.third);
                 String parse=conversazione.substring(cco.second,cco.third);
                 hash_ner=findHash(parse, hash);
                 if(hash_ner==false)
                	 entita_riconosciute.add(parse);
                 System.out.println("Entità: "+parse);
             }
        } else {
        	System.err.println("ERRORE: Nessuna named entity riconosciuta dal NER...");
        }
    }
	

	protected String depuraNamedEntityRiconosciute(final String testo) {
		Collection<String> parole_ner= new ArrayList<String>();
		Collection<String> parole_formattate = new ArrayList<String>();
		StringBuilder ner_depurato = new StringBuilder();
    	String formatter[] = testo.split(" ");

    	for (String stringa : formatter) 
    	{
    		if (stringa.contains("/ORGANIZATION")|stringa.contains("/LOCATION")|stringa.contains("/MISC")|stringa.contains("/PERSON")) 
    		{ 
    			String replaced = stringa.replaceAll("/ORGANIZATION", "");
    			replaced = replaced.replaceAll("/LOCATION", "");
    			replaced = replaced.replaceAll("/MISC", "");
    			replaced = replaced.replaceAll("/PERSON", "");
    			
    			parole_ner.add(replaced);
    		}
    	}
    	for(String parola : parole_ner) {
    		if (parola.contains("/O")) 
    		{
    			String risultato = parola.replaceAll("/O", "");
    			// ELIMINAZIONE DEGLI ULTIMI CARATTERI DI PUNTEGGIATURA DALLE ENTITY
    			risultato = risultato.replace(",", "");
    			risultato = risultato.replace(".", "");
    			risultato = risultato.replace("\'", "");
    			risultato = risultato.replace("#", "");
    			parole_formattate.add(risultato);
    		} else {
    			parole_formattate.add(parola);
    		}
    		
    	}
    	// VIENE COSTRUITA LA STRINGA FINALE
    	for (String word : parole_formattate) 
    		ner_depurato.append(word + " ");
    	return (!ner_depurato.toString().isEmpty()|ner_depurato.toString()!=null) ? ner_depurato.toString() : null;
	}
	
	
	/**
	 * Named Entity tokenizer.
	 * @param entita
	 *            entity corrente
	 * @param entita_riconosciute
	 *            lista con entità riconosciute
	 */
	public void tokenizza(final String entita, List<String> entita_riconosciute) 
	{
		StringTokenizer tokens = new StringTokenizer(entita);
		while (tokens.hasMoreElements()) {
			String ris = tokens.nextToken();
			entita_riconosciute.add(ris);
		}
		
	}
	
	
	public boolean findHash(final String parse, ArrayList<String> hash) 
    {
        boolean found=false;
        for (int i=0;i<hash.size();i++)
        {
          if (hash.get(i).equalsIgnoreCase(parse)) {
              found=true;
              break;
          }
        }
        return found;
    }
	
	
	/**
	 * Identify Name,organization location etc entities and return Map<List>
	 * @param text
	 *            -- data
	 * @param model
	 *            - Stanford model names out of the three models
	 * @param list
	 *            -- will contains the identified words
	 * @return map
	 */
	public LinkedHashMap <String,LinkedHashSet<String>> identifyNER(String text,String model,ArrayList<String> hash) throws ParserConfigurationException, SAXException, IOException, ClassCastException, ClassNotFoundException
	{
		 LinkedHashMap <String,LinkedHashSet<String>> map=new LinkedHashMap <String,LinkedHashSet<String>>();
		 String serializedClassifier=model;
		 boolean hash_ner=false;
		 CRFClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
		 List<List<CoreLabel>> classify = classifier.classify(text);
		 for (List<CoreLabel> coreLabels : classify) 
		 {
			 for (CoreLabel coreLabel : coreLabels)
			 {
				 String word = coreLabel.word();
				 String category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
				 hash_ner=findHash(word, hash);
				 if (!"O".equals(category)) 
				 {
					 if (map.containsKey(category) && hash_ner==false)
					 {
						 // Key is already their just insert in arraylist
						 map.get(category).add(word);
					 } else {
						 LinkedHashSet<String> temp=new LinkedHashSet<String>();
						 temp.add(word);
						 map.put(category,temp);
					 }
					 
					 System.out.println(word+":"+category);
				 }
			 }
		 }
		 return map;
	 }
	

}
