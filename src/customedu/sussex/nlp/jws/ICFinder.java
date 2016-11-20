// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 15.18.32
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   ICFinder.java

package customedu.sussex.nlp.jws;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ICFinder
{

    public ICFinder(String s)
    {
        p = null;
        m = null;
        editor = null;
        icfilename = "";
        in = null;
        line = "";
        lookup = null;
        nouns_sum = 0.0D;
        verbs_sum = 0.0D;
        nounsandverbs_sum = 0.0D;
        nounroot_sum = 0.0D;
        verbroot_sum = 0.0D;
        nounroots = null;
        verbroots = null;
        //System.out.println("... calculating IC <roots> ...");
        //System.out.println("... ICFinder");
        icfilename = s;
        lookup = new Hashtable();
        nounroots = new ArrayList();
        verbroots = new ArrayList();
        Vector vector = setup();
        nouns_sum = ((Double)vector.get(0)).doubleValue();
        verbs_sum = ((Double)vector.get(1)).doubleValue();
        nounsandverbs_sum = nouns_sum + verbs_sum;
        nounroot_sum = ((Double)vector.get(2)).doubleValue();
        verbroot_sum = ((Double)vector.get(3)).doubleValue();
    }

    public double getRootSum(String s)
    {
        if(s.equalsIgnoreCase("v"))
            return verbroot_sum;
        else
            return nounroot_sum;
    }

    private double getFrequency(String s, String s1)
    {
        if(lookup.containsKey((new StringBuilder()).append(s).append(s1).toString()))
            return ((Double)lookup.get((new StringBuilder()).append(s).append(s1).toString())).doubleValue();
        else
            return 0.0D;
    }

    private double getProbability(String s, String s1)
    {
        double d = getFrequency(s, s1);
        if(d == 0.0D)
            return 0.0D;
        double d1 = 0.0D;
        if(s1.equalsIgnoreCase("n"))
            d1 = d / nounroot_sum;
        if(s1.equalsIgnoreCase("v"))
            d1 = d / verbroot_sum;
        return d1;
    }

    public double getIC(String s, String s1)
    {
        double d = 0.0D;
        if(s == null || s.length() == 0)
            return d;
        double d2 = getProbability(s, s1);
        if(d2 == 0.0D)
        {
            return d;
        } else
        {
            double d1 = -Math.log(d2);
            return d1;
        }
    }

    private Vector setup()
    {
        String s = "";
        double d = 0.0D;
        double d3 = 0.0D;
        double d4 = 0.0D;
        double d5 = 0.0D;
        double d6 = 0.0D;
        Vector vector = new Vector();
        try
        {
            in = new BufferedReader(new FileReader(icfilename));
            while((line = in.readLine()) != null) 
            {
                editor = line.split("\\s");
                int i = 0;
                while(i < editor.length) 
                {
                    String s1 = editor[i];
                    if(s1.endsWith("n"))
                    {
                        lookup.put(editor[0], Double.valueOf(Double.parseDouble(editor[1])));
                        double d1 = Double.parseDouble(editor[1]);
                        d3 += d1;
                        if(editor.length == 3)
                        {
                            d5 += d1;
                            nounroots.add(editor[0].substring(0, editor[0].length() - 1));
                        }
                    }
                    if(s1.endsWith("v"))
                    {
                        lookup.put(editor[0], Double.valueOf(Double.parseDouble(editor[1])));
                        double d2 = Double.parseDouble(editor[1]);
                        d4 += d2;
                        if(editor.length == 3)
                        {
                            d6 += d2;
                            verbroots.add(editor[0].substring(0, editor[0].length() - 1));
                        }
                    }
                    i++;
                }
            }
            in.close();
        }
        catch(IOException ioexception)
        {
            ioexception.printStackTrace();
        }
        vector.add(Double.valueOf(d3));
        vector.add(Double.valueOf(d4));
        vector.add(Double.valueOf(d5));
        vector.add(Double.valueOf(d6));
        return vector;
    }

    public ArrayList getNounRoots()
    {
        return nounroots;
    }

    public ArrayList getVerbRoots()
    {
        return verbroots;
    }

    private Pattern p;
    private Matcher m;
    private String editor[];
    private String icfilename;
    private BufferedReader in;
    private String line;
    private Hashtable lookup;
    private double nouns_sum;
    private double verbs_sum;
    private double nounsandverbs_sum;
    private double nounroot_sum;
    private double verbroot_sum;
    private ArrayList nounroots;
    private ArrayList verbroots;
}