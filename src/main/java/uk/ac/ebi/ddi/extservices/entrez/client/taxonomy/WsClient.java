package uk.ac.ebi.ddi.extservices.entrez.client.taxonomy;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ddi.extservices.entrez.config.TaxWsConfigProd;


/**
 * Abstract client to query The Tax NCBI to get the Id
 *
 * @author ypriverol
 */

public class WsClient {

    protected RestTemplate restTemplate;
    protected TaxWsConfigProd config;

    /**
     * Default constructor for Archive clients
     * @param config
     */
    public WsClient(TaxWsConfigProd config){
        this.config = config;
        this.restTemplate = new RestTemplate(clientHttpRequestFactory());
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(2000);
        factory.setConnectTimeout(2000);
        return factory;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TaxWsConfigProd getConfig() {
        return config;
    }

    public void setConfig(TaxWsConfigProd config) {
        this.config = config;
    }
}
