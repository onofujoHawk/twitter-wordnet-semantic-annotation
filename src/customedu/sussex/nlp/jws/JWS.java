// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 11/01/2011 12.45.38
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   JWS.java

package customedu.sussex.nlp.jws;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import java.io.File;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import customedu.sussex.nlp.jws.*;
// Referenced classes of package edu.sussex.nlp.jws:
//            ICFinder, DepthFinder, PathFinder, JiangAndConrath, 
//            Lin, Resnik, Path, WuAndPalmer, 
//            AdaptedLesk, AdaptedLeskTanimoto, AdaptedLeskTanimotoNoHyponyms, HirstAndStOnge, 
//            LeacockAndChodorow

public class JWS
{

    public JWS(String s, String s1, String s2)
    {
        rootdir = "";
        wnhome = "";
        icfilename = "";
        url = null;
        dict = null;
        roots = null;
        icfinder = null;
        depthfinder = null;
        pathfinder = null;
        jc = null;
        lin = null;
        res = null;
        path = null;
        wup = null;
        leskO = null;
        lesk1 = null;
        lesk2 = null;
        lch = null;
        hso = null;
        System.out.println("Loading modules");
        wnhome = (new StringBuilder()).append(s).append("/").append("/dict").toString();
        icfilename = (new StringBuilder()).append(s).append("/").append("/WordNet-InfoContent-").append(s1).append("/").append(s2).toString();
        if(!exists(wnhome) || !exists(icfilename))
        {
            System.out.println((new StringBuilder()).append("your directory paths are wrong:\n").append(wnhome).append("\n").append(icfilename).toString());
            System.exit(1);
        }
        System.out.println("set up:");
        initialiseWordNet();
        System.out.println("... finding noun and verb <roots>");
        roots = new ArrayList();
        getRoots(POS.NOUN);
        getRoots(POS.VERB);
        icfinder = new ICFinder(icfilename);
        depthfinder = new DepthFinder(dict, icfilename);
        pathfinder = new PathFinder(dict);
        jc = new JiangAndConrath(dict, icfinder);
        lin = new Lin(dict, icfinder);
        res = new Resnik(dict, icfinder);
        path = new Path(dict, roots);
        wup = new WuAndPalmer(dict, roots);
        leskO = new AdaptedLesk(dict);
        lesk1 = new AdaptedLeskTanimoto(dict);
        lesk2 = new AdaptedLeskTanimotoNoHyponyms(dict);
        hso = new HirstAndStOnge(dict);
        lch = new LeacockAndChodorow(dict, roots);
        System.out.println((new StringBuilder()).append("\n\nJava WordNet::Similarity using WordNet ").append(dict.getVersion()).append(" : loaded\n\n\n").toString());
    }

    public JWS(String s, String s1)
    {
        rootdir = "";
        wnhome = "";
        icfilename = "";
        url = null;
        dict = null;
        roots = null;
        icfinder = null;
        depthfinder = null;
        pathfinder = null;
        jc = null;
        lin = null;
        res = null;
        path = null;
        wup = null;
        leskO = null;
        lesk1 = null;
        lesk2 = null;
        lch = null;
        hso = null;
        System.out.println("Loading modules");
        wnhome = (new StringBuilder()).append(s).append("/").append("/dict").toString();
        icfilename = (new StringBuilder()).append(s).append("/").append("/WordNet-InfoContent-").append(s1).append("/ic-semcor.dat").toString();
        if(!exists(wnhome) || !exists(icfilename))
        {
            System.out.println((new StringBuilder()).append("your directory paths are wrong:\n").append(wnhome).append("\n").append(icfilename).toString());
           // System.exit(1);
        }
        System.out.println("set up:");
        initialiseWordNet();
        System.out.println("... finding noun and verb <roots>");
        roots = new ArrayList();
        getRoots(POS.NOUN);
        getRoots(POS.VERB);
        icfinder = new ICFinder(icfilename);
        depthfinder = new DepthFinder(dict, icfilename);
        pathfinder = new PathFinder(dict);
        jc = new JiangAndConrath(dict, icfinder);
        lin = new Lin(dict, icfinder);
        res = new Resnik(dict, icfinder);
        path = new Path(dict, roots);
        wup = new WuAndPalmer(dict, roots);
        leskO = new AdaptedLesk(dict);
        lesk1 = new AdaptedLeskTanimoto(dict);
        lesk2 = new AdaptedLeskTanimotoNoHyponyms(dict);
        hso = new HirstAndStOnge(dict);
        lch = new LeacockAndChodorow(dict, roots);
        System.out.println((new StringBuilder()).append("\n\nJava WordNet::Similarity using WordNet ").append(dict.getVersion()).append(" : loaded\n\n\n").toString());
    }

    private void getRoots(POS pos)
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
                roots.add(isynset.getID());
        } while(true);
    }

    private boolean exists(String s)
    {
        return (new File(s)).exists();
    }

    private void initialiseWordNet()
    {
        try
        {
            url = new URL("file", null, wnhome);
        }
        catch(MalformedURLException malformedurlexception)
        {
            malformedurlexception.printStackTrace();
        }
        if(url == null)
        {
            return;
        } else
        {
            dict = new Dictionary(url);
            dict.open();
            return;
        }
    }

    public JiangAndConrath getJiangAndConrath()
    {
        return jc;
    }

    public Lin getLin()
    {
        return lin;
    }

    public Resnik getResnik()
    {
        return res;
    }

    public Path getPath()
    {
        return path;
    }

    public WuAndPalmer getWuAndPalmer()
    {
        return wup;
    }

    public AdaptedLesk getAdaptedLesk()
    {
        return leskO;
    }
    
    public AdaptedLeskTanimoto getAdaptedLeskTanimoto()
    {
        return lesk1;
    }

    public AdaptedLeskTanimotoNoHyponyms getAdaptedLeskTanimotoNoHyponyms()
    {
        return lesk2;
    }

    public LeacockAndChodorow getLeacockAndChodorow()
    {
        return lch;
    }

    public HirstAndStOnge getHirstAndStOnge()
    {
        return hso;
    }

    public IDictionary getDictionary()
    {
        return dict;
    }

    private String rootdir;
    private String wnhome;
    private String icfilename;
    private URL url;
    private IDictionary dict;
    private ArrayList roots;
    private ICFinder icfinder;
    private DepthFinder depthfinder;
    private PathFinder pathfinder;
    private JiangAndConrath jc;
    private Lin lin;
    private Resnik res;
    private Path path;
    private WuAndPalmer wup;
    private AdaptedLesk leskO;
    private AdaptedLeskTanimoto lesk1;
    private AdaptedLeskTanimotoNoHyponyms lesk2;
    private LeacockAndChodorow lch;
    private HirstAndStOnge hso;
}