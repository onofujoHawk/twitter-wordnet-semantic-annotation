// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 15.17.38
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   CompoundWords.java

package customedu.sussex.nlp.jws;

import java.util.ArrayList;
import java.util.Iterator;

public class CompoundWords
{

    public CompoundWords()
    {
        editor = null;
        store = null;
        temp = null;
        separators = null;
        word = "";
        store = new ArrayList();
        temp = new ArrayList();
        separators = new ArrayList();
        separators.add("-");
        separators.add("_");
        separators.add(" ");
    }

    public ArrayList getCompounds(String s)
    {
        ArrayList arraylist = new ArrayList();
        store.clear();
        editor = s.split("[-_\\s]");
        for(int i = 0; i < editor.length; i++)
        {
            s = editor[i];
            temp.clear();
            if(i == editor.length - 1)
            {
                String s1;
                for(Iterator iterator = store.iterator(); iterator.hasNext(); arraylist.add((new StringBuilder()).append(s1).append(s).toString()))
                    s1 = (String)iterator.next();

                continue;
            }
            for(Iterator iterator1 = separators.iterator(); iterator1.hasNext();)
            {
                String s2 = (String)iterator1.next();
                if(!store.isEmpty())
                {
                    Iterator iterator2 = store.iterator();
                    while(iterator2.hasNext()) 
                    {
                        String s3 = (String)iterator2.next();
                        temp.add((new StringBuilder()).append(s3).append(s).append(s2).toString());
                    }
                } else
                {
                    temp.add((new StringBuilder()).append(s).append(s2).toString());
                }
            }

            if(store.isEmpty())
            {
                store.addAll(temp);
            } else
            {
                store.clear();
                store.addAll(temp);
            }
        }

        return arraylist;
    }

    private String editor[];
    private ArrayList store;
    private ArrayList temp;
    private ArrayList separators;
    private String word;
}