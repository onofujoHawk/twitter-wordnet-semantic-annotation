/*
 * Università degli Studi di Napoli Parthenope
 */
package ita.parthenope.twitternlp.babelnet;

import it.uniroma1.lcl.babelfy.commons.BabelfyParameters;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters.DisambiguationConstraint;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters.MatchingType;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters.PosTaggingOptions;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters.ScoredCandidates;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters.SemanticAnnotationType;





/**
 * Setting dei parametri della disambiguazione per Babelfy API.
 * @author onofrio
 *
 */
public abstract class BabelfyParameter {
	
	protected BabelfyParameters bp;
	
	
	public BabelfyParameter() 
	{
		super();
		this.bp = new BabelfyParameters();
	}
	
	
	protected BabelfyParameters setParametriBabelfy() {
		/**
		 * Processo standard di POS tagging.
		 */
		bp.setPoStaggingOptions(PosTaggingOptions.STANDARD);
		
		/**
		 * Solo i match esatti vengono considerati per la disambiguazione.
		 */
		bp.setMatchingType(MatchingType.EXACT_MATCHING);
		
		/**
		 * Disambigua l'intero testo e ritorna le corrispondenti annotazioni.
		 */
		bp.setDisambiguationConstraint(DisambiguationConstraint.DISAMBIGUATE_ALL);
		
		/**
		 * Restituisce i candidati classificati come TOP per un frammento.
		 */
		bp.setScoredCandidates(ScoredCandidates.TOP);
		
		/**
		 * Disambigua tutto (SOSTANTIVI, VERBI, CONCETTI).
		 */
		bp.setAnnotationType(SemanticAnnotationType.ALL);
		
		
		return bp;
	}

	
	/**
	 * @return the bp
	 */
	public BabelfyParameters getBp() {
		return bp;
	}

	
	/**
	 * @param bp the bp to set
	 */
	public void setBp(BabelfyParameters bp) {
		this.bp = bp;
	}
	

}
