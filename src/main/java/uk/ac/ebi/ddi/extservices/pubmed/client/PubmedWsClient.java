package uk.ac.ebi.ddi.extservices.pubmed.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import uk.ac.ebi.ddi.extservices.pubmed.config.PubmedWsConfigProd;
import uk.ac.ebi.ddi.extservices.pubmed.model.PubmedJSON;

import java.util.List;


/**
 * @author Yasset Perez-Riverol ypriverol
 */
public class PubmedWsClient extends WsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubmedWsClient.class);

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
            StringBuilder term = new StringBuilder();
            dois.size();
            int count = 0;
            for (String value: dois) {
                if (count == dois.size() - 1) {
                    term.append(value);
                } else {
                    term.append(value).append(",");
                }
                count++;
            }

            String url = String.format("%s://%s/pmc/utils/idconv/v1.0/?tool=my_tool&ids=%s&format=json",
                    config.getProtocol(), config.getHostName(), term.toString());
            //Todo: Needs to be removed in the future, this is for debugging
            LOGGER.debug(url);

            return this.restTemplate.getForObject(url, PubmedJSON.class);
        }
        return null;
    }
}
