// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 15.18.48
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   JiangAndConrath.java

package customedu.sussex.nlp.jws;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Referenced classes of package edu.sussex.nlp.jws:
//            CompoundWords, ICFinder

public class JiangAndConrath
{

    public JiangAndConrath(IDictionary idictionary, ICFinder icfinder1)
    {
        dict = null;
        icfinder = null;
        editor = null;
        formatter = new DecimalFormat("0.0000");
        compounds = null;
        cp = null;
        cm = null;
        System.out.println("... JiangAndConrath");
        dict = idictionary;
        icfinder = icfinder1;
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

    public double jcn(String s, int i, String s1, int j, String s2)
    {
        double d = 0.0D;
        IIndexWord iindexword = getWordNetWord(s, s2);
        IIndexWord iindexword1 = getWordNetWord(s1, s2);
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
        IWordID iwordid = (IWordID)iindexword.getWordIDs().get(i - 1);
        ISynset isynset = dict.getWord(iwordid).getSynset();
        IWordID iwordid1 = (IWordID)iindexword1.getWordIDs().get(j - 1);
        ISynset isynset1 = dict.getWord(iwordid1).getSynset();
        double d3 = icfinder.getIC((new StringBuilder()).append("").append(isynset.getOffset()).toString(), s2);
        double d4 = icfinder.getIC((new StringBuilder()).append("").append(isynset1.getOffset()).toString(), s2);
        if(d3 == 0.0D || d4 == 0.0D)
            return 0.0D;
        ISynset isynset2 = getLCS(isynset, isynset1, s2);
        double d5 = 0.0D;
        if(isynset2 == null)
            d5 = icfinder.getIC(null, s2);
        else
            d5 = icfinder.getIC((new StringBuilder()).append("").append(isynset2.getOffset()).toString(), s2);
        if(d3 == 0.0D && d4 == 0.0D && d5 == 0.0D)
            return 0.0D;
        if(isynset.equals(isynset1) && isynset1.equals(isynset2) || d3 + d4 == 2D * d5)
        {
            double d6 = icfinder.getRootSum(s2);
            double d1 = 1.0D / -Math.log((d6 - 0.01D) / d6);
            return d1;
        } else
        {
            double d2 = 1.0D / ((d3 + d4) - 2D * d5);
            return d2;
        }
    }

    public TreeMap jcn(String s, String s1, String s2)
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
                    double d = jcn(s, i, s1, j, s2);
                    treemap.put((new StringBuilder()).append(iindexword.getLemma()).append("#").append(s2).append("#").append(i).append(",").append(iindexword1.getLemma()).append("#").append(s2).append("#").append(j).toString(), Double.valueOf(d));
                    j++;
                }

                i++;
            }

        } else
        {
            System.out.println((new StringBuilder()).append(s).append(" and/or ").append(s1).append(" in POS ").append(s2).append(" do not exist in WordNet ").append(dict.getVersion()).toString());
            return treemap;
        }
        return treemap;
    }

    public TreeMap jcn(String s, String s1, int i, String s2)
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
                double d = jcn(s, j, s1, i, s2);
                treemap.put((new StringBuilder()).append(s).append("#").append(s2).append("#").append(j).append(",").append(s1).append("#").append(s2).append("#").append(i).toString(), Double.valueOf(d));
                j++;
            }

        } else
        {
            System.out.println((new StringBuilder()).append(iindexword.getLemma()).append(" and/or ").append(iindexword1.getLemma()).append(" in POS ").append(s2).append(" do not exist in WordNet ").append(dict.getVersion()).toString());
            return treemap;
        }
        return treemap;
    }

    public TreeMap jcn(String s, int i, String s1, String s2)
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
                double d = jcn(s, i, s1, j, s2);
                treemap.put((new StringBuilder()).append(iindexword.getLemma()).append("#").append(s2).append("#").append(i).append(",").append(iindexword1.getLemma()).append("#").append(s2).append("#").append(j).toString(), Double.valueOf(d));
                j++;
            }

        } else
        {
            System.out.println((new StringBuilder()).append(s).append(" and/or ").append(s1).append(" in POS ").append(s2).append(" do not exist in WordNet ").append(dict.getVersion()).toString());
            return treemap;
        }
        return treemap;
    }

    public ISynset getLCS(ISynset isynset, ISynset isynset1, String s)
    {
        HashSet hashset = new HashSet();
        hashset.add(isynset.getID());
        HashSet hashset1 = new HashSet();
        getHypernyms(hashset, hashset1);
        hashset1.add(isynset.getID());
        HashSet hashset2 = new HashSet();
        hashset2.add(isynset1.getID());
        HashSet hashset3 = new HashSet();
        getHypernyms(hashset2, hashset3);
        hashset3.add(isynset1.getID());
        hashset1.retainAll(hashset3);
        if(hashset1.isEmpty())
            return null;
        double d = -1.7976931348623157E+308D;
        ISynsetID isynsetid = null;
        Iterator iterator = hashset1.iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            ISynsetID isynsetid1 = (ISynsetID)iterator.next();
            double d1 = icfinder.getIC((new StringBuilder()).append("").append(isynsetid1.getOffset()).toString(), s);
            if(d1 > d)
            {
                d = d1;
                isynsetid = isynsetid1;
            }
        } while(true);
        return dict.getSynset(isynsetid);
    }

    private void getHypernyms(HashSet hashset, HashSet hashset1)
    {
        HashSet hashset2 = new HashSet();
        ISynset isynset;
        for(Iterator iterator = hashset.iterator(); iterator.hasNext(); hashset2.addAll(isynset.getRelatedSynsets(Pointer.HYPERNYM_INSTANCE)))
        {
            ISynsetID isynsetid = (ISynsetID)iterator.next();
            isynset = dict.getSynset(isynsetid);
            hashset2.addAll(isynset.getRelatedSynsets(Pointer.HYPERNYM));
        }

        if(!hashset2.isEmpty())
        {
            hashset1.addAll(hashset2);
            getHypernyms(hashset2, hashset1);
        }
    }

    public double max(String s, String s1, String s2)
    {
        double d = 0.0D;
        TreeMap treemap = jcn(s, s1, s2);
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
    private ICFinder icfinder;
    private String editor[];
    private NumberFormat formatter;
    private CompoundWords compounds;
    private Pattern cp;
    private Matcher cm;
}