// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 15.19.05
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   JWSRandom.java

package customedu.sussex.nlp.jws;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import java.io.PrintStream;
import java.util.*;

public class JWSRandom
{

    public JWSRandom(IDictionary idictionary)
    {
        dict = null;
        fixed = false;
        rand = null;
        store = null;
        key = "";
        max = 0.0D;
        dict = idictionary;
        rand = new Random();
        fixed = false;
        max = 0.0D;
        System.out.println("... JWSRandom");
    }

    public JWSRandom(IDictionary idictionary, boolean flag)
    {
        dict = null;
        fixed = false;
        rand = null;
        store = null;
        key = "";
        max = 0.0D;
        dict = idictionary;
        fixed = flag;
        rand = new Random();
        if(flag)
            store = new Hashtable();
        max = 0.0D;
        System.out.println("... JWSRandom");
    }

    public JWSRandom(IDictionary idictionary, boolean flag, double d)
    {
        dict = null;
        fixed = false;
        rand = null;
        store = null;
        key = "";
        max = 0.0D;
        dict = idictionary;
        fixed = flag;
        max = d;
        rand = new Random();
        if(flag)
            store = new Hashtable();
        System.out.println("... JWSRandom");
    }

    public double random(String s, int i, String s1, int j, String s2)
    {
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
        List list = iindexword.getWordIDs();
        List list1 = iindexword1.getWordIDs();
        if(i > list.size())
        {
            System.out.println((new StringBuilder()).append(s).append(" sense: ").append(i).append(" not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        if(j > list1.size())
        {
            System.out.println((new StringBuilder()).append(s1).append(" sense: ").append(j).append(" not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        double d = 0.0D;
        key = (new StringBuilder()).append(s).append("#").append(s2).append("#").append(i).append(",").append(s1).append("#").append(s2).append("#").append(j).toString();
        if(fixed)
        {
            if(store.containsKey(key))
                return ((Double)store.get(key)).doubleValue();
            double d1 = rand.nextDouble();
            if(max > 0.0D)
                d1 = max / (1.0D / d1);
            store.put(key, Double.valueOf(d1));
            return d1;
        }
        if(max > 0.0D)
            return max / (1.0D / rand.nextDouble());
        else
            return rand.nextDouble();
    }

    public TreeMap random(String s, String s1, String s2)
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
        if(iindexword != null && iindexword1 != null)
        {
            List list = iindexword.getWordIDs();
            List list1 = iindexword1.getWordIDs();
            int i = 1;
            Object obj = null;
            Object obj1 = null;
            for(Iterator iterator = list.iterator(); iterator.hasNext();)
            {
                IWordID iwordid = (IWordID)iterator.next();
                int j = 1;
                for(Iterator iterator1 = list1.iterator(); iterator1.hasNext();)
                {
                    IWordID iwordid1 = (IWordID)iterator1.next();
                    double d = random(s, i, s1, j, s2);
                    treemap.put((new StringBuilder()).append(s).append("#").append(s2).append("#").append(i).append(",").append(s1).append("#").append(s2).append("#").append(j).toString(), Double.valueOf(d));
                    j++;
                }

                i++;
            }

        } else
        {
            return treemap;
        }
        return treemap;
    }

    public TreeMap random(String s, String s1, int i, String s2)
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
        if(iindexword != null && iindexword1 != null)
        {
            List list = iindexword.getWordIDs();
            int j = 1;
            for(Iterator iterator = list.iterator(); iterator.hasNext();)
            {
                IWordID iwordid = (IWordID)iterator.next();
                double d = random(s, j, s1, i, s2);
                treemap.put((new StringBuilder()).append(s).append("#").append(s2).append("#").append(j).append(",").append(s1).append("#").append(s2).append("#").append(i).toString(), Double.valueOf(d));
                j++;
            }

        } else
        {
            return treemap;
        }
        return treemap;
    }

    public TreeMap random(String s, int i, String s1, String s2)
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
        if(iindexword != null && iindexword1 != null)
        {
            List list = iindexword1.getWordIDs();
            int j = 1;
            for(Iterator iterator = list.iterator(); iterator.hasNext();)
            {
                IWordID iwordid = (IWordID)iterator.next();
                double d = random(s, i, s1, j, s2);
                treemap.put((new StringBuilder()).append(s).append("#").append(s2).append("#").append(i).append(",").append(s1).append("#").append(s2).append("#").append(j).toString(), Double.valueOf(d));
                j++;
            }

        } else
        {
            return treemap;
        }
        return treemap;
    }

    public double max(String s, String s1, String s2)
    {
        double d = 0.0D;
        TreeMap treemap = random(s, s1, s2);
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

    private IDictionary dict;
    private boolean fixed;
    private Random rand;
    private Hashtable store;
    private String key;
    private double max;
}