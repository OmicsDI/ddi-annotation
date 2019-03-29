package uk.ac.ebi.ddi.retriever.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ddi.annotation.utils.Constants;
import uk.ac.ebi.ddi.retriever.DatasetFileUrlRetriever;
import uk.ac.ebi.ddi.retriever.IDatasetFileUrlRetriever;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class LincsFileUrlRetriever extends DatasetFileUrlRetriever {

    private static final String LINCS_ENDPOINT = "http://lincsportal.ccs.miami.edu/dcic/api";

    private static final Logger LOGGER = LoggerFactory.getLogger(LincsFileUrlRetriever.class);

    public LincsFileUrlRetriever(IDatasetFileUrlRetriever datasetDownloadingRetriever) {
        super(datasetDownloadingRetriever);
    }

    @Override
    public Set<String> getAllDatasetFiles(String accession, String database) throws IOException {
        Set<String> result = new HashSet<>();
        if (database.equals(Constants.LINCS_DATABASE)) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(LINCS_ENDPOINT)
                    .path("/fetchdata")
                    .queryParam("limit", 1)
                    .queryParam("searchTerm", "datasetid:" + accession)
                    .queryParam("skip", 0);
            URI uri = builder.build().encode().toUri();
            ResponseEntity<JsonNode> responseEntity = execute(x -> restTemplate.getForEntity(uri, JsonNode.class));
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                LOGGER.error("Exception occurred when fetching dataset's files of {}", accession);
                return result;
            }
            if (responseEntity.getBody().get("results").get("totalDocuments").intValue() < 1) {
                LOGGER.error("Exception occurred when fetching dataset's files of {}", accession);
                return result;
            }
            JsonNode node = responseEntity.getBody().get("results").get("documents").elements().next();
            List<JsonNode> levelPaths = Lists.newArrayList(node.get("levelspath").elements());
            List<JsonNode> datasetLevels = Lists.newArrayList(node.get("datasetlevels").elements());

            for (int i = 0; i < levelPaths.size(); i++) {
                String path = levelPaths.get(i).asText().replace("/projects/ccs/bd2klincs/", "");
                UriComponentsBuilder fileUrl = UriComponentsBuilder.fromHttpUrl(LINCS_ENDPOINT)
                        .path("/download")
                        .queryParam("path", path)
                        .queryParam("file", datasetLevels.get(i).asText() + ".tar.gz");
                result.add(fileUrl.build().encode().toUriString());
            }
        }
        return result;
    }
}
