// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 15.20.10
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Path.java

package customedu.sussex.nlp.jws;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Referenced classes of package edu.sussex.nlp.jws:
//            CompoundWords

public class Path
{

    public Path(IDictionary idictionary, ArrayList arraylist)
    {
        dict = null;
        roots = null;
        compounds = null;
        cp = null;
        cm = null;
        System.out.println("... Path");
        dict = idictionary;
        roots = arraylist;
        compounds = new CompoundWords();
        cp = Pattern.compile("[-_\\s]");
    }

    private IIndexWord getWordNetWord(String s, String s1)
    {
        IIndexWord iindexword;
label0:
        {
            iindexword = null;
            POS pos = null;
            if(s1.equalsIgnoreCase("n"))
                pos = POS.NOUN;
            if(s1.equalsIgnoreCase("v"))
                pos = POS.VERB;
            cm = cp.matcher(s);
            if(cm.find())
            {
                ArrayList arraylist = compounds.getCompounds(s);
                Iterator iterator = arraylist.iterator();
                do
                {
                    if(!iterator.hasNext())
                        break label0;
                    String s2 = (String)iterator.next();
                    iindexword = dict.getIndexWord(s2, pos);
                } while(iindexword == null);
                return iindexword;
            }
            iindexword = dict.getIndexWord(s, pos);
        }
        return iindexword;
    }

    public double path(String s, int i, String s1, int j, String s2)
    {
        if(!s2.equalsIgnoreCase("n") && !s2.equalsIgnoreCase("v"))
        {
            System.out.println("error: Path is applicable to nouns (n) and verbs (v) only");
            return 0.0D;
        }
        double d = 0.0D;
        IIndexWord iindexword = getWordNetWord(s, s2);
        IIndexWord iindexword1 = getWordNetWord(s1, s2);
        if(iindexword == null || iindexword1 == null)
        {
            System.out.println((new StringBuilder()).append("error: WordNet does not contain word(s):\t(").append(s).append(" , ").append(s1).append(") in POS:").append(s2).toString());
            return 0.0D;
        }
        if(i > iindexword.getWordIDs().size() || j > iindexword1.getWordIDs().size())
        {
            System.out.println((new StringBuilder()).append("error: WordNet does not contain sense(s):\t(").append(i).append(" , ").append(j).append(")").toString());
            return 0.0D;
        } else
        {
            IWordID iwordid = (IWordID)iindexword.getWordIDs().get(i - 1);
            ISynset isynset = dict.getWord(iwordid).getSynset();
            IWordID iwordid1 = (IWordID)iindexword1.getWordIDs().get(j - 1);
            ISynset isynset1 = dict.getWord(iwordid1).getSynset();
            double d2 = getShortestPath(isynset, isynset1);
            double d1 = 1.0D / d2;
            return d1;
        }
    }

    private double getShortestPath(ISynset isynset, ISynset isynset1)
    {
        double d = 0.0D;
        ArrayList arraylist = new ArrayList();
        ISynsetID isynsetid = (ISynsetID)isynset.getID();
        ISynsetID isynsetid1 = (ISynsetID)isynset1.getID();
        if(isynsetid.equals(isynsetid1))
            return 1.0D;
        HashSet hashset = new HashSet();
        hashset.add(isynsetid);
        TreeMap treemap = new TreeMap();
        treemap.put(Double.valueOf(1.0D), hashset);
        getHypernyms(1.0D, isynsetid1, hashset, treemap);
        HashSet hashset1 = new HashSet();
        hashset1.add(isynsetid1);
        TreeMap treemap1 = new TreeMap();
        treemap1.put(Double.valueOf(1.0D), hashset1);
        getHypernyms(1.0D, isynsetid, hashset1, treemap1);
        for(Iterator iterator = treemap.keySet().iterator(); iterator.hasNext();)
        {
            Double double1 = (Double)iterator.next();
            HashSet hashset2 = new HashSet();
            hashset2.addAll((Collection)treemap.get(double1));
            if(hashset2.contains(isynsetid1))
                arraylist.add(double1);
            Iterator iterator1 = treemap1.keySet().iterator();
            while(iterator1.hasNext()) 
            {
                Double double2 = (Double)iterator1.next();
                HashSet hashset3 = new HashSet();
                hashset3.addAll((Collection)treemap1.get(double2));
                if(hashset3.contains(isynsetid))
                    arraylist.add(double2);
                hashset3.retainAll(hashset2);
                if(!hashset3.isEmpty())
                    arraylist.add(Double.valueOf((double1.doubleValue() + double2.doubleValue()) - 1.0D));
            }
        }

        if(arraylist.isEmpty())
        {
            double d1 = getShortestRoot(treemap);
            double d2 = getShortestRoot(treemap1);
            d = d1 + d2 + 1.0D;
        } else
        {
            Collections.sort(arraylist);
            d = ((Double)arraylist.get(0)).doubleValue();
        }
        return d;
    }

