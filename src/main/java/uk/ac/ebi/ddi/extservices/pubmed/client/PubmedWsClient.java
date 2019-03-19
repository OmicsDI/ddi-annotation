package uk.ac.ebi.ddi.extservices.pubmed.client;

import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ddi.extservices.pubmed.config.PubmedWsConfigProd;
import uk.ac.ebi.ddi.extservices.pubmed.model.PubmedJSON;

import java.net.URI;
import java.util.List;


/**
 * @author Yasset Perez-Riverol ypriverol
 */
public class PubmedWsClient extends WsClient {

    /**
     * Default constructor for Ws clients
     *
     * @param config
     */
    public PubmedWsClient(PubmedWsConfigProd config) {
        super(config);
    }


    public PubmedJSON getPubmedIds(List<String> dois) throws RestClientException {

        if (dois != null && dois.size() > 0) {
            UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                    .scheme(config.getProtocol())
                    .host(config.getHostName())
                    .path("/pmc/utils/idconv/v1.0")
                    .queryParam("tool", "my_tool")
                    .queryParam("ids", String.join(",", dois))
                    .queryParam("format", "json");

            URI uri = builder.build().encode().toUri();
            return execute(ctx -> restTemplate.getForObject(uri, PubmedJSON.class));
        }
        return null;
    }
}
