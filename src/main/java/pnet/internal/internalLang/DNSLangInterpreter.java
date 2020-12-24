package pnet.internal.internalLang;

import pnet.internal.PlugNetProxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DNSLangInterpreter {
    public static class Interpreter {
        public static ArrayList<Object> getDataFromURL(URL url) {
            try {
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));

                boolean hookIndex = false;
                boolean hookEnd = false;

                String line = "";
                while(line != null) {
                    line = reader.readLine();
                    if(line == null) {
                        break;
                    }
                    Map<LangCodeTypes, Object[]> langInterpreterResponse = interpretLine(line);

                    LangCodeTypes type = langInterpreterResponse.keySet().stream().findFirst().get();
                    if(type == LangCodeTypes.INDEX) {
                        if(hookIndex && !hookEnd) {
                            throw new InternalLanguageException("Already indexed without end");
                        }
                        hookIndex = true;
                        hookEnd = false;
                    }
                    if(type == LangCodeTypes.END) {
                        if(hookEnd) {
                            throw new InternalLanguageException("Two END statement in a row without an INDEX declaration");
                        }
                        hookEnd = true;
                        hookIndex = false;
                    }
                    if(type == LangCodeTypes.COMMENT) {
                        Object comment = langInterpreterResponse.values().stream().findFirst().get()[0];
                        System.out.println("Read comment: " + comment);
                    }
                    if(type == LangCodeTypes.DEFINEVARIABLE) {
                        Object[] args = langInterpreterResponse.values().stream().findFirst().get();
                        Object key = args[0];
                        Object value = args[1];
                        System.out.println("Read var: KEY(" + key + "):VALUE("+value+")");
                        PlugNetProxy.Variables variable = new PlugNetProxy.Variables(key, value);
                    }
                    if(type == LangCodeTypes.SETURL) {
                        Object[] args = langInterpreterResponse.values().stream().findFirst().get();
                        System.out.println("Reading URL Redirect: When listen " + args[0] + " send to " + args[1]);
                        PlugNetProxy.redirects.put((String)args[0]+ "." + PlugNetProxy.Variables.postAllocatedVariables.get("ListDomain"), (String) args[1] );
                    }
                    //


                }
            } catch (IOException | InternalLanguageException e) {
                e.printStackTrace();
            }
            return null;
        }

        public static Map<LangCodeTypes, Object[]> interpretLine(String line) {
            LangCodeTypes type =
                    line.startsWith("COMMENT>") ? LangCodeTypes.COMMENT :
                    line.startsWith("IND") ? LangCodeTypes.INDEX :
                    line.startsWith("DEF>") ? LangCodeTypes.DEFINEVARIABLE :
                    line.startsWith("END") ? LangCodeTypes.END :
                    line.startsWith("SetURL") ? LangCodeTypes.SETURL :
                    LangCodeTypes.NONE;

            Object[] args = null;
            if(type == LangCodeTypes.DEFINEVARIABLE) {
             String parsedLine = line.replace("DEF>", "");
             args = parsedLine.split(":");
            }
            if(type == LangCodeTypes.COMMENT) {
                String parsedLine = line.replace("COMMENT>", "");
                args = new Object[] {parsedLine};
            }
            if(type == LangCodeTypes.SETURL) {
                String parsedLine = line.replace("SetURL>", "");
                args = parsedLine.split("\\$");
            }

            HashMap<LangCodeTypes, Object[]> map = new HashMap<>();
            map.put(type,args);
            return map;
        }

        static enum LangCodeTypes {
            INDEX,
            END,
            COMMENT,
            DEFINEVARIABLE,
            SETURL,
            NONE
        }
    }
}
