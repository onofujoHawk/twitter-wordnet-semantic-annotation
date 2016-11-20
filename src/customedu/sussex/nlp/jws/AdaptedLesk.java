// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 15.16.14
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   AdaptedLesk.java

package customedu.sussex.nlp.jws;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.WordnetStemmer;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

// Referenced classes of package edu.sussex.nlp.jws:
//            LeskGlossOverlaps, RelatedSynsets

public class AdaptedLesk
{

    public AdaptedLesk(IDictionary idictionary)
    {
    	max = 0;
        dict = null;
        lgo = null;
        relations = null;
        stemmer = null;
        System.out.println("... Adapted Lesk : all relations");
        dict = idictionary;
        lgo = new LeskGlossOverlaps(idictionary);
        relations = new RelatedSynsets(idictionary);
        stemmer = new WordnetStemmer(idictionary);
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
        HashSet hashset = new HashSet();
        hashset.add(isynset.getID());
        hashset.addAll(getPointers(isynset));
        HashSet hashset1 = new HashSet();
        hashset1.add(isynset1.getID());
        hashset1.addAll(getPointers(isynset1));
        for(Iterator iterator = hashset.iterator(); iterator.hasNext();)
        {
            ISynsetID isynsetid = (ISynsetID)iterator.next();
            //System.out.println("In function dict.getSynset(isynsetid)"+dict.getSynset(isynsetid)+"\n");
            String s3 = dict.getSynset(isynsetid).getGloss();
            Iterator iterator1 = hashset1.iterator();
            while(iterator1.hasNext()) 
            {
                ISynsetID isynsetid1 = (ISynsetID)iterator1.next();
                String s4 = dict.getSynset(isynsetid1).getGloss();
                //System.out.println("Sono qui s3\n"+s3+"\n");
                //System.out.println("Sono qui s4\n"+s4+"\n");
                double d1 = lgo.overlap(s3, s4);
                d += d1;
            }
        }

        return d;
    }
    /*************************************************************/
    public double lesk_custom_disambiguate(String s, int i, String s1, int j, String s2,String s7)
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
        if(s7.equalsIgnoreCase("n"))
        {            
            iindexword1 = dict.getIndexWord(s1, POS.NOUN);
        }
        if(s7.equalsIgnoreCase("v"))
        {            
            iindexword1 = dict.getIndexWord(s1, POS.VERB);
        }
        if(s7.equalsIgnoreCase("a"))
        {            
            iindexword1 = dict.getIndexWord(s1, POS.ADJECTIVE);
        }
        if(s7.equalsIgnoreCase("r"))
        {            
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
        for(Iterator iterator = hashset.iterator(); iterator.hasNext();)
        {
            ISynsetID isynsetid = (ISynsetID)iterator.next();
            //System.out.println("In function dict.getSynset(isynsetid)"+dict.getSynset(isynsetid)+"\n");
            String s3 = dict.getSynset(isynsetid).getGloss();
            Iterator iterator1 = hashset1.iterator();
            while(iterator1.hasNext()) 
            {
                ISynsetID isynsetid1 = (ISynsetID)iterator1.next();
                String s4 = dict.getSynset(isynsetid1).getGloss();
                //System.out.println("Sono qui s3\n"+s3+"\n");
                //System.out.println("Sono qui s4\n"+s4+"\n");
                double d1 = lgo.overlap(s3, s4);
                d += d1;
            }
        }

        return d;
    }
    /************************************************************/
    /*************************************************************/
    
    public double lesk_custom(IIndexWord iindexword, int i, IIndexWord iindexword1, int j)
    {
        double d = 0.0D;                
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
        System.out.println("i:"+i+"\n");
        System.out.println("j:"+j+"\n");
        IWordID iwordid = (IWordID)iindexword.getWordIDs().get(i - 1);
        System.out.println("iwordid:"+iwordid+"\n");        
        ISynset isynset = dict.getWord(iwordid).getSynset();
        System.out.println("isynset:"+isynset+"\n");        
        IWordID iwordid1 = (IWordID)iindexword1.getWordIDs().get(j - 1);
        System.out.println("iwordid1:"+iwordid1+"\n");
        ISynset isynset1 = dict.getWord(iwordid1).getSynset(); 
        System.out.println("isynset1:"+isynset1+"\n");
        HashSet hashset = new HashSet();
        System.out.println("isynset.getID():"+isynset.getID()+"\n");
        System.out.println("getPointers(isynset):"+getPointers(isynset)+"\n");

        System.out.println("isynset1.getID():"+isynset1.getID()+"\n");
        System.out.println("getPointers(isynset1):"+getPointers(isynset1)+"\n");
        hashset.add(isynset.getID());
        hashset.addAll(getPointers(isynset));
        HashSet hashset1 = new HashSet();
        hashset1.add(isynset1.getID());
        hashset1.addAll(getPointers(isynset1));
        for(Iterator iterator = hashset.iterator(); iterator.hasNext();)
        {
            ISynsetID isynsetid = (ISynsetID)iterator.next();
            String s3 = dict.getSynset(isynsetid).getGloss();
            Iterator iterator1 = hashset1.iterator();
            while(iterator1.hasNext()) 
            {
                ISynsetID isynsetid1 = (ISynsetID)iterator1.next();
                String s4 = dict.getSynset(isynsetid1).getGloss();
                double d1 = lgo.overlap(s3, s4);
                d += d1;
            }
        }

        return d;
    }
    
