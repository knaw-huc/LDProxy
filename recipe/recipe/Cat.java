package nl.clariah.ldproxy.recipe;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import net.sf.saxon.s9api.XdmItem;
import nl.mpi.tla.util.Saxon;

public class Cat extends nl.clariah.ldproxy.recipe.Recipe {

    @Override
    public BufferedReader handle(BufferedWriter client,Matcher site) {
        BufferedReader res = null;
        try {
            res = new BufferedReader(new InputStreamReader(new FileInputStream(new File(Saxon.xpath2string(this.config, "file/@src")))));
        } catch (Exception e) {
            System.out.println("!ERR: couldn't finish the LDProxy recipe["+this.getClass()+"]! \n"+e.getMessage());
            e.printStackTrace(System.out);
        }
        return res;
    }
    
}
