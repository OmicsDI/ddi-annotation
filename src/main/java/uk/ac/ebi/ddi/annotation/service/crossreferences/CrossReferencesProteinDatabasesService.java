package uk.ac.ebi.ddi.annotation.service.crossreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import uk.ac.ebi.ddi.annotation.service.dataset.DDIDatasetAnnotationService;
import uk.ac.ebi.ddi.annotation.utils.Constants;
import uk.ac.ebi.ddi.extservices.uniprot.UniprotIdentifier;
import uk.ac.ebi.ddi.gpmdb.GetGPMDBInformation;
import uk.ac.ebi.ddi.pride.web.service.client.assay.AssayWsClient;
import uk.ac.ebi.ddi.pride.web.service.client.project.ProjectWsClient;
import uk.ac.ebi.ddi.pride.web.service.config.ArchiveWsConfigProd;
import uk.ac.ebi.ddi.pride.web.service.model.assay.AssayDetail;
import uk.ac.ebi.ddi.pride.web.service.model.project.ProjectDetails;
import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;
import uk.ac.ebi.ddi.xml.validator.parser.model.CrossReferences;
import uk.ac.ebi.ddi.xml.validator.parser.model.Entry;
import uk.ac.ebi.ddi.xml.validator.parser.model.Reference;
import uk.ac.ebi.ddi.xml.validator.utils.Field;
import uk.ac.ebi.pride.tools.protein_details_fetcher.ProteinDetailFetcher;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 03/12/2015
 */
public class CrossReferencesProteinDatabasesService {

    private static AssayWsClient assayWsClient = new AssayWsClient(new ArchiveWsConfigProd());
    private static ProjectWsClient projectclient = new ProjectWsClient(new ArchiveWsConfigProd());

    public static final Logger LOGGER = LoggerFactory.getLogger(CrossReferencesProteinDatabasesService.class);

    /**
     * Annotated Cross References.
     * @param dataset INSERTED to be added
     * @return Resulting dataset
     */
    public static Entry annotateCrossReferences(Entry dataset) {

        if (dataset != null && dataset.getCrossReferences() != null) {
            List<Reference> finalReferences = new ArrayList<>();
            for (Reference crossRef: dataset.getCrossReferences().getRef()) {
                if (crossRef.getDbname().equalsIgnoreCase("PRIDE")) {
                    AssayDetail px = assayWsClient.getAssayByAccession(crossRef.getDbkey());
                    if (px != null && px.projectAccession != null) {
                        crossRef.setDbkey(px.projectAccession);
                        finalReferences.add(crossRef);
                    }
                } else if (crossRef.getDbname().equalsIgnoreCase("ProteomeExchange")) {
                    try {
                        ProjectDetails px = projectclient.getProject(crossRef.getDbkey());
                        if (px != null && px.accession != null) {
                            crossRef.setDbkey(px.getAccession());
                            crossRef.setDbname("pride");
                            finalReferences.add(crossRef);
                        }

                    } catch (IOException e) {
                        LOGGER.error("Exception occurred when processing dataset {}, ", dataset.getAcc(), e);
                    }
                } else if (!crossRef.getDbname().equalsIgnoreCase("PeptideAtlas")) {
                    finalReferences.add(crossRef);
                }
            }
            CrossReferences newCrossReferences = new CrossReferences();
            newCrossReferences.setRef(finalReferences);
            dataset.setCrossReferences(newCrossReferences);
        }
        return dataset;
    }

    @Deprecated
    public static Entry annotateGPMDBProteins(Entry dataset) {
        if (dataset != null && dataset.getAdditionalFields() != null && !dataset.getAdditionalFields().isEmpty()) {
            List<String> models = new ArrayList<>();
            dataset.getAdditionalFields().getField().stream()
                    .filter(field -> field != null && field.getName().equalsIgnoreCase(Field.GPMDB_MODEL.getName()))
                    .forEach(field -> {
                        String valueModel = field.getValue();
                        String[] valueString = valueModel.split("=");
                        if (valueString.length > 1) {
                            models.add(valueString[1].trim());
                        }
                    });
            if (!models.isEmpty()) {
                GetGPMDBInformation gpmdbInformation = GetGPMDBInformation.getInstance();
                Map<String, String> proteins = mapProteinToDatabase(gpmdbInformation.getUniqueProteinList(models));
                if (proteins != null && !proteins.isEmpty()) {
                    for (String proteinId : proteins.keySet()) {
                        dataset.addCrossReferenceValue(proteins.get(proteinId), proteinId);
                    }
                }

            }
        }
        return dataset;
    }


