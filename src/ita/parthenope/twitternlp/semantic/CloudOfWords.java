/*
 * Università degli Studi di Napoli Parthenope
 */
package ita.parthenope.twitternlp.semantic;

import java.util.List;
import java.util.Random;

import com.kennycason.kumo.nlp.FrequencyAnalyzer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.LayeredWordCloud;
import com.kennycason.kumo.PolarBlendMode;
import com.kennycason.kumo.PolarWordCloud;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.bg.PixelBoundryBackground;
import com.kennycason.kumo.bg.RectangleBackground;
import com.kennycason.kumo.font.FontWeight;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.image.AngleGenerator;
import com.kennycason.kumo.palette.ColorPalette;
import com.kennycason.kumo.palette.LinearGradientColorPalette;
import com.kennycason.kumo.wordstart.CenterWordStart;

import ita.parthenope.twitternlp.babelnet.StopWordRemover;




/**
 * Costruzione di una Word Cloud con Kumo API.
 * @author onofrio
 *
 */
public class CloudOfWords extends JPanel {
	
	private WordCloud wordCloud;
	private LayeredWordCloud layeredWordCloud;
	private PolarWordCloud polarityWordCloud;
	private FrequencyAnalyzer frequencyAnalyzer;
	private static final Random RANDOM = new Random();
	protected String percorsoImmagine = null;
	WordFrequency CHIAVE_FREQUENCY;
	
	private static String inputFile;
	private Dimension dimensione;
	private String parolaChiave;
	private String tipologia;
	
	private static final long serialVersionUID = 2248272024164645352L;

	
	/*
	 * Costruttore di default
	 */
	public CloudOfWords() {
		super();
	}
	
	
	/*
	 * Costruttore con parametri
	 */
	public CloudOfWords(String inputFile, String parolaChiave, Dimension dimensione, String tipologia)
	{
		super();
		CloudOfWords.inputFile = inputFile;
		this.parolaChiave = parolaChiave;
		this.dimensione = dimensione;
		this.tipologia = tipologia;
	}
	
	
	/*
	 * Costruttore con parametri per image-based wordcloud
	 */
	public CloudOfWords(String inputFile, Dimension dimensione, String parolaChiave, String percorsoImmagine) throws IOException 
	{
		super();
		CloudOfWords.inputFile = inputFile;
		this.dimensione = dimensione;
		this.parolaChiave = parolaChiave;
		this.tipologia = "image";
		this.percorsoImmagine = percorsoImmagine;
	}
	
	
	/**
	 * 
	 * @param parolaChiave the parolaChiave to set
	 */
	public void setParolaChiave(final String parolaChiave) {
		this.parolaChiave = parolaChiave;
	}
	

