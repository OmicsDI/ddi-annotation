package uk.ac.ebi.ddi.annotation.service;

import uk.ac.ebi.ddi.annotation.utils.Constants;
import uk.ac.ebi.ddi.extservices.uniprot.UniprotIdentifier;
import uk.ac.ebi.ddi.gpmdb.GetGPMDBInformation;
import uk.ac.ebi.ddi.pride.web.service.client.assay.AssayWsClient;
import uk.ac.ebi.ddi.pride.web.service.client.project.ProjectWsClient;
import uk.ac.ebi.ddi.pride.web.service.config.ArchiveWsConfigProd;
import uk.ac.ebi.ddi.pride.web.service.model.assay.AssayDetail;
import uk.ac.ebi.ddi.pride.web.service.model.project.ProjectDetails;
import uk.ac.ebi.ddi.xml.validator.parser.model.Entry;
import uk.ac.ebi.ddi.xml.validator.parser.model.Field;
import uk.ac.ebi.ddi.xml.validator.parser.model.Reference;
import uk.ac.ebi.pride.tools.protein_details_fetcher.ProteinDetailFetcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 03/12/2015
 */
public class CrossReferencesProteinDatabasesService {

    private static AssayWsClient assayWsClient = new AssayWsClient(new ArchiveWsConfigProd());
    private static ProjectWsClient projectclient = new ProjectWsClient(new ArchiveWsConfigProd());

    /**
     * Annotated Cross References.
     * @param dataset Dataset to be added
     * @return Resulting dataset
     */
    public static Entry annotateCrossReferences(Entry dataset){

        if(dataset != null && dataset.getCrossReferences() != null){
            List<Reference> finalReferences = new ArrayList<Reference>();
            for(Reference crossRef: dataset.getCrossReferences().getRef()){
                if(crossRef.getDbname().equalsIgnoreCase("PRIDE")){
                    AssayDetail px = assayWsClient.getAssayByAccession(crossRef.getDbkey());
                    if(px != null && px.projectAccession != null){
                        crossRef.setDbkey(px.projectAccession);
                        finalReferences.add(crossRef);
                    }
                }else if(crossRef.getDbname().equalsIgnoreCase("ProteomeExchange")){
                    try {
                        ProjectDetails px = projectclient.getProject(crossRef.getDbkey());
                        if(px != null && px.accession != null){
                            crossRef.setDbkey(px.getAccession());
                            crossRef.setDbname("pride");
                            finalReferences.add(crossRef);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else if(!crossRef.getDbname().equalsIgnoreCase("PeptideAtlas")){
                    finalReferences.add(crossRef);
                }
            }
            dataset.setCrossReferences(finalReferences);
        }
        return dataset;
    }


    public static Entry annotateGPMDBProteins(Entry dataset){
        if(dataset != null && dataset.getAdditionalFields() != null && !dataset.getAdditionalFields().isEmpty()){
            List<String> models = new ArrayList<String>();
            for(Field field: dataset.getAdditionalFields().getField()){
                if(field != null && field.getName().equalsIgnoreCase(uk.ac.ebi.ddi.xml.validator.utils.Field.GPMDB_MODEL.getName())){
                    String valueModel = field.getValue();
                    String[] valueString = valueModel.split("=");
                    if(valueString.length > 1)
                        models.add(valueString[1].trim());
                }
            }
            if(!models.isEmpty()){
                uk.ac.ebi.ddi.gpmdb.GetGPMDBInformation gpmdbInformation = GetGPMDBInformation.getInstance();
                Map<String, String> proteins = mapProteinToDatabase(gpmdbInformation.getUniqueProteinList(models));
                if(proteins != null && !proteins.isEmpty())
                    for(String proteinId: proteins.keySet())
                        dataset.addCrossReferenceValue(proteins.get(proteinId), proteinId);

            }
        }
        return dataset;
    }

    private static Map<String, String> mapProteinToDatabase(List<String> proteinIds){

        Map<String, String> mapIdentifiers = new HashMap<String, String>();
        if(proteinIds != null && !proteinIds.isEmpty()){
            List<String> accUniprot = new ArrayList<String>();
            for(String accession: proteinIds){
                ProteinDetailFetcher accessionResolver = new ProteinDetailFetcher();
                ProteinDetailFetcher.AccessionType accessionType = accessionResolver.getAccessionType(accession);
                if(accessionType == ProteinDetailFetcher.AccessionType.ENSEMBL) {
                    mapIdentifiers.put(accession, Constants.ENSEMBL_DATABASE);
                } else if(accessionType == ProteinDetailFetcher.AccessionType.ENSEMBL_TRANSCRIPT){
                    mapIdentifiers.put(accession, Constants.ENSEMBL_DATABASE);
                }else if(accessionType == ProteinDetailFetcher.AccessionType.UNIPROT_ID){
                    mapIdentifiers.put(accession, Constants.UNIPROT_DATABASE);
                }else if(accessionType == ProteinDetailFetcher.AccessionType.UNIPROT_ACC){
                    accUniprot.add(accession);
                }
            }
            if(!accUniprot.isEmpty()){
                for(String id: UniprotIdentifier.retrieve(accUniprot, "ACC", "ID"))
                    mapIdentifiers.put(id, Constants.UNIPROT_DATABASE);
            }
        }
        return mapIdentifiers;
    }

}
