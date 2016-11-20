// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 15.17.57
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   DepthFinder.java

package customedu.sussex.nlp.jws;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DepthFinder
{

    public DepthFinder(IDictionary idictionary, String s)
    {
        dict = null;
        icfile = "";
        nounroots = null;
        verbroots = null;
        System.out.println("... DepthFinder");
        dict = idictionary;
        icfile = s;
        nounroots = new ArrayList();
        verbroots = new ArrayList();
        getRoots();
    }

    public HashSet getLCSbyDepth(ISynset isynset, ISynset isynset1, String s)
    {
        HashSet hashset = new HashSet();
        if(isynset.equals(isynset1))
        {
            HashSet hashset1 = new HashSet();
            hashset1.add(isynset.getID());
            return hashset1;
        }
        double d = getSynsetDepth(isynset, s);
        double d1 = getSynsetDepth(isynset1, s);
        if(d == 0.0D && d1 == 0.0D)
            return hashset;
        if(d == 0.0D || d1 == 0.0D)
        {
            if(d == 0.0D)
                hashset.add(isynset.getID());
            if(d1 == 0.0D)
                hashset.add(isynset1.getID());
            return hashset;
        }
        TreeMap treemap = new TreeMap();
        HashSet hashset2 = new HashSet();
        hashset2.add(isynset.getID());
        HashSet hashset3 = new HashSet();
        getHypernyms(hashset2, hashset3);
        HashSet hashset4 = new HashSet();
        hashset4.add(isynset1.getID());
        HashSet hashset5 = new HashSet();
        getHypernyms(hashset4, hashset5);
        hashset3.retainAll(hashset5);
        for(Iterator iterator = hashset3.iterator(); iterator.hasNext();)
        {
            ISynsetID isynsetid = (ISynsetID)iterator.next();
            TreeMap treemap1 = getSynsetDepth(isynsetid.getOffset(), s);
            Iterator iterator1 = treemap1.keySet().iterator();
            while(iterator1.hasNext()) 
            {
                Integer integer = (Integer)iterator1.next();
                HashSet hashset6 = (HashSet)treemap1.get(integer);
                if(treemap.containsKey(integer))
                {
                    HashSet hashset7 = (HashSet)treemap.get(integer);
                    hashset7.add(isynsetid);
                    treemap.put(integer, hashset7);
                } else
                {
                    HashSet hashset8 = new HashSet();
                    hashset8.add(isynsetid);
                    treemap.put(integer, hashset8);
                }
            }
        }

        int i = ((Integer)treemap.lastKey()).intValue();
        hashset = (HashSet)treemap.get(Integer.valueOf(i));
        return hashset;
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

    public int getTaxonomyDepth(int i, String s)
    {
        boolean flag = false;
        SynsetID synsetid = null;
        ArrayList arraylist = null;
        if(s.equalsIgnoreCase("n"))
        {
            arraylist = nounroots;
            synsetid = new SynsetID(i, POS.NOUN);
        }
        if(s.equalsIgnoreCase("v"))
        {
            arraylist = verbroots;
            synsetid = new SynsetID(i, POS.VERB);
        }
        if(!arraylist.contains(Integer.valueOf(i)))
        {
            return -1;
        } else
        {
            HashSet hashset = new HashSet();
            hashset.add(synsetid);
            int j = treediver(hashset);
            return j;
        }
    }

    private int treediver(HashSet hashset)
    {
        int i = 0;
        ArrayList arraylist = new ArrayList();
        arraylist.addAll(hashset);
        for(boolean flag = true; flag;)
        {
            HashSet hashset1 = new HashSet();
            ISynset isynset;
            for(; !arraylist.isEmpty(); hashset1.addAll(isynset.getRelatedSynsets(Pointer.HYPONYM_INSTANCE)))
            {
                isynset = dict.getSynset((ISynsetID)arraylist.remove(0));
                hashset1.addAll(isynset.getRelatedSynsets(Pointer.HYPONYM));
            }

            if(hashset1.isEmpty())
            {
                flag = false;
            } else
            {
                i++;
                arraylist.addAll(hashset1);
            }
        }

        return i;
    }

    public double getSynsetDepth(String s, int i, String s1)
    {
        IIndexWord iindexword = null;
        ArrayList arraylist = null;
        if(s1.equalsIgnoreCase("n"))
        {
            iindexword = dict.getIndexWord(s, POS.NOUN);
            arraylist = nounroots;
        }
        if(s1.equalsIgnoreCase("v"))
        {
            iindexword = dict.getIndexWord(s, POS.VERB);
            arraylist = verbroots;
        }
        IWordID iwordid = (IWordID)iindexword.getWordIDs().get(i - 1);
        ISynset isynset = dict.getWord(iwordid).getSynset();
        TreeMap treemap = new TreeMap();
        HashSet hashset = new HashSet();
        hashset.add(isynset.getID());
        treecreeper(0, hashset, treemap, arraylist);
        if(treemap.isEmpty())
            return 0.0D;
        else
            return (double)((Integer)treemap.lastKey()).intValue() + 2D;
    }

    public double getSynsetDepth(ISynset isynset, String s)
    {
        ArrayList arraylist = null;
        if(s.equalsIgnoreCase("n"))
            arraylist = nounroots;
        if(s.equalsIgnoreCase("v"))
            arraylist = verbroots;
        TreeMap treemap = new TreeMap();
        HashSet hashset = new HashSet();
        hashset.add(isynset.getID());
        treecreeper(0, hashset, treemap, arraylist);
        if(treemap.isEmpty())
            return 0.0D;
        else
            return (double)((Integer)treemap.lastKey()).intValue() + 2D;
    }

    public TreeMap getSynsetDepth(int i, String s)
    {
        TreeMap treemap = new TreeMap();
        SynsetID synsetid = null;
        ArrayList arraylist = null;
        if(s.equalsIgnoreCase("n"))
        {
            synsetid = new SynsetID(i, POS.NOUN);
            arraylist = nounroots;
        }
        if(s.equalsIgnoreCase("v"))
        {
            synsetid = new SynsetID(i, POS.VERB);
            arraylist = verbroots;
        }
        if(synsetid == null)
        {
            return null;
        } else
        {
            int j = 0;
            HashSet hashset = new HashSet();
            hashset.add(synsetid);
            treecreeper(j, hashset, treemap, arraylist);
            return treemap;
        }
    }

    private void treecreeper(int i, HashSet hashset, TreeMap treemap, ArrayList arraylist)
    {
        i++;
        Object obj = null;
        HashSet hashset1 = new HashSet();
        ISynset isynset;
        for(Iterator iterator = hashset.iterator(); iterator.hasNext(); hashset1.addAll(isynset.getRelatedSynsets(Pointer.HYPERNYM_INSTANCE)))
        {
            ISynsetID isynsetid = (ISynsetID)iterator.next();
            isynset = dict.getSynset(isynsetid);
            hashset1.addAll(isynset.getRelatedSynsets(Pointer.HYPERNYM));
        }

        if(!hashset1.isEmpty())
        {
            Iterator iterator1 = hashset1.iterator();
            do
            {
                if(!iterator1.hasNext())
                    break;
                ISynsetID isynsetid1 = (ISynsetID)iterator1.next();
                int j = isynsetid1.getOffset();
                if(arraylist.contains(Integer.valueOf(j)))
                    if(treemap.containsKey(Integer.valueOf(i)))
                    {
                        HashSet hashset2 = (HashSet)treemap.get(Integer.valueOf(i));
                        hashset2.add(isynsetid1);
                        treemap.put(Integer.valueOf(i), hashset2);
                    } else
                    {
                        HashSet hashset3 = new HashSet();
                        hashset3.add(isynsetid1);
                        treemap.put(Integer.valueOf(i), hashset3);
                    }
            } while(true);
            treecreeper(i, hashset1, treemap, arraylist);
        }
    }

    public int getSynsetMaximumDepth(int i, String s)
    {
        TreeMap treemap = getSynsetDepth(i, s);
        return ((Integer)treemap.lastKey()).intValue();
    }

    public HashSet getSynsetMaximumRoots(int i, String s)
    {
        TreeMap treemap = getSynsetDepth(i, s);
        java.util.Map.Entry entry = treemap.lastEntry();
        return (HashSet)entry.getValue();
    }

    private void getRoots()
    {
        Pattern pattern = Pattern.compile("[0-9]+n [0-9]+ ROOT");
        Pattern pattern1 = Pattern.compile("[0-9]+v [0-9]+ ROOT");
        Object obj = null;
        String s = "";
        try
        {
            BufferedReader bufferedreader = new BufferedReader(new FileReader(icfile));
            do
            {
                String s3;
                if((s3 = bufferedreader.readLine()) == null)
                    break;
                Matcher matcher = pattern.matcher(s3);
                if(matcher.matches())
                {
                    String s1 = s3.split("\\s")[0].split("n")[0];
                    nounroots.add(Integer.valueOf(Integer.parseInt(s1)));
                }
                matcher = pattern1.matcher(s3);
                if(matcher.matches())
                {
                    String s2 = s3.split("\\s")[0].split("v")[0];
                    verbroots.add(Integer.valueOf(Integer.parseInt(s2)));
                }
            } while(true);
            bufferedreader.close();
        }
        catch(IOException ioexception)
        {
            ioexception.printStackTrace();
        }
    }

    public HashSet getTaxonomies(int i, String s)
    {
        HashSet hashset = new HashSet();
        TreeMap treemap = getSynsetDepth(i, s);
        Integer integer;
        for(Iterator iterator = treemap.keySet().iterator(); iterator.hasNext(); hashset.addAll((Collection)treemap.get(integer)))
            integer = (Integer)iterator.next();

        return hashset;
    }

    private IDictionary dict;
    private String icfile;
    private ArrayList nounroots;
    private ArrayList verbroots;
}