	/**
	 * Word Cloud costruita su un'immagine di input.
	 * @return la WordCloud costruita
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public WordCloud CloudOfWordsUsingImage() throws FileNotFoundException, IOException 
	{
		this.frequencyAnalyzer = new FrequencyAnalyzer();
		frequencyAnalyzer.setWordFrequenciesToReturn(500);
        frequencyAnalyzer.setMinWordLength(2);
        CHIAVE_FREQUENCY = new WordFrequency(analyzeParolaChiave(), 20);
        
        final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(getInputStream(getInputFile())); 
        wordFrequencies.add(CHIAVE_FREQUENCY);
		final Dimension dimension = getDimensione();
		this.wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
		wordCloud.setPadding(2);
		if (checkPercorsoImmagine())
			wordCloud.setBackground(new PixelBoundryBackground(getInputStream(this.percorsoImmagine)));
		else
			wordCloud.setBackground(new PixelBoundryBackground(getInputStream("backgrounds/whale_small.png")));
		wordCloud.setColorPalette(buildRandomColorPalette(10));
		wordCloud.setFontScalar(new LinearFontScalar(10, 40));
		wordCloud.setAngleGenerator(new AngleGenerator(0));
		
		final long startTime = System.currentTimeMillis();
		wordCloud.build(wordFrequencies);

		System.out.println("Took " + (System.currentTimeMillis() - startTime) + "ms to build");
		wordCloud.writeToFile("wordcloud/kumo_wordcloud_bgimage_"+getParolaChiave().toLowerCase()+".png");
		
		return wordCloud; 
	}
	
	
	/**
	 * Costruisce una Word Cloud di forma rettangolare.
	 * @return la Word Cloud costruita
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public WordCloud RectangleCloudOfWords() throws FileNotFoundException, IOException {
		this.frequencyAnalyzer = new FrequencyAnalyzer();
        frequencyAnalyzer.setMinWordLength(2);
        CHIAVE_FREQUENCY = new WordFrequency(analyzeParolaChiave(), 20);
		
		final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(getInputStream(getInputFile())); //Testo di Input
		wordFrequencies.add(CHIAVE_FREQUENCY);
        //final List<WordFrequency> wordFrequencies = buildRandomWordFrequencies(); //Assegna casualmente una frequenza ad ogni word
		final Dimension dimension = new Dimension(600, 600); //getDimensione();
		this.wordCloud = new WordCloud(dimension, CollisionMode.RECTANGLE);
		wordCloud.setPadding(2);
		wordCloud.setBackground(new RectangleBackground(dimension));
		wordCloud.setColorPalette(new ColorPalette(new Color(0x4055F1), new Color(0x408DF1), new Color(0x40AAF1), new Color(0x40C5F1), new Color(0x40D3F1), new Color(0xFFFFFF)));
		wordCloud.setFontScalar(new LinearFontScalar(10, 40));
		wordCloud.setAngleGenerator(new AngleGenerator(0));
		wordCloud.setWordStartStrategy(new CenterWordStart());
		
		final long startTime = System.currentTimeMillis();
		wordCloud.build(wordFrequencies);
		System.out.println("Took " + (System.currentTimeMillis() - startTime) + "ms to build");
		
		wordCloud.writeToFile("wordcloud/kumo_wordcloud_rectangular_"+getParolaChiave().toLowerCase()+".png"); //Immagine di Output
		return wordCloud;
	}
	
	
	/**
	 * Costruisce una Word Cloud di forma circolare.
	 * @return la Word Cloud costruita
	 * @throws IOException
	 */
	public WordCloud CircularCloudOfWords() throws IOException {
		this.frequencyAnalyzer = new FrequencyAnalyzer();
        frequencyAnalyzer.setMinWordLength(2);
        frequencyAnalyzer.setWordFrequenciesToReturn(300);
        CHIAVE_FREQUENCY = new WordFrequency(analyzeParolaChiave(), 20);
		
		final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(getInputStream(getInputFile())); //Testo di Input
		wordFrequencies.add(CHIAVE_FREQUENCY);
		final Dimension dimension;
		if (getDimensione().getWidth() == getDimensione().getHeight()) {
			//Ok dimensione corrente
			dimension = getDimensione();
		} else {
			System.err.println("WARNING: For a circular word cloud width and height must be equals");
			int dim = Math.max((int) getDimensione().getWidth(),(int) getDimensione().getHeight());
			dimension = new Dimension(dim,dim);
		}
		this.wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
		wordCloud.setPadding(2);
		wordCloud.setBackground(new CircleBackground((int) (getDimensione().getWidth()/2)));
		wordCloud.setColorPalette(new LinearGradientColorPalette(Color.WHITE, Color.RED, Color.BLUE, 10 , 10));
		wordCloud.setFontScalar(new SqrtFontScalar(10, 40));
		wordCloud.setWordStartStrategy(new CenterWordStart());
		
		final long startTime = System.currentTimeMillis();
		wordCloud.build(wordFrequencies);
		System.out.println("Took " + (System.currentTimeMillis() - startTime) + "ms to build");
		
		wordCloud.writeToFile("wordcloud/kumo_wordcloud_circle_sqrt_"+getParolaChiave().toLowerCase()+".png"); //Immagine di Output
		return wordCloud;
	}
	
	
	/**
	 * Costruzione di una layered Word Cloud, utilizzando due testi di input e
	 * producendo in output una cloud a due livelli.
	 * @return la Word Cloud costruita
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public LayeredWordCloud LayeredCloudOfWords() throws FileNotFoundException, IOException {
		this.frequencyAnalyzer = new FrequencyAnalyzer();
        frequencyAnalyzer.setWordFrequenciesToReturn(300);
        frequencyAnalyzer.setMinWordLength(5);
        frequencyAnalyzer.setStopWords(loadStopWords());
        CHIAVE_FREQUENCY = new WordFrequency(analyzeParolaChiave(), 20);

        final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(getInputStream("resources/testo_esempio_positive.txt"));
        final List<WordFrequency> wordFrequencies2 = frequencyAnalyzer.load(getInputStream("resources/testo_esempio_negative.txt"));
        final Dimension dimension = new Dimension(600, 386);
        this.layeredWordCloud = new LayeredWordCloud(2, dimension, CollisionMode.PIXEL_PERFECT);

        layeredWordCloud.setPadding(0, 1);
        layeredWordCloud.setPadding(1, 1);

        layeredWordCloud.setKumoFont(0, new KumoFont("LICENSE PLATE", FontWeight.BOLD));
        layeredWordCloud.setKumoFont(1, new KumoFont("Comic Sans MS", FontWeight.BOLD));

        layeredWordCloud.setBackground(0, new PixelBoundryBackground(getInputStream("backgrounds/cloud_bg.bmp")));
        layeredWordCloud.setBackground(1, new PixelBoundryBackground(getInputStream("backgrounds/cloud_fg.bmp")));

        layeredWordCloud.setColorPalette(0, new ColorPalette(new Color(0xABEDFF), new Color(0x82E4FF), new Color(0x55D6FA)));
        layeredWordCloud.setColorPalette(1, new ColorPalette(new Color(0xFFFFFF), new Color(0xDCDDDE), new Color(0xCCCCCC)));

        layeredWordCloud.setFontScalar(0, new SqrtFontScalar(10, 40));
        layeredWordCloud.setFontScalar(1, new SqrtFontScalar(10, 40));

        final long startTime = System.currentTimeMillis();
        layeredWordCloud.build(0, wordFrequencies);
        layeredWordCloud.build(1, wordFrequencies2);
        
        System.out.println("Took " + (System.currentTimeMillis() - startTime) + "ms to build");
        layeredWordCloud.writeToFile("wordcloud/layered_wordcloud_sqrt_"+getParolaChiave().toLowerCase()+".png");
        
        return layeredWordCloud;
	}
	
	
	/**
	 * Costruisce una Cloud basata su polarità, andando ad utilizzare due testi
	 * di input, contenenti le word per costruire la Word Cloud polare.
	 * @return la Word Cloud costruita
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public PolarWordCloud PolarityCloudOfWords() throws FileNotFoundException, IOException {
		this.frequencyAnalyzer = new FrequencyAnalyzer();
		frequencyAnalyzer.setWordFrequenciesToReturn(750);
		frequencyAnalyzer.setMinWordLength(4);
		frequencyAnalyzer.setStopWords(loadStopWords());
		CHIAVE_FREQUENCY = new WordFrequency(analyzeParolaChiave(), 20);

		final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(getInputStream("resources/testo_esempio_positive.txt"));
		final List<WordFrequency> wordFrequencies2 = frequencyAnalyzer.load(getInputStream("resources/testo_esempio_negative.txt"));
		final Dimension dimension;
		if (getDimensione().getWidth() == getDimensione().getHeight()) {
			//Ok dimensione corrente
			dimension = getDimensione();
		} 
		else {
			System.err.println("WARNING: For a polarity word cloud width and height must be equals");
			int dim = Math.max((int) getDimensione().getWidth(),(int) getDimensione().getHeight());
			dimension = new Dimension(dim,dim);
		}
		this.polarityWordCloud = new PolarWordCloud(dimension, CollisionMode.PIXEL_PERFECT, PolarBlendMode.BLUR);
		polarityWordCloud.setPadding(2);
		polarityWordCloud.setBackground(new CircleBackground(300));
		polarityWordCloud.setFontScalar(new SqrtFontScalar(10, 40));
		
		final long startTime = System.currentTimeMillis();
		polarityWordCloud.build(wordFrequencies, wordFrequencies2);
		System.out.println("Took " + (System.currentTimeMillis() - startTime) + "ms to build");
		polarityWordCloud.writeToFile("wordcloud/polar_newyork_circle_blur_sqrt_font.png");
        
        return polarityWordCloud;
	}
	
	
	protected Component assembler() throws IOException 
	{
		/*
		 *  Sarà costruita una WordCloud in base al tipo settato.
		 */
		switch(tipologia) 
		{
		case "rectangle":
			
			this.wordCloud = RectangleCloudOfWords();

	        final JLabel rectangleWordCloudLabel = new JLabel(new ImageIcon(wordCloud.getBufferedImage()));
	        add(rectangleWordCloudLabel);
	        repaint();
	        
			return rectangleWordCloudLabel;
			
		case "circular":
			
			this.wordCloud = CircularCloudOfWords();

	        final JLabel wordCloudLabel = new JLabel(new ImageIcon(wordCloud.getBufferedImage()));
	        add(wordCloudLabel);
	        repaint();
	        
			return wordCloudLabel;
			
		case "layered":
			
			this.layeredWordCloud = LayeredCloudOfWords();

	        final JLabel layeredWordCloudLabel = new JLabel(new ImageIcon(layeredWordCloud.getBufferedImage()));
	        add(layeredWordCloudLabel);
	        repaint();
	        
			return layeredWordCloudLabel;
		
		case "polarity":
			
			this.polarityWordCloud = PolarityCloudOfWords();

	        final JLabel polarityWordCloudLabel = new JLabel(new ImageIcon(polarityWordCloud.getBufferedImage()));
	        add(polarityWordCloudLabel);
	        repaint();
	        
			return polarityWordCloudLabel;
			
		case "image":
			
			this.wordCloud = CloudOfWordsUsingImage();

	        final JLabel bgimageWordCloudLabel = new JLabel(new ImageIcon(wordCloud.getBufferedImage()));
	        add(bgimageWordCloudLabel);
	        repaint();
	        
			return bgimageWordCloudLabel;
			
		default:
			
			return null;
		}
		
	}
	
	
	/**
	 * Costruzione della cloud e visualizzazione.
	 * @throws IOException
	 */
	public void buildCloud() throws IOException {
        final JFrame frame = new JFrame("Kumo: WordCloud Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //This function attach AWT Component to JPanel 
        Component component = assembler(); 
        frame.add(component); 
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
	
	
	/**
	 * 
	 * @param inputFile the inputFile to set
	 */
	public void setInputFile(String inputFile) {
		CloudOfWords.inputFile = inputFile;
	}
	
	
	private Set<String> loadStopWords() {
        try {
            final List<String> lines = IOUtils.readLines(getInputStream("resources/stopwords_en.txt"));
            return new HashSet<>(lines);

        } catch (IOException e) {
        	Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
        return Collections.emptySet();
    }
	
	
	static private InputStream getInputStream(final String path) throws FileNotFoundException {
		return new FileInputStream(path);
    }
	
	
	/**
	 * 
	 * @return the inputFile
	 */
	static public String getInputFile() {
		return inputFile;
	}

	
	/**
	 * 
	 * @return the parolaChiave
	 */
	public String getParolaChiave() {
		return parolaChiave;
	}
	
	
	protected ColorPalette buildRandomColorPalette(int n) {
		final Color[] colors = new Color[n];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = new Color(RANDOM.nextInt(230) + 25, RANDOM.nextInt(230) + 25, RANDOM.nextInt(230) + 25);
        }
        
        return new ColorPalette(colors);
    }
	
	
	/**
	 * Assegna in modo randomico un valore di frequenza ad ogni parola
	 * e passa all'eliminazione delle stopwords dal testo.
	 * @return la lista di frequenze per ogni parola
	 * @throws IOException
	 */
	protected List<WordFrequency> buildRandomWordFrequencies() throws IOException 
	{
		final List<String> wordNames = getWordNames();
        final List<WordFrequency> wordFrequencies = new ArrayList<>();
        StopWordRemover stops = new StopWordRemover("resources/stopwords_en.txt");
       
        //Vengono rimossi i duplicati
        Collection<String> wordNamesNoDups = new LinkedHashSet<String>(wordNames);
        wordNames.clear();
        wordNames.addAll(wordNamesNoDups);
        
        for (String name : wordNames) {
        	Pattern p = Pattern.compile("\\b[A-Z]{4,}\\b");
        	Matcher m = p.matcher(name);
        	if (m.find()) {
        		
        		String k = stops.capitalizeFirstLetter(m.group().toLowerCase());
        		wordFrequencies.add(new WordFrequency(k, Integer.MAX_VALUE));
        	} 
        	else {
        		//Si assegna ad ogni nome una frequenza scelta casualmente
        		wordFrequencies.add(new WordFrequency(name, RANDOM.nextInt(100) + 1));
        	}
        }
        
        return wordFrequencies;
    }
	
	
	/**
	 * 
	 * @return the dimensione
	 */
	public Dimension getDimensione() {
		return dimensione;
	}

	
	/**
	 * 
	 * @return dimensione the dimensione to set
	 */
	public void setDimensione(Dimension dimensione) {
		this.dimensione = dimensione;
	}
	
	
	static private List<String> getWordNames() throws IOException {
        return IOUtils.readLines(getInputStream(getInputFile()));
    }
	
	
	/**
	 * @return the percorsoImmagine
	 */
	public String getPercorsoImmagine() {
		return percorsoImmagine;
	}


	/**
	 * @param percorsoImmagine the percorsoImmagine to set
	 */
	public void setPercorsoImmagine(String percorsoImmagine) {
		this.percorsoImmagine = percorsoImmagine;
	}


	/**
	 * @param tipologia the tipologia to set
	 */
	public void setTipologia(String tipologia) {
		this.tipologia = tipologia;
	}


	private String analyzeParolaChiave() 
	{
		if (getParolaChiave().equals("Brexit"))
			this.parolaChiave = getParolaChiave().toUpperCase();
		else if (StringUtils.isAllLowerCase(getParolaChiave()))
		{
			StopWordRemover stopword = new StopWordRemover("resources/stopwords_en.txt");
			this.parolaChiave = stopword.capitalizeFirstLetter(getParolaChiave());
		}
			
		return parolaChiave;
	}
	
	
	/**
	 * Verifica il valore di percorsoImmagine.
	 * @return il valore booleano associato
	 */
	private boolean checkPercorsoImmagine()
	{
		return (percorsoImmagine != null || !percorsoImmagine.equals("")) ? true : false; 
	}
	
}

