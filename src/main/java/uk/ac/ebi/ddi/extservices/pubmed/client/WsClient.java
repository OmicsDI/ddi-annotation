package uk.ac.ebi.ddi.extservices.pubmed.client;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ddi.extservices.pubmed.config.PubmedWsConfigProd;
import uk.ac.ebi.ddi.extservices.utils.RetryClient;


/**
 * Abstract client to query The Tax NCBI to get the Id
 *
 * @author ypriverol
 */

public class WsClient extends RetryClient {

    protected RestTemplate restTemplate;
    protected PubmedWsConfigProd config;

    /**
     * Default constructor for Archive clients
     * @param config
     */
    public WsClient(PubmedWsConfigProd config) {
        this.config = config;
        this.restTemplate = new RestTemplate(clientHttpRequestFactory());
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(20000);
        factory.setConnectTimeout(20000);
        return factory;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PubmedWsConfigProd getConfig() {
        return config;
    }

    public void setConfig(PubmedWsConfigProd config) {
        this.config = config;
    }
}
