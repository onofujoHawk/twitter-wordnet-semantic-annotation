// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 12.18.55
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   AdaptedLeskTanimoto.java

package customedu.sussex.nlp.jws;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.WordnetStemmer;

import java.io.FileInputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.didion.jwnl.JWNL;

public class AdaptedLeskTanimoto
{

    public AdaptedLeskTanimoto(IDictionary idictionary)
    {
        dict = null;
        formatter = new DecimalFormat("0.0000");
        p = null;
        m = null;
        stemmer = null;
        stoplist = null;
        list = "a aboard about above across after against all along alongside although amid amidst among amongst an and another anti any anybody anyone anything around as astride at aught bar barring because before behind below beneath beside besides between beyond both but by circa concerning considering despite down during each either enough everybody everyone except excepting excluding few fewer following for from he her hers herself him himself his hisself i idem if ilk in including inside into it its itself like many me mine minus more most myself naught near neither nobody none nor nothing notwithstanding of off on oneself onto opposite or other otherwise our ourself ourselves outside over own past pending per plus regarding round save self several she since so some somebody someone something somewhat such suchlike sundry than that the thee theirs them themselves there they thine this thou though through throughout thyself till to tother toward towards twain under underneath unless unlike until up upon us various versus via vis-a-vis we what whatall whatever whatsoever when whereas wherewith wherewithal which whichever whichsoever while who whoever whom whomever whomso whomsoever whose whosoever with within without worth ye yet yon yonder you you-all yours yourself";
        System.out.println("... Adapted Lesk (1)");
        dict = idictionary;
        p = Pattern.compile("[a-zA-Z-_]+");
        stemmer = new WordnetStemmer(idictionary);
        stoplist = new ArrayList();
        getStopWords();
    }

