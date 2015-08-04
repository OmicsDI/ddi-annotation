package uk.ac.ebi.ddi.annotation.service;

import uk.ac.ebi.ddi.service.db.*;

/**
 * Created by mingze on 03/08/15.
 */
public interface IDdiAnnotationService {

    /**
     *
     * @param datasetRepoId dataset Id in original Repository
     * @return enriched title of this dataset
     */
    String getEnrichedTitle(String datasetRepoId);

     /**
     *
     * @param datasetRepoId dataset Id in original Repository
     * @return enriched abstract of this dataset
     */
    String getEnrichedAbstract(String datasetRepoId);

     /**
     *
     * @param datasetRepoId dataset Id in original Repository
     * @return enriched sample protocol of this dataset
     */
    String getEnrichedSampleProtocol(String datasetRepoId);

     /**
     *
     * @param datasetRepoId dataset Id in original Repository
     * @return enriched data protocol of this dataset
     */
    String getEnrichedDataProtocol(String datasetRepoId);
}
