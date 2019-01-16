package uk.ac.ebi.ddi.annotation.service.dataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ddi.annotation.model.DatasetTobeEnriched;
import uk.ac.ebi.ddi.annotation.model.EnrichedDataset;
import uk.ac.ebi.ddi.annotation.service.publication.DDIPublicationAnnotationService;
import uk.ac.ebi.ddi.annotation.service.synonyms.DDIAnnotationService;
import uk.ac.ebi.ddi.annotation.service.synonyms.DDIExpDataImportService;
import uk.ac.ebi.ddi.annotation.utils.DataType;
import uk.ac.ebi.ddi.annotation.utils.DatasetUtils;
import uk.ac.ebi.ddi.annotation.utils.Utils;
import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;
import uk.ac.ebi.ddi.xml.validator.parser.model.Entry;
import uk.ac.ebi.ddi.xml.validator.utils.Field;

import java.util.*;

/**
 * This class contains a set of methods that hels the enrichment and annotation of different datasets
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 04/11/15
 */
public class DatasetAnnotationEnrichmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetAnnotationEnrichmentService.class);


    /**
     * This function provides a way of doing the enrichment of an specific dataset using the enrichment service
     * @param service DDIAnnotationService that enrich a correponding dataset
     * @param dataset Entry to be enriched
     * @return
     */
    @Deprecated
    public static EnrichedDataset enrichment(DDIAnnotationService service, Entry dataset, boolean overwrite)
            throws Exception {

        LOGGER.info("DatasetTobeEnriched 1: {}, {}", dataset.getId(), dataset.getRepository());

        DatasetTobeEnriched datasetTobeEnriched = new DatasetTobeEnriched(
                dataset.getId(), dataset.getRepository(), "");
        return service.enrichment(datasetTobeEnriched, overwrite);
    }

    /**
     * This logic must be placed on controller/handler
     * rather than utils like this.
     * This function will be removed in the next version
     */
    @Deprecated
    public static EnrichedDataset enrichment(DDIAnnotationService service, Dataset dataset, boolean overwrite)
            throws Exception {

        LOGGER.info("DatasetTobeEnriched 2: {}, {}", dataset.getAccession(), dataset.getDatabase());

        Map<String, String> fields = new HashMap<>();
        fields.put(Field.NAME.getName(), dataset.getName());
        fields.put(Field.DESCRIPTION.getName(), dataset.getDescription());
        fields.put(Field.DATA.getName(), DatasetUtils.getFirstAdditionalFieldValue(dataset, Field.DATA.getName()));
        fields.put(Field.SAMPLE.getName(), DatasetUtils.getFirstAdditionalFieldValue(dataset, Field.SAMPLE.getName()));
        fields.put(Field.PUBMED_ABSTRACT.getName(), DatasetUtils.getFirstAdditionalFieldValue(
                dataset, Field.PUBMED_ABSTRACT.getName()));
        fields.put(Field.PUBMED_TITLE.getName(), DatasetUtils.getFirstAdditionalFieldValue(
                dataset, Field.PUBMED_TITLE.getName()));
        return service.enrichment(
                new DatasetTobeEnriched(dataset.getAccession(), dataset.getDatabase(), fields), overwrite);
    }

