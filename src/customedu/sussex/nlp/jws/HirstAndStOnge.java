// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 15.18.12
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   HirstAndStOnge.java

package customedu.sussex.nlp.jws;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.WordnetStemmer;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

// Referenced classes of package edu.sussex.nlp.jws:
//            RelatedSynsets

public class HirstAndStOnge
{

    public HirstAndStOnge(IDictionary idictionary)
    {
    	stemmer = null;
        up = null;
        down = null;
        horizontal = null;
        all = null;
        dict = null;
        editor = null;
        formatter = new DecimalFormat("0.0000");
        pointers = null;
        cut1 = null;
        cut2 = null;
        System.out.println("... HirstAndStOnge");
        dict = idictionary;
        pointers = new RelatedSynsets(idictionary);
        up = new ArrayList();
        up.add(Pointer.HYPERNYM);
        up.add(Pointer.HYPERNYM_INSTANCE);
        up.add(Pointer.MERONYM_MEMBER);
        //up.add(Pointer.MERONYM_PART);
        //up.add(Pointer.MERONYM_SUBSTANCE);
        down = new ArrayList();
        down.add(Pointer.CAUSE);
        down.add(Pointer.ENTAILMENT);
        down.add(Pointer.HOLONYM_MEMBER);
        down.add(Pointer.HOLONYM_PART);
        down.add(Pointer.HOLONYM_SUBSTANCE);
        //down.add(Pointer.HYPONYM);
        //down.add(Pointer.HYPONYM_INSTANCE);
        horizontal = new ArrayList();
        horizontal.add(Pointer.ALSO_SEE);
        horizontal.add(Pointer.ANTONYM);
        horizontal.add(Pointer.ATTRIBUTE);
        //horizontal.add(Pointer.PERTAINYM);
        //horizontal.add(Pointer.SIMILAR_TO);
        all = new ArrayList();
        all.addAll(up);
        all.addAll(down);
        all.addAll(horizontal);
        stemmer = new WordnetStemmer(idictionary);
    }

    private boolean hasHorizontalRelation(ISynset isynset, ISynset isynset1)
    {
        ISynsetID isynsetid = (ISynsetID)isynset1.getID();
        Hashtable hashtable = pointers.getAllRelatedSynsetsAndTheirTypes(isynset);
        for(Iterator iterator = hashtable.keySet().iterator(); iterator.hasNext();)
        {
            ISynsetID isynsetid1 = (ISynsetID)iterator.next();
            if(isynsetid1.equals(isynsetid))
            {
                IPointer ipointer = (IPointer)hashtable.get(isynsetid1);
                if(horizontal.contains(ipointer))
                    return true;
            }
        }

        return false;
    }

    private boolean isCompound(IIndexWord iindexword, IIndexWord iindexword1)
    {
        String s = iindexword.getLemma();
        String s1 = iindexword1.getLemma();
        if(s.length() > s1.length())
        {
            if(s.indexOf((new StringBuilder()).append("_").append(s1).toString()) > 0 || s.indexOf((new StringBuilder()).append(s1).append("_").toString()) > 0)
                return true;
            if(s.indexOf((new StringBuilder()).append("-").append(s1).toString()) > 0 || s.indexOf((new StringBuilder()).append(s1).append("-").toString()) > 0)
                return true;
            if(s.indexOf((new StringBuilder()).append(" ").append(s1).toString()) > 0 || s.indexOf((new StringBuilder()).append(s1).append(" ").toString()) > 0)
                return true;
        }
        if(s.length() < s1.length())
        {
            if(s1.indexOf((new StringBuilder()).append("_").append(s).toString()) > 0 || s1.indexOf((new StringBuilder()).append(s).append("_").toString()) > 0)
                return true;
            if(s1.indexOf((new StringBuilder()).append("-").append(s).toString()) > 0 || s1.indexOf((new StringBuilder()).append(s).append("-").toString()) > 0)
                return true;
            if(s1.indexOf((new StringBuilder()).append(" ").append(s).toString()) > 0 || s1.indexOf((new StringBuilder()).append(s).append(" ").toString()) > 0)
                return true;
        }
        return false;
    }

    private boolean hasAnyRelation(ISynset isynset, ISynset isynset1)
    {
        ISynsetID isynsetid = (ISynsetID)isynset1.getID();
        Hashtable hashtable = pointers.getAllRelatedSynsetsAndTheirTypes(isynset);
        for(Iterator iterator = hashtable.keySet().iterator(); iterator.hasNext();)
        {
            ISynsetID isynsetid1 = (ISynsetID)iterator.next();
            if(isynsetid1.equals(isynsetid))
                return true;
        }

        return false;
    }

