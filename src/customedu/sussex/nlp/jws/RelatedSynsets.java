// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 15.20.39
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   RelatedSynsets.java

package customedu.sussex.nlp.jws;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import java.util.*;

public class RelatedSynsets
{

    public RelatedSynsets(IDictionary idictionary)
    {
        dict = null;
        dict = idictionary;
    }

    public HashSet getAllRelatedSynsetsNoTypes(String s, int i, String s1)
    {
        HashSet hashset = new HashSet();
        hashset.addAll(getAllRelatedSynsetsAndTheirTypes(s, i, s1).keySet());
        return hashset;
    }

    public HashSet getAllRelatedSynsetsNoTypes(ISynset isynset)
    {
        HashSet hashset = new HashSet();
        hashset.addAll(getAllRelatedSynsetsAndTheirTypes(isynset).keySet());
        return hashset;
    }

    public Hashtable getAllRelatedSynsetsAndTheirTypes(String s, int i, String s1)
    {
        Hashtable hashtable = new Hashtable();
        IIndexWord iindexword = null;
        if(s1.equalsIgnoreCase("n"))
            iindexword = dict.getIndexWord(s, POS.NOUN);
        if(s1.equalsIgnoreCase("v"))
            iindexword = dict.getIndexWord(s, POS.VERB);
        if(s1.equalsIgnoreCase("a"))
            iindexword = dict.getIndexWord(s, POS.ADJECTIVE);
        if(s1.equalsIgnoreCase("r"))
            iindexword = dict.getIndexWord(s, POS.ADVERB);
        if(iindexword == null)
            return hashtable;
        IWordID iwordid = (IWordID)iindexword.getWordIDs().get(i - 1);
        IWord iword = dict.getWord(iwordid);
        ISynset isynset = iword.getSynset();
        Map map = isynset.getRelatedMap();
        for(Iterator iterator = map.keySet().iterator(); iterator.hasNext();)
        {
            IPointer ipointer = (IPointer)iterator.next();
            List list = (List)map.get(ipointer);
            Iterator iterator2 = list.iterator();
            while(iterator2.hasNext()) 
            {
                ISynsetID isynsetid = (ISynsetID)iterator2.next();
                hashtable.put(isynsetid, ipointer);
            }
        }

        Map map1 = iword.getRelatedMap();
        for(Iterator iterator1 = map1.keySet().iterator(); iterator1.hasNext();)
        {
            IPointer ipointer1 = (IPointer)iterator1.next();
            List list1 = (List)map1.get(ipointer1);
            Iterator iterator3 = list1.iterator();
            while(iterator3.hasNext()) 
            {
                IWordID iwordid1 = (IWordID)iterator3.next();
                hashtable.put(iwordid1.getSynsetID(), ipointer1);
            }
        }

        return hashtable;
    }

    public Hashtable getAllRelatedSynsetsAndTheirTypes(ISynset isynset)
    {
        Hashtable hashtable = new Hashtable();
        Map map = isynset.getRelatedMap();
        for(Iterator iterator = map.keySet().iterator(); iterator.hasNext();)
        {
            IPointer ipointer = (IPointer)iterator.next();
            List list1 = (List)map.get(ipointer);
            Iterator iterator2 = list1.iterator();
            while(iterator2.hasNext()) 
            {
                ISynsetID isynsetid = (ISynsetID)iterator2.next();
                hashtable.put(isynsetid, ipointer);
            }
        }

        List list = isynset.getWords();
        for(Iterator iterator1 = list.iterator(); iterator1.hasNext();)
        {
            IWord iword = (IWord)iterator1.next();
            Map map1 = iword.getRelatedMap();
            Iterator iterator3 = map1.keySet().iterator();
            while(iterator3.hasNext()) 
            {
                IPointer ipointer1 = (IPointer)iterator3.next();
                List list2 = (List)map1.get(ipointer1);
                Iterator iterator4 = list2.iterator();
                while(iterator4.hasNext()) 
                {
                    IWordID iwordid = (IWordID)iterator4.next();
                    hashtable.put(iwordid.getSynsetID(), ipointer1);
                }
            }
        }

        return hashtable;
    }

    private IDictionary dict;
}