package uk.ac.ebi.ddi.retriever.providers;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ddi.annotation.utils.Constants;
import uk.ac.ebi.ddi.retriever.DatasetFileUrlRetriever;
import uk.ac.ebi.ddi.retriever.IDatasetFileUrlRetriever;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class BioModelsFileUrlRetriever extends DatasetFileUrlRetriever {

    private static final String BIOMODEL_ENDPOINT = "https://www.ebi.ac.uk/biomodels/model";

    public BioModelsFileUrlRetriever(IDatasetFileUrlRetriever datasetDownloadingRetriever) {
        super(datasetDownloadingRetriever);
    }

    @Override
    public Set<String> getAllDatasetFiles(String accession, String database) throws IOException {
        Set<String> result = new HashSet<>();
        if (!database.equals(Constants.BIOMODELS_DATABASE) && !database.equals(Constants.BIOMODELS_DATABASE_2)) {
            return result;
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BIOMODEL_ENDPOINT)
                .path("/files")
                .path("/" + accession);
        URI uriForFiles = builder.build().toUri();
        ResponseEntity<JsonNode> files = getRetryTemplate()
                .execute(x -> restTemplate.getForEntity(uriForFiles, JsonNode.class));
        for (JsonNode node : files.getBody().get("additional")) {
            UriComponentsBuilder urlFileBuilder = UriComponentsBuilder.fromHttpUrl(BIOMODEL_ENDPOINT)
                    .path("/download")
                    .path("/" + accession)
                    .queryParam("filename", node.get("name").asText());
            result.add(urlFileBuilder.build().encode().toUriString());
        }

        for (JsonNode node : files.getBody().get("main")) {
            UriComponentsBuilder urlFileBuilder = UriComponentsBuilder.fromHttpUrl(BIOMODEL_ENDPOINT)
                    .path("/download")
                    .path("/" + accession)
                    .queryParam("filename", node.get("name").asText());
            result.add(urlFileBuilder.build().encode().toUriString());
        }

        return result;
    }
}
