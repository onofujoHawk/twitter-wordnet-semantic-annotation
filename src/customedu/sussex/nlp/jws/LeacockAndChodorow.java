// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 15.19.19
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   LeacockAndChodorow.java

package customedu.sussex.nlp.jws;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import java.io.PrintStream;
import java.util.*;

public class LeacockAndChodorow
{

    public LeacockAndChodorow(IDictionary idictionary, ArrayList arraylist)
    {
        dict = null;
        noundepth = 0.0D;
        verbdepth = 0.0D;
        roots = null;
        System.out.println("... LeacockAndChodorow");
        System.out.println("... calculating depths of <roots> ...");
        dict = idictionary;
        roots = arraylist;
        String s = idictionary.getVersion().toString();
        if(s.equals("3.0"))
        {
            noundepth = 19D;
            verbdepth = 12D;
        }
        if(s.equals("2.1"))
        {
            noundepth = 18D;
            verbdepth = 12D;
        }
        if(s.equals("2.0"))
        {
            noundepth = 17D;
            verbdepth = 12D;
        }
        if(s.equals("1.7.1"))
        {
            noundepth = 17D;
            verbdepth = 11D;
        }
        if(s.equals("1.7"))
        {
            noundepth = 15D;
            verbdepth = 11D;
        }
        if(s.equals("1.6"))
        {
            noundepth = 15D;
            verbdepth = 11D;
        }
        noundepth++;
        verbdepth += 2D;
    }

    public double lch(String s, int i, String s1, int j, String s2)
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
        double d1 = 0.0D;
        if(isynset.equals(isynset1))
            d1 = 1.0D;
        else
            d1 = getShortestPath(isynset, isynset1);
        double d2 = 0.0D;
        if(s2.equalsIgnoreCase("n"))
            d2 = noundepth;
        if(s2.equalsIgnoreCase("v"))
            d2 = verbdepth;
        d = -Math.log(d1 / (2D * d2));
        return d;
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

    public TreeMap lch(String s, String s1, String s2)
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
                    double d = lch(s, i, s1, j, s2);
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

    public TreeMap lch(String s, String s1, int i, String s2)
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
        if(iindexword != null && iindexword1 != null)
        {
            List list = iindexword.getWordIDs();
            int j = 1;
            for(Iterator iterator = list.iterator(); iterator.hasNext();)
            {
                IWordID iwordid = (IWordID)iterator.next();
                double d = lch(s, j, s1, i, s2);
                treemap.put((new StringBuilder()).append(s).append("#").append(s2).append("#").append(j).append(",").append(s1).append("#").append(s2).append("#").append(i).toString(), Double.valueOf(d));
                j++;
            }

        } else
        {
            return treemap;
        }
        return treemap;
    }

    public TreeMap lch(String s, int i, String s1, String s2)
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
        if(iindexword != null && iindexword1 != null)
        {
            List list = iindexword1.getWordIDs();
            int j = 1;
            for(Iterator iterator = list.iterator(); iterator.hasNext();)
            {
                IWordID iwordid = (IWordID)iterator.next();
                double d = lch(s, i, s1, j, s2);
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
        TreeMap treemap = lch(s, s1, s2);
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

    private final double n30 = 19D;
    private final double v30 = 12D;
    private final double n21 = 18D;
    private final double v21 = 12D;
    private final double n20 = 17D;
    private final double v20 = 12D;
    private final double n171 = 17D;
    private final double v171 = 11D;
    private final double n17 = 15D;
    private final double v17 = 11D;
    private final double n16 = 15D;
    private final double v16 = 11D;
    private IDictionary dict;
    private double noundepth;
    private double verbdepth;
    private ArrayList roots;
}