/*
Copyright (c) 2009, ShareThis, Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.

    * Neither the name of the ShareThis, Inc., nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package ita.parthenope.twitternlp.textrank;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import net.didion.jwnl.data.POS;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.log4j.PropertyConfigurator;


/**
 * Java implementation of the TextRank algorithm by Rada Mihalcea, et al.
 *    http://lit.csci.unt.edu/index.php/Graph-based_NLP
 *
 * @author paco@sharethis.com
 */

public class
    TextRank
    implements Callable<Collection<MetricVector>>
{
    // logging

    private final static Log LOG =
        LogFactory.getLog(TextRank.class.getName());


    /**
     * Public definitions.
     */

    public final static String NLP_RESOURCES = "nlp.resources";
    public final static double MIN_NORMALIZED_RANK = 0.0001D;
    
    // SETTATO PRECEDENTEMENTE A 5
    public final static int MAX_NGRAM_LENGTH = 2; 
    public final static long MAX_WORDNET_TEXT = 2000L;
    public final static long MAX_WORDNET_GRAPH = 5L;
    
    // LE MIE IMPOSTAZIONE PER IL TEXTRANK
    public final static String CONFIGURAZIONE_LOG4J = "res/log4j.properties";
    public final static String LANGUAGE_CODE = "en";
    public final static String PATH_RISULTATO = "res";

    
    /**
     * My Private members.
     */
    
    private static List<String> myKeyword = new ArrayList<String>();
    public static void reset() 
    {
    	myKeyword.clear();
    }
    
    
    /**
     * Protected members.
     */

    protected LanguageModel lang = null;

    protected String text = null;
    protected boolean use_wordnet = false;

    protected Graph graph = null;
    protected Graph ngram_subgraph = null;
    protected Map<NGram, MetricVector> metric_space = null;

    protected long start_time = 0L;
    protected long elapsed_time = 0L;


    /**
     * Constructor.
     */

    public
	TextRank (final String res_path, final String lang_code)
	throws Exception
    {
	lang = LanguageModel.buildLanguage(res_path, lang_code);
	WordNet.buildDictionary(res_path, lang_code);
    }
    

    /**
     * Prepare to call algorithm with a new text to analyze.
     */

    public void
	prepCall (final String testoDaAnalizzare, final boolean use_wordnet)
	throws Exception
    {
	graph = new Graph();
	ngram_subgraph = null;
	metric_space = new HashMap<NGram, MetricVector>();

	this.text = testoDaAnalizzare;
	this.use_wordnet = use_wordnet;
    }


    /**
     * Run the TextRank algorithm on the given semi-structured text
     * (e.g., results of parsed HTML from crawled web content) to
     * build a graph of weighted key phrases.
     */

    public Collection<MetricVector>
	call ()
	throws Exception
    {
	//////////////////////////////////////////////////
	// PASS 1: construct a graph from PoS tags

	initTime();

	// scan sentences to construct a graph of relevent morphemes

	final ArrayList<Sentence> s_list = new ArrayList<Sentence>();

	for (String sent_text : lang.splitParagraph(text)) {
	    final Sentence s = new Sentence(sent_text.trim());
	    s.mapTokens(lang, graph);
	    s_list.add(s);

	    if (LOG.isDebugEnabled()) {
		LOG.debug("s: " + s.text);
		LOG.debug(s.md5_hash);
	    }
	}

	markTime("construct_graph");

	//////////////////////////////////////////////////
	// PASS 2: run TextRank to determine keywords

	initTime();

	final int max_results =
	    (int) Math.round((double) graph.size() * Graph.KEYWORD_REDUCTION_FACTOR);

	graph.runTextRank();
	graph.sortResults(max_results);

	ngram_subgraph = NGram.collectNGrams(lang, s_list, graph.getRankThreshold());

	markTime("basic_textrank");

	if (LOG.isInfoEnabled()) {
	    LOG.info("TEXT_BYTES:\t" + text.length());
	    LOG.info("GRAPH_SIZE:\t" + graph.size());
	}

	//////////////////////////////////////////////////
	// PASS 3: lemmatize selected keywords and phrases

	initTime();

	Graph synset_subgraph = new Graph();

	// filter for edge cases

	if (use_wordnet &&
	    (text.length() < MAX_WORDNET_TEXT) &&
	    (graph.size() < MAX_WORDNET_GRAPH)
	    ) {
	    // test the lexical value of nouns and adjectives in WordNet

	    for (Node n: graph.values()) {
		final KeyWord kw = (KeyWord) n.value;

		if (lang.isNoun(kw.pos)) {
		    SynsetLink.addKeyWord(synset_subgraph, n, kw.text, POS.NOUN);
		}
		else if (lang.isAdjective(kw.pos)) {
		    SynsetLink.addKeyWord(synset_subgraph, n, kw.text, POS.ADJECTIVE);
		}
	    }

	    // test the collocations in WordNet

	    for (Node n : ngram_subgraph.values()) {
		final NGram gram = (NGram) n.value;

		if (gram.nodes.size() > 1) {
		    SynsetLink.addKeyWord(synset_subgraph, n, gram.getCollocation(), POS.NOUN);
		}
	    }

	    synset_subgraph =
		SynsetLink.pruneGraph(synset_subgraph, graph);
	}

	// augment the graph with n-grams added as nodes

	for (Node n : ngram_subgraph.values()) {
	    final NGram gram = (NGram) n.value;

	    if (gram.length < MAX_NGRAM_LENGTH) {
		graph.put(n.key, n);

		for (Node keyword_node : gram.nodes) {
		    n.connect(keyword_node);
		}
	    }
	}

	markTime("augment_graph");

	//////////////////////////////////////////////////
	// PASS 4: re-run TextRank on the augmented graph

	initTime();

	graph.runTextRank();
	//graph.sortResults(graph.size() / 2);

	// collect stats for metrics

	final int ngram_max_count =
	    NGram.calcStats(ngram_subgraph);

	if (use_wordnet) {
	    SynsetLink.calcStats(synset_subgraph);
	}

	markTime("ngram_textrank");

	if (LOG.isInfoEnabled()) {
	    if (LOG.isDebugEnabled()) {
		LOG.debug("RANK: " + ngram_subgraph.dist_stats);

		for (Node n : new TreeSet<Node>(ngram_subgraph.values())) {
		    final NGram gram = (NGram) n.value;
		    LOG.debug(gram.getCount() + " " + n.rank + " " + gram.text /* + " @ " + gram.renderContexts() */);
		}
	    }

	    if (LOG.isDebugEnabled()) {
		LOG.debug("RANK: " + synset_subgraph.dist_stats);

		for (Node n : new TreeSet<Node>(synset_subgraph.values())) {
		    final SynsetLink s = (SynsetLink) n.value;
		    LOG.info("emit: " + s.synset + " " + n.rank + " " + s.relation);
		}
	    }
	}

	//////////////////////////////////////////////////
	// PASS 5: construct a metric space for overall ranking

	initTime();

	final double link_min = ngram_subgraph.dist_stats.getMin();
	final double link_coeff = ngram_subgraph.dist_stats.getMax() - ngram_subgraph.dist_stats.getMin();

	final double count_min = 1;
	final double count_coeff = (double) ngram_max_count - 1;

	final double synset_min = synset_subgraph.dist_stats.getMin();
	final double synset_coeff = synset_subgraph.dist_stats.getMax() - synset_subgraph.dist_stats.getMin();

	for (Node n : ngram_subgraph.values()) {
	    final NGram gram = (NGram) n.value;

	    if (gram.length < MAX_NGRAM_LENGTH) {
		final double link_rank = (n.rank - link_min) / link_coeff;
		final double count_rank = (gram.getCount() - count_min) / count_coeff;
		final double synset_rank = use_wordnet ? n.maxNeighbor(synset_min, synset_coeff) : 0.0D;

		final MetricVector mv = new MetricVector(gram, link_rank, count_rank, synset_rank);
		metric_space.put(gram, mv);
	    }
	}

	markTime("normalize_ranks");

	// return results

	return metric_space.values();
    }


    //////////////////////////////////////////////////////////////////////
    // access and utility methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Re-initialize the timer.
     */

    public void
	initTime ()
    {
	start_time = System.currentTimeMillis();
    }


    /**
     * Report the elapsed time with a label.
     */

    public void
	markTime (final String label)
    {
	elapsed_time = System.currentTimeMillis() - start_time;

	if (LOG.isInfoEnabled()) {
	    LOG.info("ELAPSED_TIME:\t" + elapsed_time + "\t" + label);
	}
    }


    /**
     * Accessor for the graph.
     */

    public Graph
	getGraph ()
    {
	return graph;
    }


    /**
     * Serialize the graph to a file which can be rendered.
     */

    public void
	serializeGraph (final String graph_file)
	throws Exception
    {
	for (Node n : graph.values()) {
	    n.marked = false;
	}

	final TreeSet<String> entries = new TreeSet<String>();

	for (Node n : ngram_subgraph.values()) {
	    final NGram gram = (NGram) n.value;
	    final MetricVector mv = metric_space.get(gram);

	    if (mv != null) {
		final StringBuilder sb = new StringBuilder();

		sb.append("rank").append('\t');
		sb.append(n.getId()).append('\t');
		sb.append(mv.render());
		entries.add(sb.toString());

		n.serializeGraph(entries);
	    }
	}

        final OutputStreamWriter fw =
	    new OutputStreamWriter(new FileOutputStream(graph_file), "UTF-8");
						   
        try {
	    for (String entry : entries) {
		fw.write(entry, 0, entry.length());
		fw.write('\n');
	    }
        }
	finally {
            fw.close();
        }
    }


    /**
     * Serialize resulting graph to a string.
     */

    public String
	toString ()
    {
	final TreeSet<MetricVector> key_phrase_list = new TreeSet<MetricVector>(metric_space.values());
	final StringBuilder sb = new StringBuilder();

	for (MetricVector mv : key_phrase_list) {
	    if (mv.metric >= MIN_NORMALIZED_RANK) {
	    // SOLO IL TESTO CLASSIFICATO DAL TEXTRANK
		//sb.append(mv.value.text).append("\n");
	    	sb.append(mv.render()).append("\t").append(mv.value.text).append("\n");
	    	if (!myKeyword.contains(cleanKeyWord(mv.value.text)) && cleanKeyWord(mv.value.text).length() > 2) 
	    	{
	    		myKeyword.add(cleanKeyWord(mv.value.text));
	    	}
	    }
	}

	return sb.toString();
    }


    /**
     * Classificatore di parole significative.
     * @throws Exception 
     */
    
    public void
    doTextRanking (String testoDaAnalizzare) 
    throws Exception 
    {
    	final String log4j_conf=new File(CONFIGURAZIONE_LOG4J)
    			.getCanonicalPath();
    	final String res_path = PATH_RISULTATO;
    	final String lang_code = LANGUAGE_CODE;
    	
            // set up logging for debugging and instrumentation
            
            PropertyConfigurator.configure(log4j_conf);

    	// load the sample text from a file

    	//final String text = IOUtils.readFile(data_file);

    	// filter out overly large files

    	boolean use_wordnet = true; // false
    	use_wordnet = use_wordnet && ("en".equals(lang_code));

    	// main entry point for the algorithm

    	final TextRank tr = new TextRank(res_path, lang_code);
    	tr.prepCall(testoDaAnalizzare, use_wordnet);

    	// wrap the call in a timed task

    	final FutureTask<Collection<MetricVector>> task = new FutureTask<Collection<MetricVector>>(tr);
    	Collection<MetricVector> answer = null;

    	final Thread thread = new Thread(task);
    	thread.run();

    	try {
    	    answer = task.get();  // run until complete
    	    //answer = task.get(15000L, TimeUnit.MILLISECONDS); // timeout in N ms
    	}
    	catch (ExecutionException e) {
    	    LOG.error("exec exception", e);
    	}
    	catch (InterruptedException e) {
    	    LOG.error("interrupt", e);
    	}
    	//catch (TimeoutException e) {
    	    //LOG.error("timeout", e);
    	    
    	    // Unfortunately, with graph size > 700, even read-only
    	    // access to WordNet on disk will block and cause the
    	    // thread to be uninterruptable. None of the following
    	    // remedies work...

    	    //thread.interrupt();
    	    //task.cancel(true);
    	    //return null;
    	//}
    	
    	LOG.info("\n" + tr);     
    }
    
    public static String cleanKeyWord(String keyword) 
    {
    	keyword=keyword.replaceAll("\'","\'\'");
    	keyword=keyword.replaceAll("\"","\'\"");
    	keyword=keyword.replaceAll("[^A-Z+a-z+0-9+&àèìòù#]"," ").toLowerCase();
    	keyword=keyword.replaceAll("      "," ");
    	keyword=keyword.replaceAll("     "," ");
    	keyword=keyword.replaceAll("    "," ");
    	keyword=keyword.replaceAll("   "," ");
    	keyword=keyword.replaceAll("  "," ");
    	return keyword;
    }
    
    public List<String> getKeyWord()
    {
    	ArrayList<String> ritorno = new ArrayList<String>();
    	for (String key : myKeyword) 
    	{
    		ritorno.add(new String(key));
    	}
    	
    	return ritorno;
    }


	public LanguageModel 
	getLang() 
	{
		return lang;
	}

	public void 
	setLang(LanguageModel lang) 
	{
		this.lang = lang;
	}

	public String 
	getText() 
	{
		return text;
	}

	public void 
	setText(String text) 
	{
		this.text = text;
	}

	public boolean 
	isUse_wordnet() 
	{
		return use_wordnet;
	}

	public void 
	setUse_wordnet(boolean use_wordnet) 
	{
		this.use_wordnet = use_wordnet;
	}

	public Graph 
	getNgram_subgraph() 
	{
		return ngram_subgraph;
	}

	public void 
	setNgram_subgraph(Graph ngram_subgraph) 
	{
		this.ngram_subgraph = ngram_subgraph;
	}

	public Map<NGram, MetricVector> 
	getMetric_space() 
	{
		return metric_space;
	}

	public void 
	setMetric_space(Map<NGram, MetricVector> metric_space) 
	{
		this.metric_space = metric_space;
	}

	public long 
	getStart_time() 
	{
		return start_time;
	}

	public void 
	setStart_time(long start_time) 
	{
		this.start_time = start_time;
	}

	public long 
	getElapsed_time() 
	{
		return elapsed_time;
	}

	public void 
	setElapsed_time(long elapsed_time) 
	{
		this.elapsed_time = elapsed_time;
	}

	public static Log 
	getLog() 
	{
		return LOG;
	}

	public static double 
	getMinNormalizedRank() 
	{
		return MIN_NORMALIZED_RANK;
	}

	public static int 
	getMaxNgramLength() 
	{
		return MAX_NGRAM_LENGTH;
	}

	public static long 
	getMaxWordnetText() 
	{
		return MAX_WORDNET_TEXT;
	}

	public static long 
	getMaxWordnetGraph() 
	{
		return MAX_WORDNET_GRAPH;
	}

	public void 
	setGraph(Graph graph) 
	{
		this.graph = graph;
	}

}
