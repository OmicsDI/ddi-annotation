package uk.ac.ebi.ddi.extservices.uniprot;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 03/12/2015
 */
public class UniprotIdentifier {

    private static final String UNIPROT_SERVER = "http://www.uniprot.org/";
    private static final Logger LOG = Logger.getAnonymousLogger();

    private static List<String> run(String tool, ParameterNameValue[] params) throws Exception{
        List<String> newIdentifiers = new ArrayList<String>();

        StringBuilder locationBuilder = new StringBuilder(UNIPROT_SERVER + tool + "/?");
        for (int i = 0; i < params.length; i++){
            if (i > 0)
                locationBuilder.append('&');
            locationBuilder.append(params[i].name).append('=').append(params[i].value);
        }

        String location = locationBuilder.toString();
        URL url = new URL(location);
        LOG.info("Submitting...");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        HttpURLConnection.setFollowRedirects(true);

        conn.setDoInput(true);

        conn.connect();

        int status = conn.getResponseCode();
        while (true){
            int wait = 0;
            String header = conn.getHeaderField("Retry-After");
            if (header != null)
                wait = Integer.valueOf(header);
            if (wait == 0)
                break;
            LOG.info("Waiting (" + wait + ")...");
            conn.disconnect();
            Thread.sleep(wait * 1000);
            conn = (HttpURLConnection) new URL(location).openConnection();
            conn.setDoInput(true);
            conn.connect();
            status = conn.getResponseCode();
        }
        if (status == HttpURLConnection.HTTP_OK){
            LOG.info("Got a OK reply");
            InputStream reader = conn.getInputStream();
            URLConnection.guessContentTypeFromStream(reader);
            StringBuilder builder = new StringBuilder();
            int a = 0;
            while ((a = reader.read()) != -1)
                builder.append((char) a);
            System.out.println(builder.toString());
        }else
            LOG.severe("Failed, got " + conn.getResponseMessage() + " for " + location);
        conn.disconnect();
        return newIdentifiers;
    }

    public static List<String> retrieve (List<String> identifiers, String fromAccession, String toAccession){
        String identiferString = "";
        if(identifiers != null && identifiers.size() > 0){
            for(String id: identifiers)
                identiferString = id + " " + identiferString;
            identiferString.trim();
        }
        List<String> newIdentifiers = null;
        try {
            newIdentifiers = run("mapping", new ParameterNameValue[] {
                    new ParameterNameValue("from", fromAccession),
                    new ParameterNameValue("to", toAccession),
                    new ParameterNameValue("format", "tab"),
                    new ParameterNameValue("query", identiferString),
            });
        } catch (Exception e) {
            LOG.severe(e.getMessage());
        }
        return newIdentifiers;
    }

    private static class ParameterNameValue {

        private final String name;
        private final String value;

        public ParameterNameValue(String name, String value)
                throws UnsupportedEncodingException
        {
            this.name = URLEncoder.encode(name, "UTF-8");
            this.value = URLEncoder.encode(value, "UTF-8");
        }
    }



}
