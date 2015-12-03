package uk.ac.ebi.ddi.annotation.service;

import uk.ac.ebi.ddi.pride.web.service.client.assay.AssayWsClient;
import uk.ac.ebi.ddi.pride.web.service.client.project.ProjectWsClient;
import uk.ac.ebi.ddi.pride.web.service.config.ArchiveWsConfigProd;
import uk.ac.ebi.ddi.pride.web.service.model.assay.AssayDetail;
import uk.ac.ebi.ddi.pride.web.service.model.project.ProjectDetails;
import uk.ac.ebi.ddi.xml.validator.parser.model.Entry;
import uk.ac.ebi.ddi.xml.validator.parser.model.Reference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

}
