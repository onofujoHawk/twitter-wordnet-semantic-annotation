/*
 * Università degli Studi di Napoli Parthenope
 */
package ita.parthenope.twitternlp.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLException;

import org.apache.commons.configuration.ConfigurationException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ita.parthenope.twitternlp.oracle.TweetService;
import ita.parthenope.twitternlp.utils.Hashing;



/**
 * Crawler per lo streaming dei dati dalle API di Twitter.
 * @author onofrio
 *
 */
public class TweetCrawler extends TweetService {

    private int indice;
    private HashMap<Integer, String> hp;
    private Properties property;

    Vector<String> mappa_reverse;
    //OAuth 
    private OAuthService service;
	private Token accessToken;
	Hashing hash;
	//Parsing dei dati
    private String line;
    private JsonNode node;
    private Connection connection;
    private ArrayList<String> infohashtag;
    private PreparedStatement sntutente;
    private PreparedStatement sntweet;

    //Language detector
    private int indtw;
    private boolean fine;
    private String lang;
    private PreparedStatement sql_inserimento_tabella_hashtag;


    public TweetCrawler() {
    	super();
        hp=new HashMap<Integer, String>();
        infohashtag=new ArrayList<String>();
        indice=1;
        fine=false;

        mappa_reverse=new Vector<String>();
        hash = new Hashing();

    }


    /**
     * Configurazione delle proprietà della libreria Scribe.
     * @throws ConfigurationException
     */
    private void configuraScribe() throws ConfigurationException {
    	System.out.println("Richiesta POST inviata a Twitter Streaming API");
    	InputStream in = this.getClass().getClassLoader().getResourceAsStream("scribe.properties");
        this.property=new Properties();
		try {
			property.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Vector<String> scribeOAuth = hash.ScribeOAuthDecryptor();
        service = new ServiceBuilder()
                    .provider(TwitterApi.class)
                    .apiKey(this.property.getProperty("oauth.apiKey"))
                    .apiSecret(scribeOAuth.get(0))
                    .build();

        accessToken = new Token(this.property.getProperty("oauth.accessToken"), scribeOAuth.get(1));
    }


	/**
	 * Crawling dei tweet dalle Streaming API mediante Scribe, salvataggio dei
	 * dati nel database attraverso query con Java JDBC e controllo della lingua
	 * dei tweet con Language Detector.
	 * @param parola_chiave
	 *            parola chiave di ricerca tweet
	 * @throws IOException
	 * @throws SQLException
	 * @throws ParseException
	 * @throws LangDetectException
	 * @throws InterruptedException
	 * @throws ConfigurationException
	 */
    public void crawl(final String parola_chiave) throws IOException, SQLException, ParseException, LangDetectException, InterruptedException, ConfigurationException
    {
    	String url="https://stream.twitter.com/1.1/statuses/filter.json?track="+parola_chiave+"&language=en";
    	OAuthRequest request = new OAuthRequest(Verb.POST, url);
        request.addHeader("version", "HTTP/1.1");
        request.addHeader("host", "stream.twitter.com");
        request.setConnectionKeepAlive(true);
        request.addHeader("user-agent", "Twitter Stream Reader");
        configuraScribe();
        service.signRequest(accessToken, request);
        Response response = request.send();


        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getStream(),"UTF-8"));

        System.out.println("ASCOLTO TWEET DA ANALIZZARE... ANALISI PER "+Integer.valueOf(property.getProperty("oauth.minutiAnalisi"))+" MINUTI");
        int cont=0;

