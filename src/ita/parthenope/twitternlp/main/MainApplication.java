/*
 * Università degli Studi di Napoli Parthenope
 */
package ita.parthenope.twitternlp.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;

import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import ita.parthenope.twitternlp.crawler.TweetCrawler;
import ita.parthenope.twitternlp.oracle.TweetService;
import ita.parthenope.twitternlp.semantic.SemanticGraph;
import ita.parthenope.twitternlp.utils.Utility;
import net.didion.jwnl.JWNLException;





/**
 * Main entry di questa applicazione.
 * @author onofrio
 *
 */
public class MainApplication {
	
	static String parola_chiave="resources/parola_chiave.txt";
	static Integer SCELTA=0;
	static Scanner input;

	
	public MainApplication() {
		super();
	}
	
	
	/**
	 * Avvia l'applicazione.
	 * @throws LangDetectException
	 * @throws InterruptedException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 * @throws ConfigurationException
	 * @throws JWNLException
	 */
	private void run() throws LangDetectException, InterruptedException, SQLException, ClassNotFoundException, IOException, ParseException, ConfigurationException, JWNLException 
	{
		DetectorFactory.loadProfile("language-detection/language-detector/profiles");
		String PAROLA_DA_CERCARE = null;
		boolean controlla = false;
		
		leggiOperazione();
		switch (SCELTA) 
		{
		 case 1:
			 System.out.println("MODALITA' COSTRUZIONE DEL GRAFO");
			 SemanticGraph sg=new SemanticGraph();
			 TweetService ts=new TweetService();
			 ts.contaHashtag();
			 sg.disegnaGrafo();
			 break;
			 
		 case 2:
			 System.out.println("MODALITA' AMPLIAMENTO DATABASE");
			 Utility utility=new Utility();
			 File file = new File(parola_chiave);
				if (file.exists() || file.isFile()) 
				{
					BufferedReader reader=utility.apriFile("resources/parola_chiave.txt", "input", file.isFile());
					String linea = null;
					System.out.println("LEGGO LA PAROLA DA CERCARE NEL FILE");
					
					try {
						while ((linea = reader.readLine()) != null) {
							if (!linea.isEmpty()) {
								PAROLA_DA_CERCARE = linea.trim();
								controlla = true;
							}
						}
						if (controlla) {
							Thread.sleep(1000);
							System.out.println("Lettura della parola chiave: "+PAROLA_DA_CERCARE);
						} 
					} catch (IOException e) 
					{
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
					}
				} else {
					System.err.println("ERRORE: IL FILE "+file.getName()+" NON E' PRESENTE NELLA DIRECTORY "+file.getParentFile());
				}
				if (PAROLA_DA_CERCARE == null) 
				{
					System.err.println("IL FILE E' VUOTO O INESISTENTE... PASSO ALLA LETTURA DA STDIN");
					Scanner input = new Scanner (System.in);
					Thread.sleep(1000);
					while (PAROLA_DA_CERCARE == null) {
						System.out.print("Digita la parola da cercare: ");
				        PAROLA_DA_CERCARE=input.nextLine();
					}
			        input.close();
				}
				
		        TweetCrawler cw=new TweetCrawler();
		        cw.connettiCrawlerTwitter(PAROLA_DA_CERCARE);
		        cw.riempiHashMap();
		        cw.crawl(PAROLA_DA_CERCARE);
		        cw.disconnettiCrawlerTwitter();
		        break;
		 }
	}
	
	
	/**
	 * Lettura dell'operazione da fare sull'applicazione.
	 * @throws InterruptedException
	 */
	static private void leggiOperazione() throws InterruptedException {
		input = new Scanner (System.in);
		do {
			System.out.println("Scegli un operazione");
			System.out.println("1. Costruzione del grafo semantico");
			System.out.println("2. Ampliamento del database Oracle");
			Thread.sleep(1000);
			System.out.print("your_choice:~$ ");
	        SCELTA=input.nextInt();
	        System.out.println();
		} while ( SCELTA.intValue() < 1 || SCELTA.intValue() > 2 );
		
	}
	
	
	public static void main(String... strings) 
	{
		try {
			new MainApplication().run();
		} catch (ClassNotFoundException | LangDetectException | InterruptedException | SQLException | IOException | ParseException | ConfigurationException | JWNLException e) 
		{
			System.err.println(e.getMessage());
		}
	}
	
}
