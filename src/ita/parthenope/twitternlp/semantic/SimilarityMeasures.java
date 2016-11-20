package ita.parthenope.twitternlp.semantic;

import java.util.logging.Level;
import java.util.logging.Logger;

import customedu.sussex.nlp.jws.AdaptedLesk;
import customedu.sussex.nlp.jws.JWS;
import customedu.sussex.nlp.jws.LeacockAndChodorow;
import customedu.sussex.nlp.jws.LeskGlossOverlaps;
import customedu.sussex.nlp.jws.Path;
import customedu.sussex.nlp.jws.WuAndPalmer;




/**
 * Misure di similarità tra concetti in WordNet.
 * @author onofrio
 *
 */
public abstract class SimilarityMeasures 
{
	
	protected customedu.sussex.nlp.jws.Lin lin;
	protected WuAndPalmer wup;
	protected customedu.sussex.nlp.jws.Resnik res;
	protected Path path;	
	protected LeacockAndChodorow lch;
	protected LeskGlossOverlaps lgo;
	protected AdaptedLesk lesk;
	
	protected static final String dir_diz ="C:/Program Files/WordNet";
	protected JWS jws;
	
	
	public SimilarityMeasures() 
	{
		super();
		try {
			//Percorso dizionario di WordNet 2.1
			jws=new JWS(dir_diz+"/2.1/", "2.1");
			lin=jws.getLin();
			wup=jws.getWuAndPalmer();
			path=jws.getPath();
			res=jws.getResnik();
			lgo=new LeskGlossOverlaps(jws.getDictionary());
			lesk=jws.getAdaptedLesk();
		} catch (Exception jws) 
		{
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, jws);
		}
		
	}
	
	
	/**
	 * Effettua il calcolo di similarita tra due parole attraverso la misura di
	 * Lin.
	 * @param W1
	 *            prima parola
	 * @param W2
	 *            seconda parola
	 * @param PartOfSpeech
	 *            pos della parola
	 * @return il valore di similarità
	 */
	protected Double Lin(String W1,String W2,String PartOfSpeech) {
		return lin.max(W1,W2,PartOfSpeech);
	}

	
	/**
	 * Effettua il calcolo di similarita tra due parole attraverso la misura di
	 * LeskGlossOverlap.
	 * @param G1
	 *            la prima glossa
	 * @param G2
	 *            la seconda glossa
	 * @return il valore di similarità
	 */
	protected Double LeskGlossOverlap(String G1,String G2) {
		lgo.useLemmatiser(false);
		lgo.useStopList(false);
		return lgo.overlap(G1,G2);
	}
	
	
	/**
	 * Effettua il calcolo di similarita tra due concetti attraverso la misura
	 * di Adapted Lesk specificando anche su quale senso si vuole agire.
	 * @param W1
	 *            prima parola
	 * @param Senso1
	 *            il senso della prima parola
	 * @param W2
	 *            seconda parola
	 * @param Senso2
	 *            il senso della seconda parola
	 * @param PartOfSpeech
	 *            pos delle due parole
	 * @return la misura di similarità
	 */
	protected Double AdaptedLesk(String W1,int Senso1,String W2,int Senso2,String PartOfSpeech) {
		lesk.useLemmatiser(false);
		lesk.useStopList(false);
		return lesk.lesk(W1,Senso1,W2,Senso2,PartOfSpeech);
	}
	
	
	/**
	 * Effettua il calcolo di similarita tra due concetti attraverso la misura
	 * di Adapted Lesk, versione customizzata per integrazione con BabelNet.
	 * @param W1
	 *            la prima parola
	 * @param W2
	 *            la parola chiave di ricerca
	 * @param G2
	 *            la glossa della seconda parola
	 * @param PartOfSpeech
	 *            la pos delle due parole
	 * @return la misura di similarità
	 */
	protected Double AdaptedLeskCustom(String W1,String W2,String G2,String PartOfSpeech) {
		lesk.useLemmatiser(false);
		lesk.useStopList(false);
		return lesk.overlap_gloss_custom(W1,W2,G2,PartOfSpeech);
	}
	
	
	/*
	 * Effettua il calcolo di similarita tra due concetti attraverso la misura
	 * di Adapted Lesk
	 */
	protected Double AdaptedLesk(String W1,String W2,String PartOfSpeech) {
		lesk.useLemmatiser(false);
		lesk.useStopList(false);
		return lesk.max(W1,W2,PartOfSpeech);
	}
	
	
	/*
	 * Effettua il calcolo di similarita tra due parole attraverso la misura
	 * di WuAndPalmer
	 */
	protected Double WuPalmer(String W1,String W2,String PartOfSpeech) {
		return wup.max(W1,W2,PartOfSpeech);
	}
	
	
	/*
	 * Effettua il calcolo di similarita tra due parole attraverso la misura
	 * di Resnik
	 */
	protected Double Resnik(String W1,String W2,String PartOfSpeech) {
		return res.max(W1,W2,PartOfSpeech);
	}
	
	
	/*
	 * Effettua il calcolo di similarita tra due parole attraverso la misura
	 * di LeacockAndChodorow
	 */
	protected Double LeacockAndChodorow(String W1,String W2,String PartOfSpeech) {
		return lch.max(W1,W2,PartOfSpeech);
	}
	
	
	/*
	 * Effettua il calcolo di similarita tra due parole attraverso la misura
	 * di Path
	 */
	protected Double Path(String W1,String W2,String PartOfSpeech) {
		return path.max(W1,W2,PartOfSpeech);
	}

}
