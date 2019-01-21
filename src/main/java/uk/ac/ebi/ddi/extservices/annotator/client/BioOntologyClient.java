package uk.ac.ebi.ddi.extservices.annotator.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ddi.annotation.utils.Constants;
import uk.ac.ebi.ddi.extservices.annotator.config.BioOntologyWsConfigProd;
import uk.ac.ebi.ddi.extservices.annotator.model.AnnotatedOntologyQuery;
import uk.ac.ebi.ddi.extservices.annotator.model.RecomendedOntologyQuery;
import uk.ac.ebi.ddi.extservices.annotator.model.SynonymQuery;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

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
public class BioOntologyClient extends WsClient {


    private static final Logger LOGGER = LoggerFactory.getLogger(BioOntologyClient.class);

    static final ObjectMapper MAPPER = new ObjectMapper();

    static final String REST_URL = "http://data.bioontology.org";

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
    public RecomendedOntologyQuery[] getRecommendedTerms(String query, String[] ontologies)
            throws UnsupportedEncodingException, RestClientException {
        String ontology = getStringfromArray(ontologies);
        query = URLEncoder.encode(query, "UTF-8");

        String url = String.format("%s://%s/recommender?ontologies=%s&apikey=%s&input=%s",
                config.getProtocol(), config.getHostName(), ontology, Constants.OBO_KEY, query);
        return restTemplate.getForObject(url, RecomendedOntologyQuery[].class);

    }

    private String getStringfromArray(String[] ontologies) {

        StringBuilder ontology = new StringBuilder();
        if ((ontologies != null) && (ontologies.length > 0)) {
            int count = 0;
            for (String value : ontologies) {
                if (count == ontologies.length - 1) {
                    ontology.append(value);
                } else {
                    ontology.append(value).append(",");
                }
                count++;
            }
        }
        return ontology.toString();
    }

    /**
     * Retrieve the Recommended term using a query and set of ontologies.
     * @param query Query Text
     * @param ontologies List of ontologies
     * @return
     * @throws UnsupportedEncodingException
     */
    public RecomendedOntologyQuery[] postRecommendedTerms(String query, String[] ontologies)
            throws UnsupportedEncodingException, RestClientException {
        String ontology = getStringfromArray(ontologies);

        query = URLEncoder.encode(query, "UTF-8");

        String url = String.format("%s://%s/recommender?ontologies=%s&apikey=%s&input=%s",
                config.getProtocol(), config.getHostName(), ontology, Constants.OBO_KEY, query);
        return restTemplate.postForObject(url, null, RecomendedOntologyQuery[].class);
    }

    public JsonNode getAnnotatedSynonyms(String query) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(REST_URL)
                .path("/annotator")
                .queryParam("ontologies", getStringfromArray(Constants.OBO_ONTOLOGIES))
                .queryParam("longest_only", true)
                .queryParam("whole_word_only", true)
                .queryParam("include", "prefLabel,synonym,definition")
                .queryParam("max_level", 3);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "apikey token=" + Constants.OBO_KEY);
        headers.add("Content-Type", "application/json");
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("text", query);
        HttpEntity<?> httpEntity = new HttpEntity<>(map, headers);
        URI uri = builder.build().encode().toUri();
        return getRetryTemplate().execute(ctx -> restTemplate.postForObject(uri, httpEntity, JsonNode.class));
    }

    public AnnotatedOntologyQuery[] getAnnotatedTerms(String query, String[] ontologies) throws RestClientException {
        String ontology = getStringfromArray(ontologies);

        String url = String.format(
                "%s://%s/annotator?ontologies=%s&longest_only=true&whole_word_only=false&apikey=%s&text=%s",
                config.getProtocol(), config.getHostName(), ontology, Constants.OBO_KEY, query);

        return restTemplate.getForObject(url, AnnotatedOntologyQuery[].class);

    }

    public SynonymQuery getAllSynonyms(String ontology, String term) throws RestClientException {

        String url = String.format("%s://%s/ontologies/%s/classes/%s?apikey=%s",
                config.getProtocol(), config.getHostName(), ontology, term, Constants.OBO_KEY);
        LOGGER.debug(url);
        System.out.println(url);

        return this.restTemplate.getForObject(url, SynonymQuery.class);
    }

    public SynonymQuery getAllSynonymByURL(String url) throws RestClientException {

        url = String.format("%s?apikey=%s", url, Constants.OBO_KEY);
        return this.restTemplate.getForObject(url, SynonymQuery.class);

    }

    private static JsonNode jsonToNode(String json) throws IOException {
        return MAPPER.readTree(json);
    }

    @Deprecated
    private static String get(String urlToGet, String API_KEY) throws IOException {
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
    }

    @Deprecated
    private static String post(String urlToGet, String urlParameters, String API_KEY) throws IOException {
        URL url;
        HttpURLConnection conn;

        String line;
        String result = "";
        url = new URL(urlToGet);
        LOGGER.debug(urlToGet + urlParameters);
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
    }
    private static void printAnnotations(JsonNode annotations) throws IOException {
        for (JsonNode annotation : annotations) {
            // Get the details for the class that was found in the annotation and print
            JsonNode classDetails = jsonToNode(get(annotation.get("annotatedClass").get("links").get("self").asText(),
                    Constants.OBO_KEY));
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
                    classDetails = jsonToNode(get(
                            hierarchyAnnotation.get("annotatedClass").get("links").get("self").asText(),
                            Constants.OBO_KEY));
                    System.out.println("\t\tClass details");
                    System.out.println("\t\t\tid: " + classDetails.get("@id").asText());
                    System.out.println("\t\t\tprefLabel: " + classDetails.get("prefLabel").asText());
                    System.out.println("\t\t\tontology: " + classDetails.get("links").get("ontology").asText());
                }
            }
        }
    }
}
