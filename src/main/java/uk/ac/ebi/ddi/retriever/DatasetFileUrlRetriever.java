package uk.ac.ebi.ddi.retriever;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ddi.extservices.utils.RetryClient;

import java.io.IOException;
import java.util.Set;

public abstract class DatasetFileUrlRetriever extends RetryClient implements IDatasetFileUrlRetriever {

    private IDatasetFileUrlRetriever datasetDownloadingRetriever;

    protected RestTemplate restTemplate;

    public DatasetFileUrlRetriever(IDatasetFileUrlRetriever datasetDownloadingRetriever) {
        this.datasetDownloadingRetriever = datasetDownloadingRetriever;
        this.restTemplate = new RestTemplate(clientHttpRequestFactory());
    }

    protected abstract Set<String> getAllDatasetFiles(String accession, String database) throws IOException;

    @Override
    public Set<String> getDatasetFiles(String accession, String database) throws IOException {
        Set<String> result = datasetDownloadingRetriever.getDatasetFiles(accession, database);
        result.addAll(getAllDatasetFiles(accession, database));
        return result;
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(200000);
        factory.setConnectTimeout(200000);
        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
        factory.setHttpClient(httpClient);
        return factory;
    }
}