//    /**
//     * This function import all the biological entities into the MongoDB database and to compute
//     * the similarity scores.
//     * @param dataset Entry dataset
//     * @param dataType Data type to be index Metabolomics, proteomics, etc
//     * @param ddiExpDataImportService The import service
//     */
//    @Deprecated
//    public static void importTermsToDatabase(Entry dataset, DataType dataType,
//                                             DDIExpDataImportService ddiExpDataImportService){
//        String entryId = dataset.getId();
//        List<Reference> refs = dataset.getCrossReferences().getRef();
//        ddiExpDataImportService.importDatasetTerms(dataType.getName(), entryId,
//                dataset.getAdditionalFieldValue(Field.REPOSITORY.getName()), refs);
//    }

    public static void importTermsToDatabase(Dataset dataset, DataType dataType,
                                             DDIExpDataImportService ddiExpDataImportService) {
        String entryId = dataset.getAccession();
        Map<String, Set<String>> refs = dataset.getCrossReferences();
        ddiExpDataImportService.importDatasetTerms(dataType.getName(), entryId, dataset.getDatabase(), refs);
    }


    /**
     * Add the enrichment fields to the entry to be use during indexing process
     *
     * This logic must be placed on controller/handler
     * rather than utils like this.
     * This function will be removed in the next version
     *
     * @param dataset Entry the dataset to add the new fields
     * @param enrichedDataset The new fields to be added to the dataset
     * @return Entry a new entry with all the fields
     */

    @Deprecated
    public static Dataset addEnrichedFields(Dataset dataset, EnrichedDataset enrichedDataset) {
        if (enrichedDataset.getEnrichedAttributes().containsKey(Field.NAME.getName())) {
            DatasetUtils.addAdditionalFieldSingleValue(dataset, Field.ENRICH_TITLE.getName(),
                    Utils.removeRedundantSynonyms(enrichedDataset.getEnrichedAttributes().get(Field.NAME.getName())));
        }

        if (enrichedDataset.getEnrichedAttributes().containsKey(Field.DESCRIPTION.getName())) {
            DatasetUtils.addAdditionalFieldSingleValue(dataset, Field.ENRICH_ABSTRACT.getName(),
                    Utils.removeRedundantSynonyms(
                            enrichedDataset.getEnrichedAttributes().get(Field.DESCRIPTION.getName())));
        }

        if (enrichedDataset.getEnrichedAttributes().containsKey(Field.SAMPLE.getName())) {
            DatasetUtils.addAdditionalFieldSingleValue(dataset, Field.ENRICH_SAMPLE.getName(),
                    Utils.removeRedundantSynonyms(
                            enrichedDataset.getEnrichedAttributes().get(Field.SAMPLE.getName())));
        }

        if (enrichedDataset.getEnrichedAttributes().containsKey(Field.DATA.getName())) {
            DatasetUtils.addAdditionalFieldSingleValue(dataset, Field.ENRICH_DATA.getName(),
                    Utils.removeRedundantSynonyms(enrichedDataset.getEnrichedAttributes().get(Field.DATA.getName())));
        }

        if (enrichedDataset.getEnrichedAttributes().containsKey(Field.PUBMED_TITLE.getName())) {
            DatasetUtils.addAdditionalFieldSingleValue(dataset, Field.ENRICHE_PUBMED_TITLE.getName(),
                    Utils.removeRedundantSynonyms(
                            enrichedDataset.getEnrichedAttributes().get(Field.PUBMED_TITLE.getName())));
        }

        if (enrichedDataset.getEnrichedAttributes().containsKey(Field.PUBMED_ABSTRACT.getName())) {
            DatasetUtils.addAdditionalFieldSingleValue(dataset, Field.ENRICH_PUBMED_ABSTRACT.getName(),
                    Utils.removeRedundantSynonyms(
                            enrichedDataset.getEnrichedAttributes().get(Field.PUBMED_ABSTRACT.getName())));
        }

        return dataset;
    }

    /**
     * Add multiple enrichment fields into dataset
     * @param dataset
     * @param fieldValues
     */
    public static void addEnrichedFields(Dataset dataset, Map<Field, String> fieldValues) {
        for (Map.Entry<Field, String> entry : fieldValues.entrySet()) {
            if (entry.getValue() != null) {
                DatasetUtils.addAdditionalFieldSingleValue(dataset, entry.getKey().getName(),
                        Utils.removeRedundantSynonyms(entry.getValue()));
            }
        }
    }


    /**
     * This function takes a dataset check if contains pubmed articles in the cross-references.
     * If the pubmed ids are not provided
     * as cross-references, the currect function looks in all the fields of a dataset for
     * dois information and retrive the pubmed
     * id and annotated them.
     *
     *
     * @param service DDIPublicationAnnotationService
     * @param dataset dataset to be updated
     * @return Entry the new dataset with the corresponding information
     */
    @Deprecated
    public static Entry updatePubMedIds(DDIPublicationAnnotationService service, Entry dataset) {
        // check if the dataset contains pubmed references
        if (dataset.getCrossReferences() == null
                || dataset.getCrossReferenceFieldValue(Field.PUBMED.getName()).isEmpty()) {
            List<String> datasetText = new ArrayList<>();
            datasetText.add(dataset.toString());
            List<String> dois = service.getDOIListFromText(datasetText);
            if (dois != null && !dois.isEmpty()) {
                List<String> ids = service.getPubMedIDsFromDOIList(dois);
                if (ids != null && ids.size() > 0) {
                    for (String pubmedID: ids) {
                        dataset.addCrossReferenceValue(Field.PUBMED.getName(), pubmedID);
                    }
                }
            }
        }
        if (dataset.getCrossReferences() != null
                && !dataset.getCrossReferenceFieldValue(Field.PUBMED.getName()).isEmpty()) {
            List<String> pubmedIds = dataset.getCrossReferenceFieldValue(Field.PUBMED.getName());
            List<Map<String, String[]>> information = service.getAbstractPublication(pubmedIds);
            for (Map<String, String[]> entry: information) {
                if (!entry.isEmpty()) {
                    for (String key: entry.keySet()) {
                        if (key.equalsIgnoreCase("description")) {
                            for (String values: entry.get(key)) {
                                dataset.addAdditionalField(Field.PUBMED_ABSTRACT.getName(), values);
                            }
                        } else if (key.equalsIgnoreCase("name")) {
                            for (String values : entry.get(key)) {
                                dataset.addAdditionalField(Field.PUBMED_TITLE.getName(), values);
                            }
                        } else if (key.equalsIgnoreCase("author")) {
                            StringBuilder authorName = new StringBuilder();
                            for (String authorValue: entry.get(key)) {
                                authorName.append(authorValue).append(",");
                            }
                            dataset.addAdditionalField(Field.PUBMED_AUTHORS.getName(), authorName.toString());
                        }
                    }
                }
            }
        }
        return dataset;
    }

    /**
     * This function takes a dataset check if contains pubmed articles in the cross-references.
     * If the pubmed ids are not provided
     * as cross-references, the currect function looks in all the fields of a dataset for dois
     * information and retrive the pubmed
     * id and annotated them.
     *
     * @param service DDIPublicationAnnotationService
     * @param dataset dataset to be updated
     * @return Entry the new dataset with the corresponding information
     */
    public static Dataset updatePubMedIds(DDIPublicationAnnotationService service, Dataset dataset) {

        if (dataset.getCrossReferences() == null
                || DatasetUtils.getCrossReferenceFieldValue(dataset, Field.PUBMED.getName()).isEmpty()) {
            List<String> dois = service.getDOIListFromText(Collections.singletonList(dataset.toString()));
            List<String> ids = service.getPubMedIDsFromDOIList(dois);
            for (String pubmedID: ids) {
                DatasetUtils.addCrossReferenceValue(dataset, Field.PUBMED.getName(), pubmedID);
            }
        }
        if (dataset.getCrossReferences() != null
                && !DatasetUtils.getCrossReferenceFieldValue(dataset, Field.PUBMED.getName()).isEmpty()) {
            Set<String> pubmedIds = DatasetUtils.getCrossReferenceFieldValue(dataset, Field.PUBMED.getName());
            List<Map<String, String[]>> information = service.getAbstractPublication(new ArrayList<>(pubmedIds));
            information.forEach(info -> info.forEach((key, value) -> {
                if (key.equalsIgnoreCase("description")) {
                    for (String values : value) {
                        DatasetUtils.addAdditionalField(dataset, Field.PUBMED_ABSTRACT.getName(), values);
                    }
                } else if (key.equalsIgnoreCase("name")) {
                    for (String values : value) {
                        DatasetUtils.addAdditionalField(dataset, Field.PUBMED_TITLE.getName(), values);
                    }
                } else if (key.equalsIgnoreCase("author")) {
                    StringBuilder authorName = new StringBuilder();
                    for (String authorValue : value) {
                        authorName.append(authorValue).append(",");
                    }
                    DatasetUtils.addAdditionalField(dataset, Field.PUBMED_AUTHORS.getName(), authorName.toString());
                }
            }));
        }

        return dataset;
    }
}
