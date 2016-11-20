// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 15.19.36
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   LeskGlossOverlaps.java

package customedu.sussex.nlp.jws;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.morph.WordnetStemmer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LeskGlossOverlaps
{

    public LeskGlossOverlaps(IDictionary idictionary)
    {
        usestoplist = true;
        uselemmatiser = true;
        dict = null;
        stemmer = null;
        list = "a aboard about above across after against all along alongside although amid amidst among amongst an and another anti any anybody anyone anything around as astride at aught bar barring because before behind below beneath beside besides between beyond both but by circa concerning considering despite down during each either enough everybody everyone except excepting excluding few fewer following for from he her hers herself him himself his hisself i idem if ilk in including inside into it its itself like many me mine minus more most myself naught near neither nobody none nor nothing notwithstanding of off on oneself onto opposite or other otherwise our ourself ourselves outside over own past pending per plus regarding round save self several she since so some somebody someone something somewhat such suchlike sundry than that the thee theirs them themselves there they thine this thou though through throughout thyself till to tother toward towards twain under underneath unless unlike until up upon us various versus via vis-a-vis we what whatall whatever whatsoever when whereas wherewith wherewithal which whichever whichsoever while who whoever whom whomever whomso whomsoever whose whosoever with within without worth ye yet yon yonder you you-all yours yourself";
        stoplist = null;
        p = null;
        m = null;
        dict = idictionary;
        p = Pattern.compile("\\b[a-zA-Z0-9-']+\\b");
        stemmer = new WordnetStemmer(idictionary);
        stoplist = new ArrayList();
        getStopWords();
    }

    public void useStopList(boolean flag)
    {
        usestoplist = flag;
    }

    public void useLemmatiser(boolean flag)
    {
        uselemmatiser = flag;
    }

    private void getStopWords()
    {
        String as[] = list.split("\\s");
        for(int i = 0; i < as.length; i++)
            stoplist.add(as[i]);

    }

    private ArrayList getWords(String s)
    {
        ArrayList arraylist = new ArrayList();
        String as[] = s.toLowerCase().split("\\s+");
        String s1 = "";
        for(int i = 0; i < as.length; i++)
        {
            String s2 = as[i].trim();
            m = p.matcher(s2);
            if(m.find())
                arraylist.add(m.group());
        }

        return arraylist;
    }

    private boolean containsContentWords(List list1)
    {
label0:
        {
            if(!usestoplist && !uselemmatiser)
                return true;
            if(!usestoplist || !uselemmatiser)
                break label0;
            Iterator iterator = list1.iterator();
            String s;
            List list2;
            do
            {
                if(!iterator.hasNext())
                    break label0;
                s = (String)iterator.next();
                list2 = stemmer.findStems(s);
            } while(list2.isEmpty() || stoplist.contains(s));
            return true;
        }
label1:
        {
            if(!usestoplist || uselemmatiser)
                break label1;
            Iterator iterator1 = list1.iterator();
            String s1;
            do
            {
                if(!iterator1.hasNext())
                    break label1;
                s1 = (String)iterator1.next();
            } while(stoplist.contains(s1));
            return true;
        }
label2:
        {
            if(usestoplist || !uselemmatiser)
                break label2;
            Iterator iterator2 = list1.iterator();
            List list3;
            do
            {
                if(!iterator2.hasNext())
                    break label2;
                String s2 = (String)iterator2.next();
                list3 = stemmer.findStems(s2);
            } while(list3.isEmpty());
            return true;
        }
        return false;
    }

    private ArrayList builder(ArrayList arraylist)
    {
        ArrayList arraylist1 = new ArrayList();
        int i = 1;
        for(int j = 0; j < arraylist.size(); j++)
        {
            for(int k = 0; k < arraylist.size(); k++)
                if(k + i <= arraylist.size())
                    arraylist1.add(0, arraylist.subList(k, k + i));

            i++;
        }

        return arraylist1;
    }

    public double overlap(String s, String s1)
    {
        double d = 0.0D;
        ArrayList arraylist = getWords(s);
        ArrayList arraylist1 = getWords(s1);
        if(s.equals(s1) && containsContentWords(arraylist))
        {
            d = Math.pow(arraylist.size(), 2D);
            return d;
        }
        ArrayList arraylist2 = builder(arraylist);
        ArrayList arraylist4 = builder(arraylist1);
        ArrayList arraylist6 = new ArrayList();
        arraylist6.addAll(arraylist2);
        arraylist6.retainAll(arraylist4);
        if(arraylist6.isEmpty())
            return 0.0D;
        ArrayList arraylist7 = getWords(s);
        ArrayList arraylist8 = getWords(s1);
        ArrayList arraylist9 = new ArrayList();
        ArrayList arraylist5;
        for(; !arraylist6.isEmpty(); arraylist6.retainAll(arraylist5))
        {
            List list1 = (List)arraylist6.remove(0);
            ArrayList arraylist10 = new ArrayList();
            arraylist10.addAll(list1);
            arraylist9.add(arraylist10);
            String s2;
            for(Iterator iterator1 = arraylist10.iterator(); iterator1.hasNext(); arraylist8.remove(s2))
            {
                s2 = (String)iterator1.next();
                arraylist7.remove(s2);
            }

            ArrayList arraylist3 = builder(arraylist7);
            arraylist5 = builder(arraylist8);
            arraylist6.clear();
            arraylist6.addAll(arraylist3);
        }

        Iterator iterator = arraylist9.iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            List list2 = (List)iterator.next();
            if(containsContentWords(list2))
                d += Math.pow(list2.size(), 2D);
        } while(true);
        return d;
    }

    private boolean usestoplist;
    private boolean uselemmatiser;
    private IDictionary dict;
    private WordnetStemmer stemmer;
    private String list;
    private ArrayList stoplist;
    private Pattern p;
    private Matcher m;
}