    /*************************************************************/

    private HashSet getPointers(ISynset isynset)
    {
        HashSet hashset = relations.getAllRelatedSynsetsNoTypes(isynset);
        return hashset;
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
                    double d = lesk(s, i, s1, j, s2);
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
        if(iindexword != null && iindexword1 != null)
        {
            List list = iindexword.getWordIDs();
            int j = 1;
            for(Iterator iterator = list.iterator(); iterator.hasNext();)
            {
                IWordID iwordid = (IWordID)iterator.next();
                double d = lesk(s, j, s1, i, s2);
                treemap.put((new StringBuilder()).append(s).append("#").append(s2).append("#").append(j).append(",").append(s1).append("#").append(s2).append("#").append(i).toString(), Double.valueOf(d));
                j++;
            }

        } else
        {
            return treemap;
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
        if(iindexword != null && iindexword1 != null)
        {
            List list = iindexword1.getWordIDs();
            int j = 1;
            for(Iterator iterator = list.iterator(); iterator.hasNext();)
            {
                IWordID iwordid = (IWordID)iterator.next();
                double d = lesk(s, i, s1, j, s2);
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

    /******************************************************************/
    
    
    public void azzera_max()
    {
    	max = 0.0;
    }
    
    
    public TreeMap lesk_gloss_custom(String t1, String t2, String g2, String pos)
    {
    	TreeMap treemap = new TreeMap();
        IIndexWord iindexword = null;
        if(pos.equalsIgnoreCase("n"))
        {
            iindexword = dict.getIndexWord(t1, POS.NOUN);
        }
        if(pos.equalsIgnoreCase("v"))
        {
            iindexword = dict.getIndexWord(t1, POS.VERB);
        }
        if(pos.equalsIgnoreCase("a"))
        {
            iindexword = dict.getIndexWord(t1, POS.ADJECTIVE);
        }
        if(pos.equalsIgnoreCase("r"))
        {
            iindexword = dict.getIndexWord(t1, POS.ADVERB);
        }
        if(iindexword != null)
        {
            List list = iindexword.getWordIDs();
            int i = 1;
            Object obj = null;
            Object obj1 = null;
            for(Iterator iterator = list.iterator(); iterator.hasNext();)
            {
                IWordID iwordid = (IWordID)iterator.next();
                //solo un significato per la glossa della parola chiave
                int j = 1; 
                double d = lesk_custom(t1, i, t2, j, g2, pos);
                treemap.put((new StringBuilder()).append(t1).append("#").append(t2).append("#").append(i).append(",").append(t1).append("#").append(t2).append("#").append(j).toString(), Double.valueOf(d));
                i++;
            }

        } else
        {
            return treemap;
        }
        return treemap;
    }
    
    
    public double lesk_custom(String s1, int i, String s2, int j, String g2, String pos)
    {
        double d = 0.0D;
        IIndexWord iindexword = null;
        if(pos.equalsIgnoreCase("n"))
        {
            iindexword = dict.getIndexWord(s1, POS.NOUN);
        }
        if(pos.equalsIgnoreCase("v"))
        {
            iindexword = dict.getIndexWord(s1, POS.VERB);
        }
        if(pos.equalsIgnoreCase("a"))
        {
            iindexword = dict.getIndexWord(s1, POS.ADJECTIVE);
        }
        if(pos.equalsIgnoreCase("r"))
        {
            iindexword = dict.getIndexWord(s1, POS.ADVERB);
        }
        if(iindexword == null)
        {
            System.out.println((new StringBuilder()).append(s1).append("(").append(s2).append(") not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        if(s2 == null)
        {
            System.out.println((new StringBuilder()).append(s1).append("(").append(s2).append(") not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        List list = iindexword.getWordIDs();
        if(i > list.size())
        {
            System.out.println((new StringBuilder()).append(s1).append(" sense: ").append(i).append(" not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        if(j != 1)
        {
            System.out.println((new StringBuilder()).append(s2).append(" sense: ").append(j).append(" not found in WordNet ").append(dict.getVersion()).toString());
            return 0.0D;
        }
        IWordID iwordid = (IWordID)iindexword.getWordIDs().get(i - 1);
        ISynset isynset = dict.getWord(iwordid).getSynset();
        HashSet hashset = new HashSet();
        hashset.add(isynset.getID());
        hashset.addAll(getPointers(isynset));
        for(Iterator iterator = hashset.iterator(); iterator.hasNext();)
        {
            ISynsetID isynsetid = (ISynsetID)iterator.next();
            String g1 = dict.getSynset(isynsetid).getGloss();
            
            
            /* 
             * praticamente lesk sarebbe la somma degli overlap 
             * delle glosse per tutti i significati dei due termini 
             */
            double d1 = lgo.overlap(g1, g2);
            
            
            d += d1; 
        }

        return d;
    }
    
    
    /**
	 * Effettua il calcolo di similarità tra due concetti attraverso la misura
	 * di Adapted Lesk, versione customizzata per integrazione con BabelNet.
	 * @author onofrio
     * @param t1 il primo termine da confrontare
     * @param t2 il secondo termine da confrontare
     * @param g1 la glossa del secondo termine già estratta
     * @param pos la POS dei due termini
     * @return lo score dopo l'overlap
     */
    public double overlap_gloss_custom(String t1, String t2, String g2, String pos)
    {
    	  double d = 0.0D;
    	  String significato = "";
          TreeMap treemap = lesk_gloss_custom(t1, t2, g2, pos);
          Iterator iterator = treemap.keySet().iterator();
          System.out.println("\rrisultato lesk overlap = "+treemap);
          do
          {
              if(!iterator.hasNext())
                  break;
              String s3 = (String)iterator.next();
              double d1 = ((Double)treemap.get(s3)).doubleValue();
              if(d1 > d) 
              {
                  d = d1;
                  significato=s3+"="+d1;
              }
          } while(true);
          
          if (d > max)
          {
        	  max = d;
        	  String[] significati=significato.toString().split("#");
              signmax=Integer.valueOf(String.valueOf(significati[2].charAt(0)));
              System.out.println("\r   VALORE DI SIGNMAX = "+signmax+", MAX OVERLAP = "+d+"\r");
          }
          System.out.println("valore di max overlap: "+d+" associato alla coppia = ("+significato+")\r");
          return d;
    }
    /*******************************************************************/
    
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
    /*******************************************************************/
    
    public void useStopList(boolean flag)
    {
        lgo.useStopList(flag);
    }

    public void useLemmatiser(boolean flag)
    {
        lgo.useLemmatiser(flag);
    }

    public static void main(String args[])
    {
        String s = "2.1";
        String s1 = (new StringBuilder()).append("C:/Program Files/WordNet/").append(s).append("/dict").toString();
        String s2 = (new StringBuilder()).append("C:/Program Files/WordNet/").append(s).append("/WordNet-InfoContent-").append(s).append("/ic-semcor.dat").toString();
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
        AdaptedLesk adaptedlesk = new AdaptedLesk(dictionary);
        double d = adaptedlesk.lesk("sweet", 1, "sour", 1, "a");
        System.out.println("specific senses");
        System.out.println((new StringBuilder()).append("lesk:\t").append(decimalformat.format(d)).toString());
        System.out.println();
        TreeMap treemap = adaptedlesk.lesk("be", "be", "v");
        System.out.println("all senses");
        String s3;
        for(Iterator iterator = treemap.keySet().iterator(); iterator.hasNext(); System.out.println((new StringBuilder()).append(s3).append("\t").append(decimalformat.format(treemap.get(s3))).toString()))
            s3 = (String)iterator.next();

        System.out.println();
        double d1 = adaptedlesk.max("be", "be", "v");
        System.out.println("max value");
        System.out.println(decimalformat.format(d1));
        System.out.println();
        TreeMap treemap1 = adaptedlesk.lesk("sweet", "sour", 2, "a");
        System.out.println("all senses of word 1 vs. fixed sense of word 2");
        String s4;
        for(Iterator iterator1 = treemap1.keySet().iterator(); iterator1.hasNext(); System.out.println((new StringBuilder()).append(s4).append("\t").append(decimalformat.format(treemap1.get(s4))).toString()))
            s4 = (String)iterator1.next();

        System.out.println();
        TreeMap treemap2 = adaptedlesk.lesk("sweet", 1, "sour", "a");
        System.out.println("fixed sense of word 1 vs. all senses of word 2");
        String s5;
        for(Iterator iterator2 = treemap2.keySet().iterator(); iterator2.hasNext(); System.out.println((new StringBuilder()).append(s5).append("\t").append(decimalformat.format(treemap2.get(s5))).toString()))
            s5 = (String)iterator2.next();

    }

    private IDictionary dict;
    private LeskGlossOverlaps lgo;
    private RelatedSynsets relations;
    public WordnetStemmer stemmer;
    public int signmax;
    double max;
}