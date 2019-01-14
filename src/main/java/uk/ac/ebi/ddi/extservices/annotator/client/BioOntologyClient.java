package uk.ac.ebi.ddi.extservices.annotator.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClientException;
import uk.ac.ebi.ddi.annotation.utils.Constants;
import uk.ac.ebi.ddi.extservices.annotator.config.BioOntologyWsConfigProd;
import uk.ac.ebi.ddi.extservices.annotator.model.AnnotatedOntologyQuery;
import uk.ac.ebi.ddi.extservices.annotator.model.RecomendedOntologyQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ddi.extservices.annotator.model.SynonymQuery;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 *  ==Overview==
 *
 *  This class
 *
 * Created by ypriverol (ypriverol@gmail.com) on 29/05/2016.
 */
public class BioOntologyClient extends WsClient{


    private static final Logger logger = LoggerFactory.getLogger(BioOntologyClient.class);

    static final ObjectMapper mapper = new ObjectMapper();

    static String REST_URL = "http://data.bioontology.org";

    private static final int RETRIES = 5;
    private static RetryTemplate template = new RetryTemplate();

    static {
        SimpleRetryPolicy policy =
                new SimpleRetryPolicy(RETRIES, Collections.singletonMap(Exception.class, true));
        template.setRetryPolicy(policy);
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(2000);
        backOffPolicy.setMultiplier(1.6);
        template.setBackOffPolicy(backOffPolicy);
    }

    /**
     * Default constructor for Archive clients
     *
     * @param config
     */
    public BioOntologyClient(BioOntologyWsConfigProd config) {
        super(config);
    }

    /**
     * Retrieve the Recommended term using a query and set of ontologies.
     * @param query Query Text
     * @param ontologies List of ontologies
     * @return
     * @throws UnsupportedEncodingException
     */
    public RecomendedOntologyQuery[] getRecommendedTerms(String query, String[] ontologies) throws UnsupportedEncodingException, RestClientException {
        String ontology = getStringfromArray(ontologies);
        query = URLEncoder.encode(query, "UTF-8");

        String url = String.format("%s://%s/recommender?ontologies=%s&apikey=%s&input=%s",
                config.getProtocol(), config.getHostName(), ontology, Constants.OBO_KEY, query);
        logger.debug(url);
        System.out.println(url);

        return this.restTemplate.getForObject(url, RecomendedOntologyQuery[].class);

    }

    private String getStringfromArray(String[] ontologies) {

        String ontology = "";
        if((ontologies != null) && (ontologies.length > 0)) {
            int count = 0;
            for (String value : ontologies) {
                if (count == ontologies.length - 1)
                    ontology = ontology + value;
                else
                    ontology = ontology + value + ",";
                count++;
            }
        }
        return ontology;
    }

    /**
     * Retrieve the Recommended term using a query and set of ontologies.
     * @param query Query Text
     * @param ontologies List of ontologies
     * @return
     * @throws UnsupportedEncodingException
     */
    public RecomendedOntologyQuery[] postRecommendedTerms(String query, String[] ontologies) throws UnsupportedEncodingException, RestClientException {
        String ontology = getStringfromArray(ontologies);

        query = URLEncoder.encode(query, "UTF-8");

        String url = String.format("%s://%s/recommender?ontologies=%s&apikey=%s&input=%s",
                config.getProtocol(), config.getHostName(), ontology, Constants.OBO_KEY, query);

        logger.debug(url);
        System.out.println(url);

        return this.restTemplate.postForObject(url, null, RecomendedOntologyQuery[].class);

    }

    public JsonNode getAnnotatedSynonyms(String query) throws IOException {

        String urlParameters;
        JsonNode annotations;
        String textToAnnotate = URLEncoder.encode(query, "ISO-8859-1");
        String ontologies = String.format("ontologies=%s&", getStringfromArray(Constants.OBO_ONTOLOGIES));

        // Annotations using POST (necessary for long text)
        urlParameters = ontologies + "&longest_only=true&whole_word_only=true&include=prefLabel,synonym,definition&max_level=3&text=" + textToAnnotate;
        annotations = jsonToNode(post(REST_URL + "/annotator", urlParameters, Constants.OBO_KEY));
        //printAnnotations(annotations);

        return annotations;

    }

