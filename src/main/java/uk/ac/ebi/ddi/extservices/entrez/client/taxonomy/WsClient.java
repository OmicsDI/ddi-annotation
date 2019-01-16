package uk.ac.ebi.ddi.extservices.entrez.client.taxonomy;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ddi.extservices.entrez.config.TaxWsConfigProd;

import java.util.Collections;


/**
 * Abstract client to query The Tax NCBI to get the Id
 *
 * @author ypriverol
 */

public class WsClient {

    protected RestTemplate restTemplate;
    protected TaxWsConfigProd config;
    private static final int RETRIES = 5;
    protected RetryTemplate retryTemplate = new RetryTemplate();

    /**
     * Default constructor for Archive clients
     * @param config
     */
    public WsClient(TaxWsConfigProd config) {
        this.config = config;
        this.restTemplate = new RestTemplate(clientHttpRequestFactory());
        SimpleRetryPolicy policy =
                new SimpleRetryPolicy(RETRIES, Collections.singletonMap(Exception.class, true));
        retryTemplate.setRetryPolicy(policy);
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(2000);
        backOffPolicy.setMultiplier(1.6);
        retryTemplate.setBackOffPolicy(backOffPolicy);
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(2000);
        factory.setConnectTimeout(2000);
        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
        factory.setHttpClient(httpClient);
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
