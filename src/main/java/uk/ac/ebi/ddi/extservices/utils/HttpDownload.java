package uk.ac.ebi.ddi.extservices.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 09/11/15
 */
public class HttpDownload {

    private static final Logger logger = LoggerFactory.getLogger(HttpDownload.class);

    public static InputStream getPage(String urlString) throws Exception {
        URL url = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(10000); //set timeout to 10 seconds

        connection.setReadTimeout(300000); // set timeout to 10 seconds

        connection.connect();

        return connection.getInputStream();
    }
}