    public AnnotatedOntologyQuery[] getAnnotatedTerms(String query, String[] ontologies) throws RestClientException{
        String ontology = getStringfromArray(ontologies);

        String url = String.format("%s://%s/annotator?ontologies=%s&longest_only=true&whole_word_only=false&apikey=%s&text=%s",
                config.getProtocol(), config.getHostName(), ontology, Constants.OBO_KEY, query);
        logger.debug(url);

        return this.restTemplate.getForObject(url, AnnotatedOntologyQuery[].class);

    }

    public SynonymQuery getAllSynonyms(String ontology, String term) throws RestClientException{

        String url = String.format("%s://%s/ontologies/%s/classes/%s?apikey=%s",
                config.getProtocol(), config.getHostName(), ontology, term, Constants.OBO_KEY);
        logger.debug(url);
        System.out.println(url);

        return this.restTemplate.getForObject(url, SynonymQuery.class);
    }

    public SynonymQuery getAllSynonymByURL(String url) throws RestClientException {

        url = String.format("%s?apikey=%s", url, Constants.OBO_KEY);
        logger.debug(url);
        System.out.println(url);

        return this.restTemplate.getForObject(url, SynonymQuery.class);

    }

    private static JsonNode jsonToNode(String json) throws IOException {
        return mapper.readTree(json);
    }

    private static String get(String urlToGet, String API_KEY) throws IOException {
        try {
            return template.execute(context -> {
                URL url;
                HttpURLConnection conn;
                String line;
                String result = "";
                url = new URL(urlToGet);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
                conn.setRequestProperty("Accept", "application/json");
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    while ((line = rd.readLine()) != null) {
                        result += line;
                    }
                }
                conn.disconnect();
                return result;
            });
        } catch (IOException e) {
            logger.error("Exception occurred while fetching API {}, ", urlToGet, e);
            throw e;
        }
    }

    private static String post(String urlToGet, String urlParameters, String API_KEY) throws IOException {
        try {
            return template.execute(context -> {
                URL url;
                HttpURLConnection conn;

                String line;
                String result = "";
                url = new URL(urlToGet);
                logger.debug(urlToGet + urlParameters);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("charset", "utf-8");
                conn.setUseCaches(false);

                try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                    wr.writeBytes(urlParameters);
                    wr.flush();
                }
                conn.disconnect();
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    while ((line = rd.readLine()) != null) {
                        result += line;
                    }
                }
                return result;
            });
        } catch (IOException e) {
            logger.error("Exception occurred while fetching API " + urlToGet + ", {}", urlParameters, e);
            throw e;
        }
    }
    private static void printAnnotations(JsonNode annotations) throws IOException {
        for (JsonNode annotation : annotations) {
            // Get the details for the class that was found in the annotation and print
            JsonNode classDetails = jsonToNode(get(annotation.get("annotatedClass").get("links").get("self").asText(), Constants.OBO_KEY));
            System.out.println("Class details");
            System.out.println("\tid: " + classDetails.get("@id").asText());
            System.out.println("\tprefLabel: " + classDetails.get("prefLabel").asText());
            System.out.println("\tontology: " + classDetails.get("links").get("ontology").asText());
            System.out.println("\n");

            JsonNode hierarchy = annotation.get("hierarchy");
            // If we have hierarchy annotations, print the related class information as well
            if (hierarchy.isArray() && hierarchy.elements().hasNext()) {
                System.out.println("\tHierarchy annotations");
                for (JsonNode hierarchyAnnotation : hierarchy) {
                    classDetails = jsonToNode(get(hierarchyAnnotation.get("annotatedClass").get("links").get("self").asText(), Constants.OBO_KEY));
                    System.out.println("\t\tClass details");
                    System.out.println("\t\t\tid: " + classDetails.get("@id").asText());
                    System.out.println("\t\t\tprefLabel: " + classDetails.get("prefLabel").asText());
                    System.out.println("\t\t\tontology: " + classDetails.get("links").get("ontology").asText());
                }
            }
        }
    }






}
