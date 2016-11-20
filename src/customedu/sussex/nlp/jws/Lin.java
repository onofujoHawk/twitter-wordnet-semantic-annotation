// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 15.19.55
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Lin.java

package customedu.sussex.nlp.jws;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.WordnetStemmer;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

// Referenced classes of package edu.sussex.nlp.jws:
//            ICFinder

public class Lin
{

    public Lin(IDictionary idictionary, ICFinder icfinder1)
    {
        dict = null;
        icfinder = null;
        editor = null;
        formatter = new DecimalFormat("0.0000");
        //System.out.println("... Lin");
        dict = idictionary;
        icfinder = icfinder1;
        stemmer = new WordnetStemmer(idictionary);
    }

    public double lin(String s, int i, String s1, int j, String s2)
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
        double d1 = icfinder.getIC((new StringBuilder()).append("").append(isynset.getOffset()).toString(), s2);
        double d2 = icfinder.getIC((new StringBuilder()).append("").append(isynset1.getOffset()).toString(), s2);
        if(d1 == 0.0D || d2 == 0.0D)
            return 0.0D;
        ISynset isynset2 = getLCS(isynset, isynset1, s2);
        double d3 = 0.0D;
        if(isynset2 == null)
            d3 = icfinder.getIC(null, s2);
        else
            d3 = icfinder.getIC((new StringBuilder()).append("").append(isynset2.getOffset()).toString(), s2);
        d = (2D * d3) / (d1 + d2);
        return d;
    }
    
    public double lin_custom_disambiguate(String s, int i, String s1, int j, String s2,String s7)
    {
        double d = 0.0D;
        IIndexWord iindexword = null;
        IIndexWord iindexword1 = null;
        if(s2.equalsIgnoreCase("n"))
        {
            iindexword = dict.getIndexWord(s, POS.NOUN);            
        }
        if(s2.equalsIgnoreCase("v"))
        {
            iindexword = dict.getIndexWord(s, POS.VERB);         
        }
        if(s7.equalsIgnoreCase("n"))
        {            
            iindexword1 = dict.getIndexWord(s1, POS.NOUN);
        }
        if(s7.equalsIgnoreCase("v"))
        {            
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
        double d1 = icfinder.getIC((new StringBuilder()).append("").append(isynset.getOffset()).toString(), s2);
        double d2 = icfinder.getIC((new StringBuilder()).append("").append(isynset1.getOffset()).toString(), s7);
        if(d1 == 0.0D || d2 == 0.0D)
            return 0.0D;
        //ISynset isynset2 = getLCS(isynset, isynset1, s2);
        ISynset isynset2 = getLCS_custom(isynset, isynset1, s2,s7);
        double d3 = 0.0D;
        double d4 = 0.0D;
        if(isynset2 == null){
            d3 = icfinder.getIC(null, s2);
            d4 = icfinder.getIC(null, s7);
            if (d4 > d3)
            	d3 = d4;
        }
        else{
            d3 = icfinder.getIC((new StringBuilder()).append("").append(isynset2.getOffset()).toString(), s2);
            d4 = icfinder.getIC((new StringBuilder()).append("").append(isynset2.getOffset()).toString(), s7);
            if (d4 > d3)
            	d3 = d4;
        }
        d = (2D * d3) / (d1 + d2);
        return d;
    }
    
/***********************************************************/
    public double lin_custom(String s, int i, String s1, int j, String s2,String s3)
    {
        double d = 0.0D;
        IIndexWord iindexword = null;
        IIndexWord iindexword1 = null;
        if(s2.equalsIgnoreCase("n"))
        {
            iindexword = dict.getIndexWord(s, POS.NOUN);            
        }
        if(s2.equalsIgnoreCase("v"))
        {
            iindexword = dict.getIndexWord(s, POS.VERB);         
        }
        /****************************************************/
        if(s3.equalsIgnoreCase("n"))
        {            
            iindexword1 = dict.getIndexWord(s1, POS.NOUN);
        }
        if(s3.equalsIgnoreCase("v"))
        {            
            iindexword1 = dict.getIndexWord(s1, POS.VERB);
        }
        /****************************************************/
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
        double d1 = icfinder.getIC((new StringBuilder()).append("").append(isynset.getOffset()).toString(), s2);
        double d2 = icfinder.getIC((new StringBuilder()).append("").append(isynset1.getOffset()).toString(), s2);
        if(d1 == 0.0D || d2 == 0.0D)
            return 0.0D;
        ISynset isynset2 = getLCS(isynset, isynset1, s2);
        double d3 = 0.0D;
        if(isynset2 == null)
            d3 = icfinder.getIC(null, s2);
        else
            d3 = icfinder.getIC((new StringBuilder()).append("").append(isynset2.getOffset()).toString(), s2);
        d = (2D * d3) / (d1 + d2);
        return d;
    }
    /************************************************/
    public TreeMap lin(String s, String s1, String s2)
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
                    double d = lin(s, i, s1, j, s2);
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

    public TreeMap lin(String s, String s1, int i, String s2)
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
                double d = lin(s, j, s1, i, s2);
                treemap.put((new StringBuilder()).append(s).append("#").append(s2).append("#").append(j).append(",").append(s1).append("#").append(s2).append("#").append(i).toString(), Double.valueOf(d));
                j++;
            }

        } else
        {
            return treemap;
        }
        return treemap;
    }

    public TreeMap lin(String s, int i, String s1, String s2)
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
                double d = lin(s, i, s1, j, s2);
                treemap.put((new StringBuilder()).append(s).append("#").append(s2).append("#").append(i).append(",").append(s1).append("#").append(s2).append("#").append(j).toString(), Double.valueOf(d));
                j++;
            }

        } else
        {
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
/****************************************************************/
    public ISynset getLCS_custom(ISynset isynset, ISynset isynset1, String s,String s2)
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
            double d2 = icfinder.getIC((new StringBuilder()).append("").append(isynsetid1.getOffset()).toString(), s2);
            if (d2 > d1)
            	d1 = d2;
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
        TreeMap treemap = lin(s, s1, s2);
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
    	String most_sense="0";
    	double [] result= new double [2];
        double d = 0.0D;
        TreeMap treemap = lin(s, s1, s2);
        Iterator iterator = treemap.keySet().iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            String s3 = (String)iterator.next();
            //System.out.println("s3:"+s3+" ");
            //System.out.println("treemap.get(s3):"+treemap.get(s3)+" ");
            double d1 = ((Double)treemap.get(s3)).doubleValue();
            if(d1 > d){
                d = d1;
	            int pos_virg = s3.indexOf(",");
	            most_sense = s3.substring(pos_virg-1, pos_virg);
            }
        } while(true);
        result [0]= d;
        result [1]= (int) Integer.parseInt(most_sense);               
        return result;
    }

    private IDictionary dict;
    private ICFinder icfinder;
    private String editor[];
    private NumberFormat formatter;
    public WordnetStemmer stemmer;
}