package uk.ac.ebi.ddi.annotation.service;

import uk.ac.ebi.ddi.service.db.model.enrichment.EnrichedDataset;
import uk.ac.ebi.ddi.service.db.service.enrichment.EnrichmentService;

/**
 * Created by mingze on 03/08/15.
 */
public class DDIAnnotationService implements IDDIAnnotationService {

    private EnrichedDataset cachedDataset;
    private EnrichmentService enrichmentService = new EnrichmentService();

    @Override
    public String getEnrichedTitle(String accession) {
        readEnrichedDataset(accession);
        return cachedDataset.getEnrichedTitle();
    }

    @Override
    public String getEnrichedAbstract(String accession) {
        readEnrichedDataset(accession);
        return cachedDataset.getEnrichedAbstractDescription();
    }

    @Override
    public String getEnrichedSampleProtocol(String accession) {
        readEnrichedDataset(accession);
        return cachedDataset.getEnrichedSampleProtocol();
    }

    @Override
    public String getEnrichedDataProtocol(String accession) {
        readEnrichedDataset(accession);
        return cachedDataset.getEnrichedDataProtocol();
    }

    private void readEnrichedDataset(String accession) {

        if (cachedDataset != null && accession.equals(cachedDataset.getAccession())) {
            return;
        }
        else if (enrichmentService.isDatasetExist(accession)) {
            cachedDataset = enrichmentService.readByaccession(accession);
            return;
        }

        cachedDataset.setAccession(accession);
        cachedDataset.setEnrichedTitle(null);
        cachedDataset.setEnrichedAbstractDescription(null);
        cachedDataset.setEnrichedSampleProtocol(null);
        cachedDataset.setEnrichedDataProtocol(null);
        return;
    }

    private String getEnrichedField(String accession, String fieldName) {

        return null;
    }
}