    private static Map<String, String> mapProteinToDatabase(List<String> proteinIds) {

        Map<String, String> mapIdentifiers = new HashMap<>();
        if (proteinIds != null && !proteinIds.isEmpty()) {
            List<String> accUniprot = new ArrayList<>();
            for (String accession: proteinIds) {
                ProteinDetailFetcher accessionResolver = new ProteinDetailFetcher();
                ProteinDetailFetcher.AccessionType accessionType = accessionResolver.getAccessionType(accession);
                if (accessionType == ProteinDetailFetcher.AccessionType.ENSEMBL) {
                    mapIdentifiers.put(accession, Constants.ENSEMBL_DATABASE);
                } else if (accessionType == ProteinDetailFetcher.AccessionType.UNIPROT_ID) {
                    mapIdentifiers.put(accession, Constants.UNIPROT_DATABASE);
                } else if (accessionType == ProteinDetailFetcher.AccessionType.UNIPROT_ACC) {
                    accUniprot.add(accession);
                }
            }
            if (!accUniprot.isEmpty()) {
                for (String id: UniprotIdentifier.retrieve(accUniprot, "ID", "ACC")) {
                    mapIdentifiers.put(id, Constants.UNIPROT_DATABASE);
                }
            }
        }
        return mapIdentifiers;
    }

    public static Dataset annotatePXCrossReferences(DDIDatasetAnnotationService datasetAnnotationService,
                                                    Dataset dataset) throws RestClientException {

        if (dataset != null && dataset.getCrossReferences() != null) {
            Map<String, Set<String>> cross = new HashMap<>();
            for (String crossRef: dataset.getCrossReferences().keySet()) {
                if (crossRef.equalsIgnoreCase("PRIDE")) {
                    Set<String> newKeys = new HashSet<>();
                    for (String dbKey: dataset.getCrossReferences().get(crossRef)) {
                        String accession = null;
                        try {
                            ProjectDetails pxDetails = projectclient.getProject(dbKey);
                            if (pxDetails != null && pxDetails.getAccession() != null) {
                                accession = dbKey;
                            }
                        } catch (Exception e) {
                            LOGGER.error("Exception occurred while fetching project {}", dbKey, e);
                        }
                        try {
                            if (accession == null) {
                                AssayDetail px = assayWsClient.getAssayByAccession(dbKey);
                                if (px != null && px.projectAccession != null) {
                                    accession = px.projectAccession;
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.error("Exception occurred while fetching assay {}", dbKey, e);
                        }
                        if (accession != null) {
                            newKeys.add(accession);
                        }
                    }
                    cross.put("pride", newKeys);
                } else if (crossRef.equalsIgnoreCase("ProteomeExchange")
                        || crossRef.equalsIgnoreCase("ProteomeXChange")) {
                    List<Dataset> allDatasets = new ArrayList<>();
                    for (String dbKey: dataset.getCrossReferences().get(crossRef)) {
                        allDatasets.addAll(datasetAnnotationService.getDataset(dbKey));
                    }

                    Map<String, Set<String>> keyValues = allDatasets.stream()
                            .collect(Collectors.groupingBy(
                                    Dataset::getDatabase,
                                    Collectors.mapping(Dataset::getAccession, Collectors.toSet())));
                    keyValues.forEach(cross::put);
                } else {
                    cross.put(crossRef, dataset.getCrossReferences().get(crossRef));
                }
            }
            dataset.setCrossReferences(cross);
        }
        return dataset;
    }
}
