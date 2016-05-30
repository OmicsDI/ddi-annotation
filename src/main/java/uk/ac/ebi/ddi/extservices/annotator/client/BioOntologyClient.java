package uk.ac.ebi.ddi.extservices.annotator.client;

import org.springframework.web.client.RestClientException;
import uk.ac.ebi.ddi.annotation.utils.Constants;
import uk.ac.ebi.ddi.extservices.annotator.config.BioOntologyWsConfigProd;
import uk.ac.ebi.ddi.extservices.annotator.model.AnnotatedOntologyQuery;
import uk.ac.ebi.ddi.extservices.annotator.model.RecomendedOntologyQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ddi.extservices.annotator.model.SynonymQuery;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by yperez on 29/05/2016.
 */
public class BioOntologyClient extends WsClient{


    private static final Logger logger = LoggerFactory.getLogger(BioOntologyClient.class);

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
    public RecomendedOntologyQuery[] getRecommendedTerms(String query, String[] ontologies) throws UnsupportedEncodingException {
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

        query = URLEncoder.encode(query, "UTF-8");

        String url = String.format("%s://%s/recommender?ontologies=%s&apikey=%s&input=%s",
                config.getProtocol(), config.getHostName(), ontology, Constants.OBO_KEY, query);
        logger.debug(url);
        System.out.println(url);

        return this.restTemplate.getForObject(url, RecomendedOntologyQuery[].class);

    }

    /**
     * Retrieve the Recommended term using a query and set of ontologies.
     * @param query Query Text
     * @param ontologies List of ontologies
     * @return
     * @throws UnsupportedEncodingException
     */
    public RecomendedOntologyQuery[] postRecommendedTerms(String query, String[] ontologies) throws UnsupportedEncodingException, RestClientException {
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

        query = URLEncoder.encode(query, "UTF-8");

        String url = String.format("%s://%s/recommender?ontologies=%s&apikey=%s&input=%s",
                config.getProtocol(), config.getHostName(), ontology, Constants.OBO_KEY, query);

        logger.debug(url);
        System.out.println(url);

        return this.restTemplate.postForObject(url, null, RecomendedOntologyQuery[].class);

    }

    public AnnotatedOntologyQuery[] getAnnotatedTerms(String query, String[] ontologies){
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

        String url = String.format("%s://%s/annotator?ontologies=%s&longest_only=true&whole_word_only=false&apikey=%s&text=%s",
                config.getProtocol(), config.getHostName(), ontology, Constants.OBO_KEY, query);
        logger.debug(url);

        return this.restTemplate.getForObject(url, AnnotatedOntologyQuery[].class);

    }

    public SynonymQuery getAllSynonyms(String ontology, String term){

        String url = String.format("%s://%s/ontologies/%s/classes/%s?apikey=%s",
                config.getProtocol(), config.getHostName(), ontology, term, Constants.OBO_KEY);
        logger.debug(url);
        System.out.println(url);

        return this.restTemplate.getForObject(url, SynonymQuery.class);
    }

    public SynonymQuery getAllSynonymByURL(String url) throws RestClientException, UnsupportedEncodingException {

        url = String.format("%s?apikey=%s", url, Constants.OBO_KEY);
        logger.debug(url);
        System.out.println(url);

        return this.restTemplate.getForObject(url, SynonymQuery.class);

    }



}
