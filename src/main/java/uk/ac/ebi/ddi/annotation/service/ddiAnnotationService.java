package uk.ac.ebi.ddi.annotation.service;

import uk.ac.ebi.ddi.service.db.*;
import uk.ac.ebi.ddi.service.db.model.enrichment.EnrichedDataset;
import uk.ac.ebi.ddi.service.db.service.enrichment.EnrichmentService;

/**
 * Created by mingze on 03/08/15.
 */
public class ddiAnnotationService implements IDdiAnnotationService {

    private EnrichedDataset cachedDataset;
    private EnrichmentService enrichmentService = new EnrichmentService();

    @Override
    public String getEnrichedTitle(String datasetRepoId) {
        readEnrichedDataset(datasetRepoId);
        return cachedDataset.getEnrichedTitle();
    }

    @Override
    public String getEnrichedAbstract(String datasetRepoId) {
        readEnrichedDataset(datasetRepoId);
        return cachedDataset.getEnrichedAbstractDescription();
    }

    @Override
    public String getEnrichedSampleProtocol(String datasetRepoId) {
        readEnrichedDataset(datasetRepoId);
        return cachedDataset.getEnrichedSampleProtocol();
    }

    @Override
    public String getEnrichedDataProtocol(String datasetRepoId) {
        readEnrichedDataset(datasetRepoId);
        return cachedDataset.getEnrichedDataProtocol();
    }

    private void readEnrichedDataset(String datasetRepoId) {

        if (cachedDataset != null && datasetRepoId.equals(cachedDataset.getDatasetRepoId())) {
            return;
        }
        else if (enrichmentService.isDatasetExist(datasetRepoId)) {
            cachedDataset = enrichmentService.readByRepoId(datasetRepoId);
            return;
        }

        cachedDataset.setDatasetRepoId(datasetRepoId);
        cachedDataset.setEnrichedTitle(null);
        cachedDataset.setEnrichedAbstractDescription(null);
        cachedDataset.setEnrichedSampleProtocol(null);
        cachedDataset.setEnrichedDataProtocol(null);
        return;
    }

    private String getEnrichedField(String datasetRepoId, String fieldName) {

        return null;
    }
}
