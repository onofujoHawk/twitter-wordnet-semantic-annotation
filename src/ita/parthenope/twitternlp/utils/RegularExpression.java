/*
 * Università degli Studi di Napoli Parthenope
 */
package ita.parthenope.twitternlp.utils;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;





/**
 * Set di espressioni regolari utilizzate nel codice.
 * @author onofrio
 *
 */
public class RegularExpression {
	
	private String regex;

	
	public RegularExpression() {
		this.regex = new String();
	}

	
	/**
	 * Rimozione di link ed url dal testo del tweet.
	 * @param strUrls
	 *            la string contenente urls
	 * @return la stringa purificata da eventuali url
	 */
	public String cancellaUrls(String strUrls) {
		regex = "https?://\\S+\\s?";
        return strUrls = strUrls.replaceAll(regex, " ");
    }
	
	
	/**
	 * Purificazione della conversazione, rimuovendo gli hashtags presenti.
	 * @param strHash
	 *            la stringa di testo contenente gli hashtag
	 * @return la stringa purificata da hashtags
	 */
	public String cancellaHashtags(String strHash)
	{
		if (strHash.contains("#")) {

			regex = "#[A-Za-z0-9_]+";
			return strHash.replaceAll(regex, " ");
			
		} else {
			
			return strHash;
		}
	}
	
	
	/**
	 * Rimozione delle emoticons, se presenti, nella conversazione estratta da
	 * Twitter attraverso request alle Streaming API.
	 * @param strEmotics
	 *            è la conversazione da depurare
	 * @return la conversazione depurata
	 */
	public String cancellaEmoticons(String strEmotics) 
	{
		if (EmojiManager.isEmoji(strEmotics)) 
		{
			strEmotics = EmojiParser.removeAllEmojis(strEmotics);
			return strEmotics;
		}
		return strEmotics;
	}
	
	
	public String cancellaDatetimes(String strDate) {
		regex = "(19|20)\\d\\d([-\\/.])([1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01]|\\d{1}) \\d{2}:\\d{2} (NZST|UTC|CEST|CDT|AST)";
		return strDate = strDate.replaceAll(regex, " ");
	}
	
	
	/**
	 * Rimozione di tags dalla conversazione.
	 * @param tag
	 *            la stringa contenente tag
	 * @return la stringa purificata
	 */
	public String cancellaTags(String tag) 
	{
		if (tag.contains("@")) {
			regex = "@[A-Za-z0-9_-]+";
			return tag = tag.replaceAll(regex, " ");
		} else {
			
			return tag;
		}

	}
	
	
	public String cancellaNumericValues(String strNumeric) {
		return strNumeric = strNumeric.replaceAll("\\d", " ");
	}
	
	
	public String cancellaBrackets(String strBrackets) {
		return strBrackets = strBrackets.replaceAll("\\[", " ").replaceAll("\\]"," ");
    }

	
	public String cancellaSeparators(String strSeparators) {
		return strSeparators = strSeparators.replace("\n", " ").replace("\r", " ");
	}
	
	
	/**
	 * Rimozione dalla conversazione di Emoji e di simboli.
	 * @param content
	 *            la conversazione da depurare
	 * @return la conversazione depurata
	 */
	public String removeEmojiAndSymbolFromString(String content) 
	{
		String utf8tweet = "";
	    try {
	        byte[] utf8Bytes = content.getBytes(
	            "UTF-8");
	 
	        utf8tweet = new String(
	            utf8Bytes, "UTF-8");
	    } catch (
	        UnsupportedEncodingException e
	    ) {
	        e.printStackTrace();
	    }
	    Pattern unicodeOutliers =
	        Pattern.compile(
	            "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
	            Pattern.UNICODE_CASE |
	            Pattern.CANON_EQ |
	            Pattern.CASE_INSENSITIVE
	        );
	    Matcher unicodeOutlierMatcher =
	        unicodeOutliers.matcher(
	            utf8tweet);
	 
	    utf8tweet =
	        unicodeOutlierMatcher.replaceAll(
	            " ");
	    
	    return utf8tweet;
	}
	
	
	public String cancellaDottedNumbers(String strDotted) {
		regex = "[0-9][.]|[0-9][0-9][.]";
		return strDotted = strDotted.replaceAll(regex, " ");
	}
	
	
	public String cancellaDoppiSpazi(String strDs) {
		regex = "\\s+";
		return strDs=strDs.trim().replaceAll(regex, " ");
	}
	
	
	/**
	 * Effettua la purificazione della conversazione Twitter.
	 * @param LINE
	 *            la stringa da purificare
	 * @return la stringa purificata
	 */
	public String purify(String LINE) 
	{
		if (LINE.contains(" s ") || LINE.contains(" S ")) 
		{
			String PATTERN=" s ";
			LINE=LINE.replaceAll(PATTERN, " ").replaceAll(PATTERN.toUpperCase(), " ");
		}
		if (LINE.contains(" t ") || LINE.contains(" T ")) 
		{
			String PATTERN=" t ";
			LINE=LINE.replaceAll(PATTERN, " ").replaceAll(PATTERN.toUpperCase(), " ");
		}
		if (LINE.contains(" l ") || LINE.contains(" L ")) 
		{
			String PATTERN=" l ";
			LINE=LINE.replaceAll(PATTERN, " ").replaceAll(PATTERN.toUpperCase(), " ");
		}
		if (LINE.contains(" ll ") || LINE.contains(" LL ")) 
		{
			String PATTERN=" ll ";
			LINE=LINE.replaceAll(PATTERN, " ").replaceAll(PATTERN.toUpperCase(), " ");
		}
		if (LINE.contains("'em") || LINE.contains("'EM")) 
		{
			String PATTERN="'em";
			LINE=LINE.replaceAll(PATTERN, " ").replaceAll(PATTERN.toUpperCase(), " ");
		}
		if (LINE.contains("’em") || LINE.contains("’EM")) 
		{
			String PATTERN="’em";
			LINE=LINE.replaceAll(PATTERN, " ").replaceAll(PATTERN.toUpperCase(), " ");
		}
		if (LINE.contains(" em ") || LINE.contains(" EM ")) 
		{
			String PATTERN=" em ";
			LINE=LINE.replaceAll(PATTERN, " ").replaceAll(PATTERN.toUpperCase(), " ");
		}
		if (LINE.contains("'s") || LINE.contains("'S")) 
		{
			String PATTERN="'s";
			LINE=LINE.replaceAll(PATTERN, " ").replaceAll(PATTERN.toUpperCase(), " ");
		}
		if (LINE.contains("’s") || LINE.contains("’S")) 
		{
			String PATTERN="’s";
			LINE=LINE.replaceAll(PATTERN, " ").replaceAll(PATTERN.toUpperCase(), " ");
		}
		if (LINE.contains("’t") || LINE.contains("’T"))
		{
			String PATTERN="’t";
			LINE=LINE.replaceAll(PATTERN, " ").replaceAll(PATTERN.toUpperCase(), " ");
		}
		if (LINE.contains("'t") || LINE.contains("'T")) 
		{
			String PATTERN="'t";
			LINE=LINE.replaceAll(PATTERN, " ").replaceAll(PATTERN.toUpperCase(), " ");
		}
		if (LINE.contains("'ll") || LINE.contains("'LL")) 
		{
			String PATTERN="'ll";
			LINE=LINE.replaceAll(PATTERN, " ").replaceAll(PATTERN.toUpperCase(), " ");
		}
		if (LINE.contains("’ll") || LINE.contains("’LL")) 
		{
			String PATTERN="'ll";
			LINE=LINE.replaceAll(PATTERN, " ").replaceAll(PATTERN.toUpperCase(), " ");
		}
		
		return LINE;
	}

	
	
}
