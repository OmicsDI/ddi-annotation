package uk.ac.ebi.ddi.extservices.annotator.client;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ddi.extservices.annotator.config.BioOntologyWsConfigProd;


/**
 * Abstract client to query The Tax NCBI to get the Id
 *
 * @author ypriverol
 */

public class WsClient {

    protected RestTemplate restTemplate;
    protected BioOntologyWsConfigProd config;

    /**
     * Default constructor for Archive clients
     *
     * @param config
     */
    public WsClient(BioOntologyWsConfigProd config) {
        this.config = config;
        this.restTemplate = new RestTemplate(clientHttpRequestFactory());
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(200000);
        factory.setConnectTimeout(200000);
        return factory;
    }

    public BioOntologyWsConfigProd getConfig() {
        return config;
    }

    public void setConfig(BioOntologyWsConfigProd config) {
        this.config = config;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

}