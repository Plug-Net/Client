package pnet.internal;

import pnet.internal.internalLang.DNSLangInterpreter;
import pnet.internal.proxy.Proxy;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Plugner
 * @since 12/24/2020
 * @apiNote This is the main class in the proxy system.
 */

public class PlugNetProxy {

    public static HashMap<String, String> redirects = new HashMap<>();

    /**
     * @param args = The arguments to execute (non)
     * @since 12/24/2020
     * @apiNote Do not use this function while develop addons.
     */

    public static void main(String[] args) throws IOException {
         Variables.setupDNSURIs();
         Variables.DNS_URIs.forEach(url -> {
             System.out.println(url);
             DNSLangInterpreter.Interpreter.getDataFromURL(url);
         });

        Proxy proxy = new Proxy(7500);
        proxy.listen();
    }

    /**
     * @since 12/24/2020
     * @author Plugner
     * @apiNote This is where the Proxy variables is allocated.
     */

    public static class Variables {
        public static HashMap<Object, Object> postAllocatedVariables = new HashMap();
        public Variables(Object key, Object value) {
            postAllocatedVariables.put(key, value);
        }

        public static URL DNS_DNSListURI;

        static {
            try {
                DNS_DNSListURI = new URL("https://raw.githubusercontent.com/Plug-Net/DNS/main/DNSList.pnetdnslist");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        public static ArrayList<URL> DNS_URIs = new ArrayList<>();
        public static void setupDNSURIs() throws IOException {
            HttpURLConnection httpConnection = (HttpURLConnection) DNS_DNSListURI.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            String content = "";
            while(content != null) {
                content = reader.readLine();
                if(content != null) DNS_URIs.add(new URL(content));
            }
        }
    }
}
