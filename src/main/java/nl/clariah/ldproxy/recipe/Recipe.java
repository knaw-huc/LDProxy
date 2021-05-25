package nl.clariah.ldproxy.recipe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.regex.Matcher;
import net.sf.saxon.s9api.XdmItem;

abstract public class Recipe {

    XdmItem config;
    
    public void init(XdmItem config) {
        this.config = config;
    }
    
    abstract public BufferedReader handle(BufferedWriter client, Matcher site);
    
}
