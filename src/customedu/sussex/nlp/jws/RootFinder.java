// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 15.21.04
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   RootFinder.java

package customedu.sussex.nlp.jws;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import java.io.*;
import java.util.*;

public class RootFinder
{

    public RootFinder(IDictionary idictionary)
    {
        dict = null;
        roots = null;
        wnversion = "";
        dict = idictionary;
        wnversion = (new StringBuilder()).append("").append(idictionary.getVersion()).toString();
        roots = new ArrayList();
        read(POS.NOUN);
        read(POS.VERB);
        write();
    }

    private void read(POS pos)
    {
        Object obj = null;
        Iterator iterator = null;
        Object obj1 = null;
        Object obj2 = null;
        iterator = dict.getSynsetIterator(pos);
        do
        {
            if(!iterator.hasNext())
                break;
            ISynset isynset = (ISynset)iterator.next();
            List list = isynset.getRelatedSynsets(Pointer.HYPERNYM);
            List list1 = isynset.getRelatedSynsets(Pointer.HYPERNYM_INSTANCE);
            if(list.isEmpty() && list1.isEmpty())
                roots.add((new StringBuilder()).append("").append(isynset.getID()).toString());
        } while(true);
    }

    private void write()
    {
        try
        {
            BufferedWriter bufferedwriter = new BufferedWriter(new FileWriter((new StringBuilder()).append(wnversion).append(".roots.out").toString()));
            String s;
            for(Iterator iterator = roots.iterator(); iterator.hasNext(); bufferedwriter.write((new StringBuilder()).append(s).append("\n").toString()))
                s = (String)iterator.next();

            bufferedwriter.close();
        }
        catch(IOException ioexception)
        {
            ioexception.printStackTrace();
        }
        System.out.println((new StringBuilder()).append("... ").append(wnversion).append(" done").toString());
    }

    private IDictionary dict;
    private ArrayList roots;
    private String wnversion;
}