    private double getShortestRoot(TreeMap treemap)
    {
        double d = 0.0D;
        Iterator iterator = treemap.keySet().iterator();
        Double double1;
label0:
        do
            if(iterator.hasNext())
            {
                double1 = (Double)iterator.next();
                HashSet hashset = (HashSet)treemap.get(double1);
                Iterator iterator1 = hashset.iterator();
                ISynsetID isynsetid;
                do
                {
                    if(!iterator1.hasNext())
                        continue label0;
                    isynsetid = (ISynsetID)iterator1.next();
                } while(!roots.contains(isynsetid));
                break;
            } else
            {
                return d;
            }
        while(true);
        return double1.doubleValue();
    }

    private void getHypernyms(double d, ISynsetID isynsetid, HashSet hashset, TreeMap treemap)
    {
        d++;
        HashSet hashset1 = new HashSet();
        ISynset isynset;
        for(Iterator iterator = hashset.iterator(); iterator.hasNext(); hashset1.addAll(isynset.getRelatedSynsets(Pointer.HYPERNYM_INSTANCE)))
        {
            ISynsetID isynsetid1 = (ISynsetID)iterator.next();
            isynset = dict.getSynset(isynsetid1);
            hashset1.addAll(isynset.getRelatedSynsets(Pointer.HYPERNYM));
        }

        if(!hashset1.isEmpty())
        {
            if(hashset1.contains(isynsetid))
            {
                treemap.put(Double.valueOf(d), hashset1);
                return;
            }
            treemap.put(Double.valueOf(d), hashset1);
            getHypernyms(d, isynsetid, hashset1, treemap);
        }
    }

    public TreeMap path(String s, String s1, String s2)
    {
        TreeMap treemap = new TreeMap();
        IIndexWord iindexword = getWordNetWord(s, s2);
        IIndexWord iindexword1 = getWordNetWord(s1, s2);
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
                    double d = path(s, i, s1, j, s2);
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

    public TreeMap path(String s, String s1, int i, String s2)
    {
        TreeMap treemap = new TreeMap();
        IIndexWord iindexword = getWordNetWord(s, s2);
        IIndexWord iindexword1 = getWordNetWord(s1, s2);
        if(iindexword != null && iindexword1 != null)
        {
            List list = iindexword.getWordIDs();
            int j = 1;
            for(Iterator iterator = list.iterator(); iterator.hasNext();)
            {
                IWordID iwordid = (IWordID)iterator.next();
                double d = path(s, j, s1, i, s2);
                treemap.put((new StringBuilder()).append(s).append("#").append(s2).append("#").append(j).append(",").append(s1).append("#").append(s2).append("#").append(i).toString(), Double.valueOf(d));
                j++;
            }

        } else
        {
            return treemap;
        }
        return treemap;
    }

    public TreeMap path(String s, int i, String s1, String s2)
    {
        TreeMap treemap = new TreeMap();
        IIndexWord iindexword = getWordNetWord(s, s2);
        IIndexWord iindexword1 = getWordNetWord(s1, s2);
        if(iindexword != null && iindexword1 != null)
        {
            List list = iindexword1.getWordIDs();
            int j = 1;
            for(Iterator iterator = list.iterator(); iterator.hasNext();)
            {
                IWordID iwordid = (IWordID)iterator.next();
                double d = path(s, i, s1, j, s2);
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
        TreeMap treemap = path(s, s1, s2);
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
    private ArrayList roots;
    private CompoundWords compounds;
    private Pattern cp;
    private Matcher cm;
}