    public double hso(String s, int i, String s1, int j, String s2)
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
        if(iindexword == null){
        	System.out.println((new StringBuilder()).append(s).append("(").append(s2).append(") not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        if(iindexword1 == null){
        	System.out.println((new StringBuilder()).append(s1).append("(").append(s2).append(") not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        List list = iindexword.getWordIDs();
        List list1 = iindexword1.getWordIDs();
        if(i > list.size())
            return 0.0D;
        if(j > list1.size())
            return 0.0D;
        if(iindexword.equals(iindexword1))
            return 16D;
        IWordID iwordid = (IWordID)iindexword.getWordIDs().get(i - 1);
        ISynset isynset = dict.getWord(iwordid).getSynset();
        IWordID iwordid1 = (IWordID)iindexword1.getWordIDs().get(j - 1);
        ISynset isynset1 = dict.getWord(iwordid1).getSynset();
        if(isynset.equals(isynset1))
            return 16D;
        if(hasHorizontalRelation(isynset, isynset1))
            return 16D;
        if(isCompound(iindexword, iindexword1) && hasAnyRelation(isynset, isynset1))
            return 16D;
        HashSet hashset = new HashSet();
        ArrayList arraylist = new ArrayList();
        arraylist.add(isynset.getID());
        hashset.add(arraylist);
        ISynsetID isynsetid = (ISynsetID)isynset1.getID();
        ArrayList arraylist1 = new ArrayList();
        HashSet hashset1 = hashset;
        Object obj = hashset1.iterator();
        do
        {
            if(!((Iterator) (obj)).hasNext())
                break;
            ArrayList arraylist2 = (ArrayList)((Iterator) (obj)).next();
            if(arraylist2.contains(isynsetid))
                arraylist1.add(getClean(arraylist2));
        } while(true);
        obj = pathfinder(hashset1);
        Object obj1 = ((HashSet) (obj)).iterator();
        do
        {
            if(!((Iterator) (obj1)).hasNext())
                break;
            ArrayList arraylist3 = (ArrayList)((Iterator) (obj1)).next();
            if(arraylist3.contains(isynsetid))
                arraylist1.add(getClean(arraylist3));
        } while(true);
        obj1 = pathfinder(((HashSet) (obj)));
        Object obj2 = ((HashSet) (obj1)).iterator();
        do
        {
            if(!((Iterator) (obj2)).hasNext())
                break;
            ArrayList arraylist4 = (ArrayList)((Iterator) (obj2)).next();
            if(arraylist4.contains(isynsetid))
                arraylist1.add(getClean(arraylist4));
        } while(true);
        obj2 = pathfinder(((HashSet) (obj1)));
        Object obj3 = ((HashSet) (obj2)).iterator();
        do
        {
            if(!((Iterator) (obj3)).hasNext())
                break;
            ArrayList arraylist5 = (ArrayList)((Iterator) (obj3)).next();
            if(arraylist5.contains(isynsetid))
                arraylist1.add(getClean(arraylist5));
        } while(true);
        obj3 = pathfinder(((HashSet) (obj2)));
        Object obj4 = ((HashSet) (obj3)).iterator();
        do
        {
            if(!((Iterator) (obj4)).hasNext())
                break;
            ArrayList arraylist6 = (ArrayList)((Iterator) (obj4)).next();
            if(arraylist6.contains(isynsetid))
                arraylist1.add(getClean(arraylist6));
        } while(true);
        obj4 = pathfinder(((HashSet) (obj3)));
        Iterator iterator = ((HashSet) (obj4)).iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            ArrayList arraylist7 = (ArrayList)iterator.next();
            if(arraylist7.contains(isynsetid))
                arraylist1.add(getClean(arraylist7));
        } while(true);
        if(arraylist1.isEmpty())
            return 0.0D;
        double d1 = 0.0D;
        Iterator iterator1 = arraylist1.iterator();
        do
        {
            if(!iterator1.hasNext())
                break;
            ArrayList arraylist8 = (ArrayList)iterator1.next();
            double d2 = allowable(arraylist8);
            if(d2 != -1D)
            {
                double d3 = arraylist8.size();
                double d4 = 8D - d3 - 1.0D * d2;
                if(d4 > d1)
                    d1 = d4;
            }
        } while(true);
        d = d1;
        return d;
    }
/******************************************************************/
    
    public double hso_custom(String s, int i, String s1, int j, String s2, String s3)
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
        if(s2.equalsIgnoreCase("a"))
        {
            iindexword = dict.getIndexWord(s, POS.ADJECTIVE);            
        }
        if(s2.equalsIgnoreCase("r"))
        {
            iindexword = dict.getIndexWord(s, POS.ADVERB);            
        }
        /************************************/
        if(s3.equalsIgnoreCase("n"))
        {            
            iindexword1 = dict.getIndexWord(s1, POS.NOUN);
        }
        if(s3.equalsIgnoreCase("v"))
        {            
            iindexword1 = dict.getIndexWord(s1, POS.VERB);
        }
        if(s3.equalsIgnoreCase("a"))
        {            
            iindexword1 = dict.getIndexWord(s1, POS.ADJECTIVE);
        }
        if(s3.equalsIgnoreCase("r"))
        {            
            iindexword1 = dict.getIndexWord(s1, POS.ADVERB);
        }
        /************************************/
        if(iindexword == null){
        	System.out.println((new StringBuilder()).append(s).append("(").append(s2).append(") not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        if(iindexword1 == null){
        	System.out.println((new StringBuilder()).append(s1).append("(").append(s2).append(") not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        List list = iindexword.getWordIDs();
        List list1 = iindexword1.getWordIDs();
        if(i > list.size())
            return 0.0D;
        if(j > list1.size())
            return 0.0D;
        if(iindexword.equals(iindexword1))
            return 16D;
        IWordID iwordid = (IWordID)iindexword.getWordIDs().get(i - 1);
        ISynset isynset = dict.getWord(iwordid).getSynset();
        IWordID iwordid1 = (IWordID)iindexword1.getWordIDs().get(j - 1);
        ISynset isynset1 = dict.getWord(iwordid1).getSynset();
        if(isynset.equals(isynset1))
            return 16D;
        if(hasHorizontalRelation(isynset, isynset1))
            return 16D;
        if(isCompound(iindexword, iindexword1) && hasAnyRelation(isynset, isynset1))
            return 16D;
        HashSet hashset = new HashSet();
        ArrayList arraylist = new ArrayList();
        arraylist.add(isynset.getID());
        hashset.add(arraylist);
        ISynsetID isynsetid = (ISynsetID)isynset1.getID();
        ArrayList arraylist1 = new ArrayList();
        HashSet hashset1 = hashset;
        Object obj = hashset1.iterator();
        do
        {
            if(!((Iterator) (obj)).hasNext())
                break;
            ArrayList arraylist2 = (ArrayList)((Iterator) (obj)).next();
            if(arraylist2.contains(isynsetid))
                arraylist1.add(getClean(arraylist2));
        } while(true);
        obj = pathfinder(hashset1);
        Object obj1 = ((HashSet) (obj)).iterator();
        do
        {
            if(!((Iterator) (obj1)).hasNext())
                break;
            ArrayList arraylist3 = (ArrayList)((Iterator) (obj1)).next();
            if(arraylist3.contains(isynsetid))
                arraylist1.add(getClean(arraylist3));
        } while(true);
        obj1 = pathfinder(((HashSet) (obj)));
        Object obj2 = ((HashSet) (obj1)).iterator();
        do
        {
            if(!((Iterator) (obj2)).hasNext())
                break;
            ArrayList arraylist4 = (ArrayList)((Iterator) (obj2)).next();
            if(arraylist4.contains(isynsetid))
                arraylist1.add(getClean(arraylist4));
        } while(true);
        obj2 = pathfinder(((HashSet) (obj1)));
        Object obj3 = ((HashSet) (obj2)).iterator();
        do
        {
            if(!((Iterator) (obj3)).hasNext())
                break;
            ArrayList arraylist5 = (ArrayList)((Iterator) (obj3)).next();
            if(arraylist5.contains(isynsetid))
                arraylist1.add(getClean(arraylist5));
        } while(true);
        obj3 = pathfinder(((HashSet) (obj2)));
        Object obj4 = ((HashSet) (obj3)).iterator();
        do
        {
            if(!((Iterator) (obj4)).hasNext())
                break;
            ArrayList arraylist6 = (ArrayList)((Iterator) (obj4)).next();
            if(arraylist6.contains(isynsetid))
                arraylist1.add(getClean(arraylist6));
        } while(true);
        obj4 = pathfinder(((HashSet) (obj3)));
        Iterator iterator = ((HashSet) (obj4)).iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            ArrayList arraylist7 = (ArrayList)iterator.next();
            if(arraylist7.contains(isynsetid))
                arraylist1.add(getClean(arraylist7));
        } while(true);
        if(arraylist1.isEmpty())
            return 0.0D;
        double d1 = 0.0D;
        Iterator iterator1 = arraylist1.iterator();
        do
        {
            if(!iterator1.hasNext())
                break;
            ArrayList arraylist8 = (ArrayList)iterator1.next();
            double d2 = allowable(arraylist8);
            if(d2 != -1D)
            {
                double d3 = arraylist8.size();
                double d4 = 8D - d3 - 1.0D * d2;
                if(d4 > d1)
                    d1 = d4;
            }
        } while(true);
        d = d1;
        return d;
    }
    /*************************************************************/
    private ArrayList getClean(ArrayList arraylist)
    {
        ArrayList arraylist1 = new ArrayList();
        for(int i = 1; i < arraylist.size(); i += 2)
            arraylist1.add((IPointer)arraylist.get(i));

        return arraylist1;
    }

    private double allowable(ArrayList arraylist)
    {
        Object obj = null;
        Object obj1 = null;
        Object obj2 = null;
        boolean flag = false;
        int i = 0;
        for(int j = 0; j < arraylist.size() - 1; j++)
        {
            IPointer ipointer = (IPointer)arraylist.get(j);
            IPointer ipointer1 = (IPointer)arraylist.get(j + 1);
            if(!all.contains(ipointer) || !all.contains(ipointer1))
                return -1D;
            if(!up.contains(ipointer) && up.contains(ipointer1))
                return -1D;
            if(ipointer.equals(ipointer1))
                continue;
            if(up.contains(ipointer) && down.contains(ipointer1))
                i++;
            if(down.contains(ipointer) && horizontal.contains(ipointer1))
                i++;
            if(horizontal.contains(ipointer) && down.contains(ipointer1))
                i++;
            if(!up.contains(ipointer) || !horizontal.contains(ipointer1))
                continue;
            if(j + 2 < arraylist.size())
            {
                IPointer ipointer2 = (IPointer)arraylist.get(j + 2);
                if(down.contains(ipointer2))
                {
                    flag = true;
                    i += 2;
                }
            } else
            {
                i++;
            }
        }

        if(i == 0)
            return 0.0D;
        if(flag && i == 2)
            return 2D;
        if(!flag && i == 1)
            return 1.0D;
        if(flag && i > 2)
            return -1D;
        return flag || i <= 1 ? -1D : -1D;
    }

    private HashSet pathfinder(HashSet hashset)
    {
        Object obj = null;
        Object obj1 = null;
        Object obj2 = null;
        HashSet hashset1 = new HashSet();
        for(Iterator iterator = hashset.iterator(); iterator.hasNext();)
        {
            ArrayList arraylist = (ArrayList)iterator.next();
            ISynsetID isynsetid = (ISynsetID)arraylist.get(arraylist.size() - 1);
            ISynset isynset = dict.getSynset(isynsetid);
            Hashtable hashtable = pointers.getAllRelatedSynsetsAndTheirTypes(isynset);
            Iterator iterator1 = hashtable.keySet().iterator();
            while(iterator1.hasNext()) 
            {
                ISynsetID isynsetid1 = (ISynsetID)iterator1.next();
                IPointer ipointer = (IPointer)hashtable.get(isynsetid1);
                if(all.contains(ipointer))
                {
                    ArrayList arraylist1 = new ArrayList();
                    arraylist1.addAll(arraylist);
                    arraylist1.add(ipointer);
                    arraylist1.add(isynsetid1);
                    hashset1.add(arraylist1);
                }
            }
        }

        return hashset1;
    }

    public TreeMap hso(String s, String s1, String s2)
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
                    double d = hso(s, i, s1, j, s2);
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

    public TreeMap hso(String s, String s1, int i, String s2)
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
                double d = hso(s, j, s1, i, s2);
                treemap.put((new StringBuilder()).append(s).append("#").append(s2).append("#").append(j).append(",").append(s1).append("#").append(s2).append("#").append(i).toString(), Double.valueOf(d));
                j++;
            }

        } else
        {
            return treemap;
        }
        return treemap;
    }

    public TreeMap hso(String s, int i, String s1, String s2)
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
                double d = hso(s, i, s1, j, s2);
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
        TreeMap treemap = hso(s, s1, s2);
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

    private ArrayList up;
    private ArrayList down;
    private ArrayList horizontal;
    private ArrayList all;
    private IDictionary dict;
    private String editor[];
    private NumberFormat formatter;
    private RelatedSynsets pointers;
    private String cut1[];
    private String cut2[];
    private final double C = 8D;
    private final double k = 1.0D;
    public WordnetStemmer stemmer;
}