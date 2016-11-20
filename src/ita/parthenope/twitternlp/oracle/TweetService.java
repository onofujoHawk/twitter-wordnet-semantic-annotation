/*
 * Università degli Studi di Napoli Parthenope
 */
package ita.parthenope.twitternlp.oracle;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;

import ita.parthenope.twitternlp.babelnet.StopWordRemover;
import ita.parthenope.twitternlp.utils.Hashing;
import ita.parthenope.twitternlp.utils.RegularExpression;




/**
 * Oracle JDBC Service.
 * @author onofrio
 *
 */
public class TweetService implements TweetDAL {
	
	//Connessione
	private Connection connection;
	private PreparedStatement smt;
	private ResultSet st;
	private Properties props;
	private int nris;
	private int numhash;
	//Conversazione
	Map<Integer,String> hmap_conversazioni = new HashMap<Integer, String>();
	//Hashtags
	Vector<String> hashtags_estratti  = new Vector<String>();
	
	private Vector<String> hashtag_associati;
	private List<String> idtweet;
	//Filtraggio conversazione
	private String inizio_conversazione_depurata;
    private String replica_conversazione_depurata;
    private String iniziale;
    private String replica;
    private String estrazione_completa;
	
	private StopWordRemover wr;
	private RegularExpression regx;
	StringBuilder builder = new StringBuilder();
	
	
	public TweetService() {
		super();
		props=new Properties();
		numhash=0;
		nris=0;
		hashtag_associati = new Vector<String>();
		idtweet = new ArrayList<String>();
		wr = new StopWordRemover("resources/stopwords_en.txt");
		regx = new RegularExpression();
		
		inizio_conversazione_depurata="";
		replica_conversazione_depurata="";
		estrazione_completa="";
		iniziale="";
		replica="";
	}
	
	
	/**
	 * Settaggio delle Properties di configurazione del database Oracle.
	 * @return la configurazione letta
	 */
	public Properties configuraOracle() 
	{
		InputStream inputstream = this.getClass().getClassLoader().getResourceAsStream("oracle.properties");
		Properties p=new Properties();
		try {
			p.load(inputstream);
		} 
		catch (IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
		}
		
		return p;
	}
	
	
	public void connettiDatabaseOracle() throws ConfigurationException {
		this.props = configuraOracle();
		
		Locale.setDefault(Locale.ENGLISH);
	    try {
			Class.forName(props.getProperty("oracle.driverClassName"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	      
	    try {
	    	Hashing hash = new Hashing();
	    	String hashed_passw = hash.OraclePasswordDecryptor();
			connection=DriverManager.getConnection(props.getProperty("oracle.url"), props.getProperty("oracle.username"), hashed_passw);
			connection.setAutoCommit(true);
			System.out.println("Connessione ORACLE avvenuta");
		} catch (SQLException e) {
			System.err.println("Connessione ORACLE fallita");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Effettua una query di conteggio del numero totale di hashtags presenti nel database.
	 * @throw SQLException
	 * @throw ConfigurationException
	 */
	public void contaHashtag() throws SQLException, ConfigurationException {
		connettiDatabaseOracle();
        for (int i=1;i<=6;i++) 
        { 
            String sql="select count(distinct hashtag"+i+")as n1 from tweet";
            smt =connection.prepareStatement(sql);
            st = smt.executeQuery(sql);
            while (st.next()) {
                numhash+=st.getInt("n1");
            }
            st.close();
            smt.close();
        }
        
        System.out.println("IL NUMERO DI HASHTAG PRESENTI E': "+numhash);
        disconnettiDatabaseOracle();
    }
	
	
	public void salvaHashMap(Vector<String> mappa_reverse, int indice, HashMap<Integer,String> hp) throws SQLException, ConfigurationException 
	{
		connettiDatabaseOracle();
		String SQL="select id_utente,screen_name from utente";
		System.out.println(SQL);
	    smt=connection.prepareStatement(SQL);
	    st=smt.executeQuery(SQL);
	    
	    while (st.next()) 
	    {
	    	mappa_reverse.add(st.getString("id_utente"));
	    	mappa_reverse.add(st.getString("screen_name"));
	        hp.put(indice,st.getString("id_utente"));
	        indice++;
	        hp.put(indice,st.getString("screen_name"));
	        indice++;
	    }
	    st.close();
	    smt.close();
	    
	    disconnettiDatabaseOracle();
	}
	
	
	private List<String> estraiNumeroConversazioneParolaChiave(final String parola_chiave_in_esame) throws SQLException 
	{
		String SQL="select count(*) as n1,tw.id_tweet as n2 from tweet tw inner join tweet replica on tw.id_tweet=replica.in_reply_to_status_id_str where tw.parola_chiave=?  group by tw.id_tweet,tw.parola_chiave";
		smt =connection.prepareStatement(SQL);
        smt.setString(1, parola_chiave_in_esame);
        st=smt.executeQuery();
        List<String> idtweet=new ArrayList<String>();
        while (st.next()) {
        	//PER OGNI CONVERSAZIONE NRIS CONTERRÀ IL NUMERO DI RISPOSTE
        	nris=st.getInt("n1");
        	if (nris>5 && nris<10) // INTERVALLO DI DEFAULT (5,10) ESCLUSI GLI ESTREMI
        		idtweet.add(st.getString("n2")); //E IDTWEET IL NOME DELLA PRIMA
        }
        st.close();
        smt.close();
        System.out.println("Il numero di conversazioni per la parola chiave "+parola_chiave_in_esame+" è: "+idtweet.size());
        
        return idtweet;
	}
	
	
	/**
	 * Ha il compito di ricostruire una intera conversazione su Twitter
	 * attraverso query di lettura dell'incipit e delle singole risposte della
	 * conversazione.
	 * @param vector_parole_chiavi
	 *            la parola chiave di ricerca
	 * @param hashtags
	 *            hashtag inerenti la conversazione
	 * @param hashmap_conversazioni
	 *            una HashMap contenente l'incipit e le singole risposte
	 * @throws SQLException
	 * @throws ConfigurationException
	 */
	public String ricostruisciConversazioneTwitter(Vector<String> vector_parole_chiavi, Vector<String> hashtags, HashMap<Integer,String> hashmap_conversazioni) 
			throws SQLException, ConfigurationException 
	{
		connettiDatabaseOracle();
	    for (String parola_chiave_in_esame : vector_parole_chiavi) 
	    {
			System.out.println("PAROLA ESTRATTA: "+parola_chiave_in_esame);
			this.idtweet.addAll(estraiNumeroConversazioneParolaChiave(parola_chiave_in_esame));
			
			for (int i=0;i<idtweet.size() ;i++) {
            	System.out.println("ANALISI CONVERSAZIONE "+(i+1)+"/"+idtweet.size());
            	
            	/*
            	 * Lettura dal db degli incipit e delle risposte ai tweet.
            	 */
            	estraiTweetIniziale(i);
            }
        }
		disconnettiDatabaseOracle();
		if (!hmap_conversazioni.isEmpty()) 
		{
			hashmap_conversazioni.putAll(this.hmap_conversazioni);
			hashtags.addAll(this.hashtag_associati);
			
			// ESTRAZIONE DELLA CONVERSAZIONE COMPLETA
			inizio_conversazione_depurata=regx.cancellaDoppiSpazi(inizio_conversazione_depurata);
		    replica_conversazione_depurata=regx.cancellaDoppiSpazi(replica_conversazione_depurata);
			this.estrazione_completa=this.inizio_conversazione_depurata+" "+this.replica_conversazione_depurata;
			
			return estrazione_completa.trim();
		}
		return null;
	}
	
	
	/**
	 * Lettura dal database dell'incipit della conversazione attaverso query.
	 * @param indice
	 *            indice della conversazione
	 * @throws SQLException
	 */
	private void estraiTweetIniziale(int indice) throws SQLException 
	{
		String SQL="select text,parola_chiave from tweet where id_tweet="+idtweet.get(indice)+"";
		PreparedStatement stmt1 = null;
	    ResultSet rs1 = null;
        stmt1 =connection.prepareStatement(SQL);
        rs1 = stmt1.executeQuery(SQL);
        
        // ESTRAZIONE HASHTAG ASSOCIATI ALLA CONVERSAZIONE CORRENTE
        hashtag_associati.addAll(estraiHashtag(idtweet.get(indice), "incipit"));
        hashtags_estratti.addAll(estraiHashtag(idtweet.get(indice), ""));
        
        while (rs1.next()) 
        {
        	// RIMOZIONE DELLE STOPWORD E PULIZIA DELL'INCIPIT DELLE CONVERSAZIONI
        	System.out.println("INIZIO DELLA CONVERSAZIONE");
            System.out.println(rs1.getString("text"));

            iniziale=rs1.getString("text");
        	iniziale=regx.cancellaTags(iniziale);
        	iniziale=regx.cancellaUrls(iniziale);
        	iniziale=regx.cancellaEmoticons(iniziale);
        	iniziale=regx.removeEmojiAndSymbolFromString(iniziale);
        	iniziale=regx.cancellaHashtags(iniziale);
        	iniziale=iniziale.replaceAll("\"", "");
        	iniziale=iniziale.replaceAll("(&)\\p{L}+","");
        	iniziale=iniziale.replaceAll("(@|#|&)\\p{L}+","");
        	iniziale=iniziale.replaceAll("[\\s\\-()]"," ");
        	iniziale=iniziale.replaceAll("(“|”|\")", "");
        	iniziale=wr.stopWordsClear(iniziale);
        	iniziale=iniziale.replaceAll("(‘|’|\')", " ");
        	iniziale=iniziale.replaceAll("\'", " ");
        	
            inizio_conversazione_depurata=inizio_conversazione_depurata+" "+iniziale;
       	    inizio_conversazione_depurata=regx.purify(inizio_conversazione_depurata);
       	    iniziale=regx.purify(iniziale);
      	    System.out.println("Incipit depurato: "+iniziale);
      	    builder.append(iniziale).append(" ");
      	    this.iniziale = new String();
      	    
      	    /*
      	     * Estrae tutte le risposte per il dato incipit.
      	     */
            estraiConversazionePerTweet(indice);
            
            //FINE RECUPERO DELL'INTERA CONVERSAZIONE
            System.out.println("FINE DELLA CONVERSAZIONE");
    		System.out.println("\nESTRAZIONE FINALE: "+inizio_conversazione_depurata+" "+replica_conversazione_depurata);
    		
    		if (!this.hashtags_estratti.isEmpty())
    		{
    			System.out.println("HASHTAGS ESTRATTI ASSOCIATI ALLA CONVERSAZIONE: "+ hashtags_estratti.toString());
    			Vector<String> hashtag_hmaps = new Vector<String>(hashtags_estratti);
    			for (String hashtag : hashtag_hmaps) {
    				//Associo alla conversazione i suoi hashtag in uppercase per distinguerli
    				builder.append(hashtag.toUpperCase()).append(" ");
    			}
    			this.hmap_conversazioni.put(indice+1, builder.toString().trim());
    		}    		
            else {
            	this.hmap_conversazioni.put(indice+1, builder.toString().trim());
            }
    		hashtags_estratti.clear();
            this.builder = new StringBuilder();
            System.out.println();
        }
      
	}
	
	
	/**
	 * Vengono estratti gli hashtag associati alla conversazione Twitter corrente.
	 * @return vector contenente gli hashtag della risposta
	 * @throws SQLException
	 */
	public Vector<String> estraiHashtag(final String id,String modalita) throws SQLException {
        Vector<String> hashtag=new Vector<String>();
        String SQL="select hashtag1,hashtag2,hashtag3,hashtag4,hashtag5,hashtag6 from tweet where id_tweet="+id+"";
        smt=connection.prepareStatement(SQL);
        st=smt.executeQuery(SQL);
        
        while (st.next()) {
            if (st.getString("hashtag1")!=null) {
              
              hashtag.add(st.getString("hashtag1"));
            }
            if (st.getString("hashtag2")!=null) {
             
              hashtag.add(st.getString("hashtag2"));
            }
            if (st.getString("hashtag3")!=null) {
              
              hashtag.add(st.getString("hashtag3"));
            }
            if (st.getString("hashtag4")!=null) {
              
              hashtag.add(st.getString("hashtag4"));
            }
            if (st.getString("hashtag5")!=null) {
              
              hashtag.add(st.getString("hashtag5"));
            }
            if (st.getString("hashtag6")!=null) {
            	
              hashtag.add(st.getString("hashtag6"));
            }
         }
        
        if (modalita.equals("risposta") && !hashtag.isEmpty()) {
        	System.out.println("HASHTAG DELLA RISPOSTA ESTRATTI: " + hashtag.toString());
        }
        else if (modalita.equals("incipit") && !hashtag.isEmpty()) {
        	System.out.println("HASHTAG CONV. ORIGINALE ESTRATTI: "+ hashtag.toString());
        }
        
        st.close();
        smt.close();
        
        return hashtag;
    }
	
	
	/**
	 * Ha il compito di estrarre tutte le risposte per un dato incipit i. Passa
	 * poi alla depurazione della conversazione attraverso rimozione di
	 * stop-words.
	 * @param i
	 *            indice dell'incipit del tweet
	 * @throws SQLException
	 */
	private void estraiConversazionePerTweet(int i) throws SQLException 
	{
		PreparedStatement stmt2=null;
	    ResultSet rs2 = null;
        String SQL="select id_tweet,text from tweet where in_reply_to_status_id_str="+idtweet.get(i)+"";
        stmt2=connection.prepareStatement(SQL);
        rs2=stmt2.executeQuery(SQL);
        
        while (rs2.next()) 
        {
        	System.out.println("RISPOSTA IN ANALISI: "+rs2.getString("text"));
        	// VENGONO ESTRATTI GLI HASHTAG ASSOCIATI AD OGNI TWEET IN RISPOSTA AL TWEET ORIGINALE
            hashtag_associati.addAll(estraiHashtag(rs2.getString("id_tweet"), "risposta"));
            hashtags_estratti.addAll(estraiHashtag(rs2.getString("id_tweet"), ""));
            
            // RIMOZIONE DELLE STOPWORD E PULIZIA DELLE RISPOSTE PER OGNI CONVERSAZIONE
            replica=regx.cancellaUrls(rs2.getString("text").replaceAll("[\\s\\-()]"," "));
            replica=regx.cancellaEmoticons(replica);
            replica=regx.cancellaTags(replica);
            replica=regx.removeEmojiAndSymbolFromString(replica);
            replica=regx.cancellaHashtags(replica);
            replica=replica.replaceAll("(@|#|&)\\p{L}+","");
            replica=replica.replaceAll("(&)\\p{L}+","");
            replica=replica.replaceAll("\"", "");
            replica=replica.replaceAll("(“|”|\")", "");
            replica=wr.stopWordsClear(replica);
            replica=replica.replaceAll("(‘|’|\')", " ");
             
            replica_conversazione_depurata=replica_conversazione_depurata+" "+replica;
            replica_conversazione_depurata=regx.purify(replica_conversazione_depurata);
            replica=regx.purify(replica);
            System.out.println("Risposta depurata: "+replica);
            builder.append(replica).append(" ");
            this.replica = new String();
        }
        
	}
	
	
	public void disconnettiDatabaseOracle() {
		try {
            connection.close();
            System.out.println("Disconnessione ORACLE avvenuta");
        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            System.out.println("Disconnessione ORACLE fallita");
        }
	}


}
