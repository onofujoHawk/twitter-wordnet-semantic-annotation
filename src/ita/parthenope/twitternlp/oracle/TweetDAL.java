/*
 * Università degli Studi di Napoli Parthenope
 */
package ita.parthenope.twitternlp.oracle;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.configuration.ConfigurationException;



/**
 * Oracle Data Access Layer object.
 * @author onofrio
 *
 */
public interface TweetDAL {
	
	/**
	 * Configurazione attraverso properties del database Oracle.
	 * @return le proprietà del database
	 */
	Properties configuraOracle();
	
	
	/**
	 * Salvataggio in Map<Integer,String> dell identificato e dello screen name
	 * dell'utente
	 * @param mappa_reverse
	 *            mappa con dati dell'utenza
	 * @param indice
	 *            indice dell'utente
	 * @param hp
	 *            mappa di output dopo la query
	 * @throws SQLException
	 * @throws ConfigurationException
	 */
	void salvaHashMap(Vector<String> mappa_reverse, int indice, HashMap<Integer,String> hp) throws SQLException, ConfigurationException;
	
	
	/**
	 * Estrae gli hashtag associati alla risposta.
	 * @param id
	 *            identificativo della conversazione
	 * @param mode
	 *            tipo di hashtag estratti: incipit/risposta
	 * @return il vector contenente gli hashtags
	 * @throws SQLException
	 */
	Vector<String> estraiHashtag(String id,String mode) throws SQLException;
	
	
	/**
	 * Va a ricostruire una conversazione di Twitter completa di incipit e
	 * risposte attraverso interrogazione del database Oracle per parola chiave
	 * di ricerca.
	 * @param chiavi
	 *            la parola chiave
	 * @param hashtags
	 *            gli hashtag associati alla conversazione
	 * @param hashmap_conversazioni
	 *            Map<Integer,String> contenente le conversazioni
	 * @return la stringa della conversazione
	 * @throws SQLException
	 * @throws ConfigurationException
	 */
	String ricostruisciConversazioneTwitter(Vector<String> chiavi, Vector<String> hashtags, HashMap<Integer,String> hashmap_conversazioni) throws SQLException, ConfigurationException;
	
	
	/**
	 * Effettua la connessione del database Oracle.
	 * @throws ConfigurationException
	 */
	void connettiDatabaseOracle() throws ConfigurationException;
	
	
	/**
	 * Effettua la disconnessione del database Oracle.
	 */
	void disconnettiDatabaseOracle();
	
	
	/**
	 * Effettua la query di conteggio degli hashtag memorizzati.
	 * @throws SQLException
	 * @throws ConfigurationException
	 */
	void contaHashtag() throws SQLException, ConfigurationException;

}
