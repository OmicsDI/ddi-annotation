package uk.ac.ebi.ddi.annotation.service;


/**
 * Created by mingze on 03/08/15.
 */
public interface IDDIAnnotationService {

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
