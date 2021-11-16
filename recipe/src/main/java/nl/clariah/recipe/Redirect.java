package nl.clariah.recipe;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import nl.mpi.tla.util.Saxon;

public class Redirect extends nl.clariah.recipe.Recipe {

    @Override
    public BufferedReader handle(BufferedWriter client,Matcher site) {
        BufferedReader res = null;
        try {
            String code = Saxon.xpath2string(this.config,"redir/@code");
            
            String avt = Saxon.xpath2string(this.config,"redir/@url");
            System.out.println("?DBG: avt["+avt+"]");

            final Field namedGroups = site.pattern().getClass().getDeclaredField("namedGroups");
            namedGroups.setAccessible(true);
            final Map<String, Integer> nameToGroupIndex = (Map<String, Integer>) namedGroups.get(site.pattern());

            Map<String,XdmValue> vars = new HashMap<>();
            for(String group:nameToGroupIndex.keySet()) {
                vars.put(group, new XdmAtomicValue(site.group(group)));
            }
            
            String redir = Saxon.avt(avt, config, vars);
            System.out.println("?DBG: redirect["+redir+"]");
            
            String response = "HTTP/1.0 "+code+"\n" +
            "Location: "+redir+"\n" +
            "Proxy-agent: LDProxyServer/1.0\n" +
            "\r\n";
            client.write(response);
            client.flush();

        } catch (Exception e) {
            System.out.println("!ERR: couldn't finish the LDProxy recipe["+this.getClass()+"]! \n"+e.getMessage());
            e.printStackTrace(System.out);
        }
        return null;
    }
}