        long trascorso=0;
        long inizio=System.currentTimeMillis();
        while(trascorso < Integer.valueOf(property.getProperty("oauth.minutiAnalisi")) * 60 * 1000)
        {
        	System.out.print("\rPROCESSATO TWEET NUMERO: "+(cont+1)+"\r");
            System.out.flush();
            try {

                line=reader.readLine();
                if(!line.isEmpty()) {
                    parsing();
                    cont++;
                }
            } catch(SSLException ex)
            {
                System.out.println("CONNESSIONE PERSA... PROVO A RISTABILIRLA TRA "+Integer.valueOf(property.getProperty("oauth.secondiAttesa"))+" SECONDI");
                Thread.sleep(Integer.valueOf(property.getProperty("oauth.secondiAttesa")) * 1000);
                request = new OAuthRequest(Verb.POST, url);
                request.addHeader("version", "HTTP/1.1");
                request.addHeader("host", "stream.twitter.com");
                request.setConnectionKeepAlive(true);
                request.addHeader("user-agent", "Twitter Stream Reader");
                service.signRequest(accessToken, request);
                response = request.send();

                reader = new BufferedReader(new InputStreamReader(response.getStream(),"UTF-8"));
            }

            trascorso = (new Date()).getTime() - inizio;
        }
    }


	public void riempiHashMap() throws SQLException, ConfigurationException {
		salvaHashMap(mappa_reverse, indice, hp);
	}


	public void parsing() throws IOException, SQLException, ParseException, LangDetectException {
	    boolean esiste=false;
	    fine=false;
	    indtw=1;
	    infohashtag.clear();
	    Iterator<String> fieldNames;

	    ObjectMapper mapper = new ObjectMapper();
	    node = mapper.readTree(line);
	    fieldNames = node.fieldNames();

	    controllaLingua();

	    // SE LA LINGUA DEL TWEET E' ENGLISH
	    if(lang.equals("en")) {
	        while (fieldNames.hasNext()) {
	            String fieldName = fieldNames.next();
	            if(fieldName.equals("user")) {
	                parsingUtente();
	            }
	            if(fieldName.equals("retweeted_status")) {
	                parsingRetweet();
	            }
	            else if(fieldName.equals("id_str")||fieldName.equals("text")|| fieldName.equals("lang") || fieldName.equals("created_at")) {
	                if(fieldName.equals("id_str")) {

	                    if(!hp.containsValue(node.get(fieldName).asText())) {
	                        mappa_reverse.add(node.get(fieldName).asText());
	                        hp.put(indice,node.get(fieldName).asText());
	                        sntweet.setObject(indtw,node.get(fieldName).asText());
	                        indtw++;
	                        indice++;
	                    }
	                    else {
	                    	esiste=true;
	                        indtw++;
	                    }
	                }
	                else {
	                	sntweet.setObject(indtw,node.get(fieldName).asText());
	                	indtw++;
	                }
	            }

	            else if(fieldName.equals("entities")) {
	                parsingEntita();
	            }
	            else if(fieldName.equals("in_reply_to_user_id_str"))
	            {
	            	if(node.get(fieldName).asText().equals("null")) {
	            		sntweet.setObject(indtw,null);
	                    indtw++;
	                } else {
	                    sntweet.setObject(indtw,node.get(fieldName).asText());
	                    indtw++;
	                }
	            }
	            else if(fieldName.equals("in_reply_to_status_id_str")) {
	                if(node.get(fieldName).asText().equals("null")) {
	                    sntweet.setObject(indtw,null);
	                    indtw++;
	                } else {
	                    sntweet.setObject(indtw,node.get(fieldName).asText());
	                    indtw++;
	                }
	            }
	            else if(fieldName.equals("in_reply_to_screen_name")) {
	            	if(node.get(fieldName).asText().equals("null")) {
	                  sntweet.setObject(indtw,null);
	                  indtw++;
	                } else {
	                	sntweet.setObject(indtw,node.get(fieldName).asText());
	                	indtw++;
	                }
	            }
	        }
	        if(esiste==false) {
	            try {
	            	sntweet.execute();
	            } catch(Exception E) {
	                System.out.println("INSERIMENTO DATI NON RIUSCITO");
	                System.out.println(sntweet.toString());
	                System.out.println(E.toString());
	            }
	        }
	    }
	}


	/**
	 * Passa al controllo della lingua del tweet.
	 */
	public void controllaLingua() {
        JsonNode dataset = node.get("text");
        Detector detector;
        try {
            detector = DetectorFactory.create();
            detector.append(dataset.asText());
            lang = detector.detect();
        } catch (Exception ex) {
            lang="ex";
        }

    }


	public void parsingRetweet() throws SQLException {
        String field;
        JsonNode data=node.get("retweeted_status");
        Iterator<String> names=data.fieldNames();
        while(names.hasNext() && fine==false) {
            field=names.next();
            if(field.equals("id_str")) {
                sntweet.setObject(indtw,data.get(field).asText());
                indtw++;
                fine=true;
            }
        }
    }


	public void parsingUtente() {
		String campo;
		boolean esiste=false;

		try {
			int j=1;
	        JsonNode dataset = node.get("user");
	        Iterator<String> nomi=dataset.fieldNames();

	        while(nomi.hasNext()&& esiste==false) {
	           campo = nomi.next();
	           if(campo.equals("id_str")) {
	               sntweet.setObject(indtw,dataset.get(campo).asText());
	               indtw++;

	           }

	           if(campo.equals("id_str")|| campo.equals("name")|| campo.equals("screen_name")||campo.equals("lang")) {
	             if(campo.equals("id_str") || campo.equals("screen_name")) {
	                 if(hp.containsValue(dataset.get(campo).asText())==false) {

	                    hp.put(indice,dataset.get(campo).asText());
	                    sntutente.setObject(j,dataset.get(campo).asText());
	                    j++;
	                    indice++;
	                 }
	                 else
	                	esiste=true;
	             }
	             else {
	              sntutente.setObject(j,dataset.get(campo).asText());
	              j++;
	             }
	           }
	           else if(campo.equals("location")) {
	                if(!dataset.get(campo).asText().equals("")) {
	                    StringTokenizer t=new StringTokenizer(dataset.get(campo).asText(),",");

	                    sntutente.setObject(j,t.nextToken());
	                    j++;

	                }
	                else {
	                	sntutente.setObject(j,null);
	                    j++;

	                }
	           }
	           else if(campo.equals("followers_count")||campo.equals("friends_count")||campo.equals("statuses_count")||campo.equals("favourites_count")) {
	        	   sntweet.setObject(indtw,dataset.get(campo).asText());
	        	   indtw++;

	           }
	        }
	        if(esiste==false)
	        	sntutente.execute();
	        else {
	            while(nomi.hasNext()) {
	                campo=nomi.next();
	                if(campo.equals("followers_count")||campo.equals("friends_count")||campo.equals("statuses_count")||campo.equals("favourites_count")) {
	                sntweet.setObject(indtw,dataset.get(campo).asText());
	                indtw++;
	                }
	            }
	        }
	     } catch (SQLException ex) {
	    	 Logger.getLogger(TweetCrawler.class.getName()).log(Level.SEVERE, null, ex);
	     }
	}


	public void parsingEntita() throws SQLException {
		int numurl=0;
        String cnomi;
        JsonNode dataset = node.get("entities");
        Iterator<String> nomi=dataset.fieldNames();
        Iterator<JsonNode> arr = dataset.iterator();

        while(nomi.hasNext()) {
        	cnomi=nomi.next();
        	if(cnomi.equals("hashtags")) {
        		JsonNode datasetElement = arr.next();
        		Iterator<JsonNode> nm=datasetElement.iterator();

        		while(nm.hasNext()) {
        			JsonNode p=nm.next();
        			Iterator<String> ca=p.fieldNames();
        			while(ca.hasNext()) {
        				String ult=ca.next();
        				if(ult.equals("text")) {
        					infohashtag.add(p.get(ult).asText());
        				}
        			}
        		}
        	}
        	else if(cnomi.equals("urls")) {
        		JsonNode datasetElement = arr.next();
        		Iterator<JsonNode> nm=datasetElement.iterator();

        		while(nm.hasNext()) {
        			JsonNode p=nm.next();
        			Iterator<String> ca=p.fieldNames();
        			while(ca.hasNext()) {
        				String ult=ca.next();
        				if(ult.equals("url"))
        					numurl++;
        			}
        		}
        	}
        }
        if(fine==false) {
        	sntweet.setObject(indtw,null);
        	indtw++;
        }
        if(infohashtag.size()>6) {
        	int indx=0;
        	while(indx<6) {
        		sntweet.setObject(indtw,infohashtag.get(indx));
        		indtw++;
        		indx++;
        	}
        }
        else {
        	for(int i=0;i<infohashtag.size();i++) {
        		sntweet.setObject(indtw,infohashtag.get(i));
        		indtw++;

        		// INSERIMENTO IN TABELLA HASHTAG
        		sql_inserimento_tabella_hashtag.setObject(1,infohashtag.get(i));
                sql_inserimento_tabella_hashtag.setObject(2,infohashtag.get(i));
                sql_inserimento_tabella_hashtag.setObject(3,infohashtag.get(i));

                //sql_inserimento_tabella_hashtag.setObject(4,"SYSDATE");
                sql_inserimento_tabella_hashtag.setObject(4,"1");
                sql_inserimento_tabella_hashtag.setObject(5,"0");
                sql_inserimento_tabella_hashtag.setObject(6,"0");
                //System.out.println(sql_inserimento_tabella_hashtag);
                sql_inserimento_tabella_hashtag.execute();


        	}

        	if(infohashtag.size()<6) {
        		int size=6-infohashtag.size();
        		for(int i=0;i<size;i++)
        		{
        			sntweet.setObject(indtw,null);
        			indtw++;
        		}
        	}
        }

        sntweet.setObject(indtw,numurl);
        indtw++;
	}


	/**
	 * Stabilisce una connessione attraverso JDBC per le successive operazioni
	 * di scrittura sulle tabelle UTENTE, TWEET ed HASHTAG; successivamente
	 * inizia il crawling dei dati dalle Streaming API.
	 * @param chiave
	 *            parola chiave di ricerca
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws ConfigurationException
	 */
	public void connettiCrawlerTwitter(final String chiave) throws SQLException, ClassNotFoundException, ConfigurationException
	{
		this.property = configuraOracle();

		Locale.setDefault(Locale.ENGLISH);
		Class.forName(property.getProperty("oracle.driverClassName"));
	    try {
	    	String hashed_passw = hash.OraclePasswordDecryptor();
			connection=DriverManager.getConnection(property.getProperty("oracle.url"), property.getProperty("oracle.username"), hashed_passw);
			System.out.println("Connessione CRAWLER avvenuta");
	    } catch (SQLException e) {
	    	System.err.println("Connessione CRAWLER fallita");
			e.printStackTrace();
		}

	    // INSERIMENTO IN TABELLA UTENTE
		String sqlutente="insert into utente values(?,?,?,?,?)";
	    sntutente=connection.prepareStatement(sqlutente);

	    // INSERIMENTO IN TABELLA TWEET
	    String sqltweet="insert into tweet values(to_timestamp_tz(?,'DY MON DD HH24:MI:SS TZR YYYY'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	    sntweet=connection.prepareStatement(sqltweet);
	    sntweet.setString(21, chiave);

	    //NOME,DATA,1,SUM_IMPACT,USATO COME KEYWORD
	    String sql_inserimento_tabella_hashtag_query="MERGE INTO hashtag t1 USING (SELECT count(*) cont FROM hashtag where nome=? ) t2 ON (cont=1) WHEN MATCHED THEN update set t1.contatore=t1.contatore+1 where t1.nome = ? WHEN NOT MATCHED THEN INSERT (t1.nome,t1.first_time,t1.contatore,t1.sum_impact,t1.researched) VALUES (?,SYSDATE,?,?,?)";
	    sql_inserimento_tabella_hashtag=connection.prepareStatement(sql_inserimento_tabella_hashtag_query);

    }


	public void disconnettiCrawlerTwitter() {
	    try {
            connection.close();
            System.out.println("Disconnessione CRAWLER avvenuta");
        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            System.out.println("Disconnessione CRAWLER fallita");
        }
	}


}
