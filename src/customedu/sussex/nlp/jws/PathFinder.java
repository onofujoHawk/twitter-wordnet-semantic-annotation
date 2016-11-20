// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 15.20.23
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   PathFinder.java

package customedu.sussex.nlp.jws;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import java.io.PrintStream;
import java.util.*;

public class PathFinder
{

    public PathFinder(IDictionary idictionary)
    {
        dict = null;
        System.out.println("... PathFinder");
        dict = idictionary;
    }

    private double getShortestPath(ISynset isynset, ISynset isynset1)
    {
        double d = 1.7976931348623157E+308D;
        HashSet hashset = new HashSet();
        hashset.add(isynset.getID());
        TreeMap treemap = new TreeMap();
        getHypernyms(0, hashset, treemap);
        HashSet hashset1 = new HashSet();
        hashset1.add(isynset1.getID());
        TreeMap treemap1 = new TreeMap();
        getHypernyms(0, hashset1, treemap1);
        for(Iterator iterator = treemap.keySet().iterator(); iterator.hasNext();)
        {
            Integer integer = (Integer)iterator.next();
            HashSet hashset2 = (HashSet)treemap.get(integer);
            Iterator iterator1 = treemap1.keySet().iterator();
            while(iterator1.hasNext()) 
            {
                Integer integer1 = (Integer)iterator1.next();
                HashSet hashset3 = (HashSet)treemap1.get(integer1);
                HashSet hashset4 = new HashSet();
                hashset4.addAll(hashset2);
                hashset4.retainAll(hashset3);
                if(!hashset4.isEmpty() && (double)(integer.intValue() + integer1.intValue()) < d)
                    d = integer.intValue() + integer1.intValue();
            }
        }

        return d + 1.0D;
    }

    private void getHypernyms(int i, HashSet hashset, TreeMap treemap)
    {
        i++;
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
            treemap.put(Integer.valueOf(i), hashset1);
            getHypernyms(i, hashset1, treemap);
        }
    }

    private IDictionary dict;
}