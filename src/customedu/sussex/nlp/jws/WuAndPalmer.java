// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 15.21.17
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   WuAndPalmer.java

package customedu.sussex.nlp.jws;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import java.io.PrintStream;
import java.util.*;

public class WuAndPalmer
{

    public WuAndPalmer(IDictionary idictionary, ArrayList arraylist)
    {
        dict = null;
        roots = null;
        System.out.println("... WuAndPalmer");
        dict = idictionary;
        roots = arraylist;
    }

    public double wup(String s, int i, String s1, int j, String s2)
    {
        //System.out.println("S: "+s+" S1: "+s1+" S2: "+s2);
        double d = 0.0D;
        IIndexWord iindexword = null;
        IIndexWord iindexword1 = null;
        if(s2.equalsIgnoreCase("n"))
        {
            iindexword = dict.getIndexWord(s, POS.NOUN);
            iindexword1 = dict.getIndexWord(s1, POS.NOUN);
        }
        else
        if(s2.equalsIgnoreCase("v"))
        {
            iindexword = dict.getIndexWord(s, POS.VERB);
            iindexword1 = dict.getIndexWord(s1, POS.VERB);
        }
        else 
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
        //System.out.println("II:"+iindexword+" "+iindexword1+"\n");
        if(iindexword == null)
        {
            //System.out.println((new StringBuilder()).append(s).append("(").append(s2).append(") not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        if(iindexword1 == null)
        {
            //System.out.println((new StringBuilder()).append(s1).append("(").append(s2).append(") not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        List list = iindexword.getWordIDs();
        List list1 = iindexword1.getWordIDs();
        if(i > list.size())
        {
            //System.out.println((new StringBuilder()).append(s).append(" sense: ").append(i).append(" not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        if(j > list1.size())
        {
            //System.out.println((new StringBuilder()).append(s1).append(" sense: ").append(j).append(" not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        IWordID iwordid = (IWordID)iindexword.getWordIDs().get(i - 1);
        ISynset isynset = dict.getWord(iwordid).getSynset();
        IWordID iwordid1 = (IWordID)iindexword1.getWordIDs().get(j - 1);
        ISynset isynset1 = dict.getWord(iwordid1).getSynset();
        if(isynset.equals(isynset1))
            return 1.0D;
        ArrayList arraylist = paths(isynset);
        ArrayList arraylist1 = paths(isynset1);
        double d1 = 0.0D;
        for(Iterator iterator = arraylist.iterator(); iterator.hasNext();)
        {
            ArrayList arraylist2 = (ArrayList)iterator.next();
            Iterator iterator1 = arraylist1.iterator();
            while(iterator1.hasNext()) 
            {
                
                ArrayList arraylist3 = (ArrayList)iterator1.next();
                //System.out.println(iterator1.toString());
                double d2 = looking(arraylist2, arraylist3);
                if(d2 > d1)
                    d1 = d2;
            }
        }

        d = d1;
            return d;
    }

    private double looking(ArrayList arraylist, ArrayList arraylist1)
    {
        double d = (double)arraylist.size() + 1.0D;
        double d1 = (double)arraylist1.size() + 1.0D;
        double d2 = 0.0D;
        double d3 = 0.0D;
        ArrayList arraylist2 = new ArrayList();
        arraylist2.addAll(arraylist);
        arraylist2.retainAll(arraylist1);
        if(arraylist2.isEmpty())
        {
            d2 = 1.0D;
        } else
        {
            double d4 = 1.7976931348623157E+308D;
            ISynsetID isynsetid = null;
            Iterator iterator = arraylist2.iterator();
            do
            {
                if(!iterator.hasNext())
                    break;
                ISynsetID isynsetid1 = (ISynsetID)iterator.next();
                int i = arraylist.indexOf(isynsetid1);
                int j = arraylist1.indexOf(isynsetid1);
                double d6 = i + j;
                if(d6 < d4)
                {
                    d4 = d6;
                    isynsetid = isynsetid1;
                }
            } while(true);
            if(isynsetid.equals(arraylist.get(0)))
                d2 = d;
            else
            if(isynsetid.equals(arraylist1.get(0)))
            {
                d2 = d1;
            } else
            {
                ArrayList arraylist3 = paths(dict.getSynset(isynsetid));
                double d5 = 1.7976931348623157E+308D;
                Iterator iterator1 = arraylist3.iterator();
                do
                {
                    if(!iterator1.hasNext())
                        break;
                    ArrayList arraylist4 = (ArrayList)iterator1.next();
                    double d7 = arraylist4.size();
                    if(d7 < d5)
                        d5 = d7;
                } while(true);
                d2 = d5 + 1.0D;
            }
        }
        d3 = (2D * d2) / (d + d1);
        if(d3 > 1.0D)
            return 0.0D;
        else
            return d3;
    }

    private ArrayList paths(ISynset isynset)
    {
        ArrayList arraylist = new ArrayList();
        ArrayList arraylist1 = new ArrayList();
        arraylist1.add(isynset.getID());
        arraylist.add(arraylist1);
        for(boolean flag = true; flag;)
        {
            int i = 0;
            while(i < arraylist.size()) 
            {
                flag = false;
                ArrayList arraylist3 = (ArrayList)arraylist.get(i);
                ArrayList arraylist4 = new ArrayList();
                ISynsetID isynsetid = (ISynsetID)arraylist3.get(arraylist3.size() - 1);
                HashSet hashset = hypernyms(isynsetid);
                if(!hashset.isEmpty())
                {
                    flag = true;
                    ArrayList arraylist6;
                    for(Iterator iterator1 = hashset.iterator(); iterator1.hasNext(); arraylist4.add(arraylist6))
                    {
                        ISynsetID isynsetid1 = (ISynsetID)iterator1.next();
                        arraylist6 = new ArrayList();
                        arraylist6.addAll(arraylist3);
                        arraylist6.add(isynsetid1);
                    }

                }
                arraylist.addAll(arraylist4);
                i++;
            }
        }

        ArrayList arraylist2 = new ArrayList();
        Iterator iterator = arraylist.iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            ArrayList arraylist5 = (ArrayList)iterator.next();
            if(roots.contains(arraylist5.get(arraylist5.size() - 1)))
                arraylist2.add(arraylist5);
        } while(true);
        return arraylist2;
    }

    private HashSet hypernyms(ISynsetID isynsetid)
    {
        HashSet hashset = new HashSet();
        ISynset isynset = dict.getSynset(isynsetid);
        hashset.addAll(isynset.getRelatedSynsets(Pointer.HYPERNYM));
        hashset.addAll(isynset.getRelatedSynsets(Pointer.HYPERNYM_INSTANCE));
        return hashset;
    }

    public TreeMap wup(String s, String s1, String s2)
    {
        //System.out.println("ARGS: S:"+s+" S1:"+ s1 +" S2:"+ s2);
        TreeMap<String,Double> treemap = new TreeMap<String,Double>();
        IIndexWord iindexword = null;
        IIndexWord iindexword1 = null;
        if(s2.equalsIgnoreCase("n"))
        {
            iindexword = dict.getIndexWord(s, POS.NOUN);
            iindexword1 = dict.getIndexWord(s1, POS.NOUN);
        }
        else
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
            //DA COME HO CAPITO, LUI PROVA TUTTI I POSSIBILI SIGNIFICATI DI OGNI PAROLA E PRENDE QUELLE CHE GLI DANNO LA SIMILARITÀ MASSIMA
            for(Iterator iterator = list.iterator(); iterator.hasNext();)
            {
                IWordID iwordid = (IWordID)iterator.next();
                int j = 1;
                for(Iterator iterator1 = list1.iterator(); iterator1.hasNext();)
                {
                    IWordID iwordid1 = (IWordID)iterator1.next();
                    double d = wup(s, i, s1, j, s2);
                    //System.out.println("CREO "+new StringBuilder().append(s).append("#").append(s2).append("#").append(i).append(",").append(s1).append("#").append(s2).append("#").append(j).toString());
                    treemap.put((new StringBuilder()).append(s).append("#").append(s2).append("#").append(i).append(",").append(s1).append("#").append(s2).append("#").append(j).toString(), Double.valueOf(d));
                    j++;
                }

                i++;
            }

        } else
        {
            return treemap;
        }
        for(Map.Entry<String,Double> entry : treemap.entrySet()) 
        {
            String key = entry.getKey();
            Double value = entry.getValue();

            //System.out.println(key + " => " + value);
        }
       
        return treemap;
    }

    public TreeMap wup(String s, String s1, int i, String s2)
    {
        TreeMap treemap = new TreeMap();
        IIndexWord iindexword = null;
        IIndexWord iindexword1 = null;
        if(s2.equalsIgnoreCase("n"))
        {
            iindexword = dict.getIndexWord(s, POS.NOUN);
            iindexword1 = dict.getIndexWord(s1, POS.NOUN);
        }
        else
        if(s2.equalsIgnoreCase("v"))
        {
            iindexword = dict.getIndexWord(s, POS.VERB);
            iindexword1 = dict.getIndexWord(s1, POS.VERB);
        }
        else 
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
                double d = wup(s, j, s1, i, s2);
                treemap.put((new StringBuilder()).append(s).append("#").append(s2).append("#").append(j).append(",").append(s1).append("#").append(s2).append("#").append(i).toString(), Double.valueOf(d));
                j++;
            }

        } else
        {
            return treemap;
        }
        return treemap;
    }

    public TreeMap wup(String s, int i, String s1, String s2)
    {
        TreeMap treemap = new TreeMap();
        IIndexWord iindexword = null;
        IIndexWord iindexword1 = null;
       if(s2.equalsIgnoreCase("n"))
        {
            iindexword = dict.getIndexWord(s, POS.NOUN);
            iindexword1 = dict.getIndexWord(s1, POS.NOUN);
        }
        else
        if(s2.equalsIgnoreCase("v"))
        {
            iindexword = dict.getIndexWord(s, POS.VERB);
            iindexword1 = dict.getIndexWord(s1, POS.VERB);
        }
        else 
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
                double d = wup(s, i, s1, j, s2);
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
        TreeMap treemap = wup(s, s1, s2);
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
}