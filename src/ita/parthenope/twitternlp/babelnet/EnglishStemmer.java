/*
 * Università degli Studi di Napoli Parthenope
 */
package ita.parthenope.twitternlp.babelnet;

import org.tartarus.snowball.ext.englishStemmer;

public class EnglishStemmer implements Stemmer {

	@Override
	public String stem(String token) {
		englishStemmer stemmer = new englishStemmer();
	    stemmer.setCurrent(token);
	    boolean result = stemmer.stem();
	    if (!result) {
	    	return token;
	    }
	    return stemmer.getCurrent();
	}

}