    public double lesk(String s, int i, String s1, int j, String s2)
    {
        double d = 0.0D;
        IIndexWord iindexword = null;
        IIndexWord iindexword1 = null;
        if(s2.equalsIgnoreCase("n"))
        {
            iindexword = dict.getIndexWord(s, POS.NOUN);
            iindexword1 = dict.getIndexWord(s1, POS.NOUN);
        }
        if(s2.equalsIgnoreCase("v"))
        {
            iindexword = dict.getIndexWord(s, POS.VERB);
            iindexword1 = dict.getIndexWord(s1, POS.VERB);
        }
        if(s2.equalsIgnoreCase("a"))
        {
            iindexword = dict.getIndexWord(s, POS.ADJECTIVE);
            iindexword1 = dict.getIndexWord(s1, POS.ADJECTIVE);
        }
        if(s2.equalsIgnoreCase("r"))
        {
            iindexword = dict.getIndexWord(s, POS.ADVERB);
            iindexword1 = dict.getIndexWord(s1, POS.ADVERB);
        }
        if(iindexword == null)
        {
            System.out.println((new StringBuilder()).append(s).append("(").append(s2).append(") not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        if(iindexword1 == null)
        {
            System.out.println((new StringBuilder()).append(s1).append("(").append(s2).append(") not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        List list1 = iindexword.getWordIDs();
        List list2 = iindexword1.getWordIDs();
        if(i > list1.size())
        {
            System.out.println((new StringBuilder()).append(s).append(" sense: ").append(i).append(" not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        if(j > list2.size())
        {
            System.out.println((new StringBuilder()).append(s1).append(" sense: ").append(j).append(" not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        } else
        {
            IWordID iwordid = (IWordID)iindexword.getWordIDs().get(i - 1);
            ISynset isynset = dict.getWord(iwordid).getSynset();
            IWordID iwordid1 = (IWordID)iindexword1.getWordIDs().get(j - 1);
            ISynset isynset1 = dict.getWord(iwordid1).getSynset();
            HashSet hashset = new HashSet();
            hashset.add(isynset.getID());
            hashset.addAll(getPointers(isynset));
            HashSet hashset1 = new HashSet();
            hashset1.add(isynset1.getID());
            hashset1.addAll(getPointers(isynset1));
            Hashtable hashtable = getSuperGloss(hashset);
            Hashtable hashtable1 = getSuperGloss(hashset1);
            HashSet hashset2 = new HashSet();
            hashset2.addAll(hashtable.keySet());
            hashset2.addAll(hashtable1.keySet());
            Vector vector = getVector(hashset2, hashtable);
            Vector vector1 = getVector(hashset2, hashtable1);
            double d1 = jaccard_tanimoto(vector, vector1);
            return d1;
        }
    }

    private Vector getVector(HashSet hashset, Hashtable hashtable)
    {
        Vector vector = new Vector();
        for(Iterator iterator = hashset.iterator(); iterator.hasNext();)
        {
            String s = (String)iterator.next();
            if(hashtable.containsKey(s))
                vector.add(Double.valueOf(((Integer)hashtable.get(s)).intValue()));
            else
                vector.add(Double.valueOf(0.0D));
        }

        return vector;
    }

    private double dot_product(Vector vector, Vector vector1)
    {
        double d = 0.0D;
        double d1 = 0.0D;
        double d3 = 0.0D;
        for(int i = 0; i < vector.size(); i++)
        {
            double d2 = ((Double)vector.get(i)).doubleValue();
            double d4 = ((Double)vector1.get(i)).doubleValue();
            if(d2 > 0.0D && d4 > 0.0D)
                d += d2 * d4;
        }

        return d;
    }

    private double lengthOfVector(Vector vector)
    {
        double d = 0.0D;
        for(int i = 0; i < vector.size(); i++)
        {
            double d1 = ((Double)vector.get(i)).doubleValue();
            if(d1 > 0.0D)
                d += d1 * d1;
        }

        if(d == 0.0D)
            return 0.0D;
        else
            return Math.sqrt(d);
    }

    public double jaccard_tanimoto(Vector vector, Vector vector1)
    {
        double d = dot_product(vector, vector1);
        double d1 = Math.pow(lengthOfVector(vector), 2D);
        double d2 = Math.pow(lengthOfVector(vector1), 2D);
        if(d == 0.0D)
            return 0.0D;
        if((d1 + d2) - d == 0.0D)
            return 0.0D;
        else
            return d / ((d1 + d2) - d);
    }

    private Hashtable getSuperGloss(HashSet hashset)
    {
        Hashtable hashtable = new Hashtable();
        for(Iterator iterator = hashset.iterator(); iterator.hasNext();)
        {
            ISynsetID isynsetid = (ISynsetID)iterator.next();
            String s = dict.getSynset(isynsetid).getGloss();
            m = p.matcher(s);
            while(m.find()) 
            {
                String s1 = m.group().trim();
                if(!stoplist.contains(s1))
                {
                    List list1 = stemmer.findStems(s1);
                    if(!list1.isEmpty())
                        if(list1.contains(s1))
                        {
                            if(hashtable.containsKey(s1))
                            {
                                int i = ((Integer)hashtable.get(s1)).intValue();
                                i++;
                                hashtable.put(s1, Integer.valueOf(i));
                            } else
                            {
                                hashtable.put(s1, Integer.valueOf(1));
                            }
                        } else
                        {
                            Iterator iterator1 = list1.iterator();
                            while(iterator1.hasNext()) 
                            {
                                String s2 = (String)iterator1.next();
                                if(hashtable.containsKey(s2))
                                {
                                    int j = ((Integer)hashtable.get(s2)).intValue();
                                    j++;
                                    hashtable.put(s2, Integer.valueOf(j));
                                } else
                                {
                                    hashtable.put(s2, Integer.valueOf(1));
                                }
                            }
                        }
                }
            }
        }

        return hashtable;
    }

    private HashSet getPointers(ISynset isynset)
    {
        HashSet hashset = new HashSet();
        hashset.addAll(isynset.getRelatedSynsets());
        Map map = isynset.getRelatedMap();
        IPointer ipointer;
        for(Iterator iterator = map.keySet().iterator(); iterator.hasNext(); hashset.addAll((Collection)map.get(ipointer)))
        {
            ipointer = (IPointer)iterator.next();
            if(!ipointer.equals(Pointer.HYPERNYM) && !ipointer.equals(Pointer.HYPERNYM_INSTANCE))
                continue;
            List list1 = (List)map.get(ipointer);
            ISynsetID isynsetid;
            for(Iterator iterator1 = list1.iterator(); iterator1.hasNext(); hashset.addAll(dict.getSynset(isynsetid).getRelatedSynsets(Pointer.HYPONYM_INSTANCE)))
            {
                isynsetid = (ISynsetID)iterator1.next();
                hashset.addAll(dict.getSynset(isynsetid).getRelatedSynsets(Pointer.HYPONYM));
            }

        }

        return hashset;
    }

    private void getStopWords()
    {
        String as[] = list.split("\\s");
        for(int i = 0; i < as.length; i++)
            stoplist.add(as[i]);

    }

    public TreeMap lesk(String s, String s1, String s2)
    {
        TreeMap treemap = new TreeMap();
        IIndexWord iindexword = null;
        IIndexWord iindexword1 = null;
        if(s2.equalsIgnoreCase("n"))
        {
            iindexword = dict.getIndexWord(s, POS.NOUN);
            iindexword1 = dict.getIndexWord(s1, POS.NOUN);
        }
        if(s2.equalsIgnoreCase("v"))
        {
            iindexword = dict.getIndexWord(s, POS.VERB);
            iindexword1 = dict.getIndexWord(s1, POS.VERB);
        }
        if(s2.equalsIgnoreCase("a"))
        {
            iindexword = dict.getIndexWord(s, POS.ADJECTIVE);
            iindexword1 = dict.getIndexWord(s1, POS.ADJECTIVE);
        }
        if(s2.equalsIgnoreCase("r"))
        {
            iindexword = dict.getIndexWord(s, POS.ADVERB);
            iindexword1 = dict.getIndexWord(s1, POS.ADVERB);
        }
        if(iindexword == null)
        {
            System.out.println((new StringBuilder()).append(s).append("(").append(s2).append(") not found in WordNet ").append(dict.getVersion()).toString());
            return treemap;
        }
        if(iindexword1 == null)
        {
            System.out.println((new StringBuilder()).append(s1).append("(").append(s2).append(") not found in WordNet ").append(dict.getVersion()).toString());
            return treemap;
        }
        List list1 = iindexword.getWordIDs();
        List list2 = iindexword1.getWordIDs();
        int i = 1;
        Object obj = null;
        Object obj1 = null;
        for(Iterator iterator = list1.iterator(); iterator.hasNext();)
        {
            IWordID iwordid = (IWordID)iterator.next();
            int j = 1;
            for(Iterator iterator1 = list2.iterator(); iterator1.hasNext();)
            {
                IWordID iwordid1 = (IWordID)iterator1.next();                
                double d = lesk(s, i, s1, j, s2);
                treemap.put((new StringBuilder()).append(s).append("#").append(s2).append("#").append(i).append(",").append(s1).append("#").append(s2).append("#").append(j).toString(), Double.valueOf(d));                
                j++;
            }

            i++;
        }

        return treemap;
    }

    public TreeMap lesk(String s, String s1, int i, String s2)
    {
        TreeMap treemap = new TreeMap();
        IIndexWord iindexword = null;
        IIndexWord iindexword1 = null;
        if(s2.equalsIgnoreCase("n"))
        {
            iindexword = dict.getIndexWord(s, POS.NOUN);
            iindexword1 = dict.getIndexWord(s1, POS.NOUN);
        }
        if(s2.equalsIgnoreCase("v"))
        {
            iindexword = dict.getIndexWord(s, POS.VERB);
            iindexword1 = dict.getIndexWord(s1, POS.VERB);
        }
        if(s2.equalsIgnoreCase("a"))
        {
            iindexword = dict.getIndexWord(s, POS.ADJECTIVE);
            iindexword1 = dict.getIndexWord(s1, POS.ADJECTIVE);
        }
        if(s2.equalsIgnoreCase("r"))
        {
            iindexword = dict.getIndexWord(s, POS.ADVERB);
            iindexword1 = dict.getIndexWord(s1, POS.ADVERB);
        }
        if(iindexword == null)
        {
            System.out.println((new StringBuilder()).append(s).append("(").append(s2).append(") not found in WordNet ").append(dict.getVersion()).toString());
            return treemap;
        }
        if(iindexword1 == null)
        {
            System.out.println((new StringBuilder()).append(s1).append("(").append(s2).append(") not found in WordNet ").append(dict.getVersion()).toString());
            return treemap;
        }
        List list1 = iindexword.getWordIDs();
        int j = 1;
        for(Iterator iterator = list1.iterator(); iterator.hasNext();)
        {
            IWordID iwordid = (IWordID)iterator.next();
            double d = lesk(s, j, s1, i, s2);
            treemap.put((new StringBuilder()).append(s).append("#").append(s2).append("#").append(j).append(",").append(s1).append("#").append(s2).append("#").append(i).toString(), Double.valueOf(d));
            j++;
        }

        return treemap;
    }

    public TreeMap lesk(String s, int i, String s1, String s2)
    {
        TreeMap treemap = new TreeMap();
        IIndexWord iindexword = null;
        IIndexWord iindexword1 = null;
        if(s2.equalsIgnoreCase("n"))
        {
            iindexword = dict.getIndexWord(s, POS.NOUN);
            iindexword1 = dict.getIndexWord(s1, POS.NOUN);
        }
        if(s2.equalsIgnoreCase("v"))
        {
            iindexword = dict.getIndexWord(s, POS.VERB);
            iindexword1 = dict.getIndexWord(s1, POS.VERB);
        }
        if(s2.equalsIgnoreCase("a"))
        {
            iindexword = dict.getIndexWord(s, POS.ADJECTIVE);
            iindexword1 = dict.getIndexWord(s1, POS.ADJECTIVE);
        }
        if(s2.equalsIgnoreCase("r"))
        {
            iindexword = dict.getIndexWord(s, POS.ADVERB);
            iindexword1 = dict.getIndexWord(s1, POS.ADVERB);
        }
        if(iindexword == null)
        {
            System.out.println((new StringBuilder()).append(s).append("(").append(s2).append(") not found in WordNet ").append(dict.getVersion()).toString());
            return treemap;
        }
        if(iindexword1 == null)
        {
            System.out.println((new StringBuilder()).append(s1).append("(").append(s2).append(") not found in WordNet ").append(dict.getVersion()).toString());
            return treemap;
        }
        List list1 = iindexword1.getWordIDs();
        int j = 1;
        for(Iterator iterator = list1.iterator(); iterator.hasNext();)
        {
            IWordID iwordid = (IWordID)iterator.next();
            double d = lesk(s, i, s1, j, s2);
            treemap.put((new StringBuilder()).append(s).append("#").append(s2).append("#").append(i).append(",").append(s1).append("#").append(s2).append("#").append(j).toString(), Double.valueOf(d));
            j++;
        }

        return treemap;
    }

    public double max(String s, String s1, String s2)
    {
        double d = 0.0D;        
        TreeMap treemap = lesk(s, s1, s2);
        Iterator iterator = treemap.keySet().iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            String s3 = (String)iterator.next();
            double d1 = ((Double)treemap.get(s3)).doubleValue();
            if(d1 > d)
                d = d1;                            
        } while(true);        
        return d;
    }

    public double[] max_custom(String s, String s1, String s2)
    {
        double d = 0.0D;
        double [] result= new double [2];
        String most_sense="0";
        TreeMap treemap = lesk(s, s1, s2);
        Iterator iterator = treemap.keySet().iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            String s3 = (String)iterator.next();
            //System.out.println("s3:"+s3+" ");
            //System.out.println("treemap.get(s3):"+treemap.get(s3)+" ");
            double d1 = ((Double)treemap.get(s3)).doubleValue();
            //System.out.println("d1:"+d1+"\n");
            //int pos_virg = s3.indexOf(",");
            //System.out.println("pos_virg:"+pos_virg+"\n");
            //String most_sense = s3.substring(pos_virg-1, pos_virg);
            //System.out.println("Senso trovato:"+most_sense+"\n");
            if(d1 > d){
                d = d1;
                int pos_virg = s3.indexOf(",");
                most_sense = s3.substring(pos_virg-1, pos_virg);
            }
        } while(true);        
        //System.out.println("d:"+d+"\n");
        //System.out.println("MostSense:"+most_sense+"\n");
        result [0]= d;
        result [1]= (int) Integer.parseInt(most_sense);               
        return result;
    }
    
    public static void main(String args[])
    {
        String s = "2.1";
        String s1 = (new StringBuilder()).append("C:/Program Files/WordNet/").append(s).append("/dict").toString();
        //String s2 = (new StringBuilder()).append("C:/Program Files/WordNet/").append(s).append("/WordNet-InfoContent-").append(s).append("/ic-semcor.dat").toString();
        double[] resultscores2n = new double [2];
        URL url = null;
        try
        {
            url = new URL("file", null, s1);
        }
        catch(MalformedURLException malformedurlexception)
        {
            malformedurlexception.printStackTrace();
        }
        if(url == null)
            return;
        Dictionary dictionary = new Dictionary(url);
        dictionary.open();
        DecimalFormat decimalformat = new DecimalFormat("0.0000");
        AdaptedLeskTanimoto adaptedlesktanimoto = new AdaptedLeskTanimoto(dictionary);
        List<String> wordRoots = null;
        List<String> wordRoots3 = null;
        wordRoots3 = adaptedlesktanimoto.stemmer.findStems("is");        
        wordRoots = adaptedlesktanimoto.stemmer.findStems("is", POS.NOUN);
        if (wordRoots3.isEmpty() == true)
        	System.out.println("wordRoots3: " + wordRoots3.isEmpty());
        if (wordRoots3 == null)
        	System.out.println("No word in WORDNET.\n");
        else
        	System.out.println("Root3:"+wordRoots3.get(0)+"\n");
        if (wordRoots.isEmpty() == true)
        	System.out.println("wordRoots: " + wordRoots.isEmpty());
        if (wordRoots == null) {
        	System.out.println("No word in WORDNET: " + "is");        	
        	} 
        else{
        	System.out.println("Yes word in WORDNET: " + "is" );
        	//for (int z = 0; z < wordRoots.size(); z++) {
        		System.out.println("Root:"+wordRoots.get(0)+"\n");      
        	//}        	
      	} 
        double d = adaptedlesktanimoto.lesk("cat", 1, "dog", 2, "n");        
        System.out.println("specific senses");
        if(d != 0.0D)
            System.out.println((new StringBuilder()).append("lesk:\t").append(decimalformat.format(d)).toString());
        System.out.println();
        TreeMap treemap = adaptedlesktanimoto.lesk("food", "chicken", "n");
        resultscores2n = adaptedlesktanimoto.max_custom("food", "chicken","n");
        System.out.println("distanza:"+resultscores2n[0]+"\n");
        System.out.println("Mostsense:"+resultscores2n[1]+"\n");
        
        System.out.println("all senses");
        String s3;
        for(Iterator iterator = treemap.keySet().iterator(); iterator.hasNext(); System.out.println((new StringBuilder()).append(s3).append("\t").append(decimalformat.format(treemap.get(s3))).toString()))
            s3 = (String)iterator.next();

        System.out.println();
        double d1 = adaptedlesktanimoto.max("cat", "dog", "n");
        System.out.println("max value");
        System.out.println(decimalformat.format(d1));
        System.out.println();
        TreeMap treemap1 = adaptedlesktanimoto.lesk("cat", "dog", 2, "n");
        System.out.println("all senses of word 1 vs. fixed sense of word 2");
        String s4;
        for(Iterator iterator1 = treemap1.keySet().iterator(); iterator1.hasNext(); System.out.println((new StringBuilder()).append(s4).append("\t").append(decimalformat.format(treemap1.get(s4))).toString()))
            s4 = (String)iterator1.next();

        System.out.println();
        TreeMap treemap2 = adaptedlesktanimoto.lesk("cat", 1, "dog", "n");
        System.out.println("fixed sense of word 1 vs. all senses of word 2");
        String s5;
        for(Iterator iterator2 = treemap2.keySet().iterator(); iterator2.hasNext(); System.out.println((new StringBuilder()).append(s5).append("\t").append(decimalformat.format(treemap2.get(s5))).toString()))
            s5 = (String)iterator2.next();

    }

    private IDictionary dict;
    private NumberFormat formatter;
    private Pattern p;
    private Matcher m;
    public WordnetStemmer stemmer;
    private ArrayList stoplist;
    private String list;
}