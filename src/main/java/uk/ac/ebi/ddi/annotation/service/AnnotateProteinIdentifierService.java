package uk.ac.ebi.ddi.annotation.service;

import uk.ac.ebi.ddi.annotation.utils.Constants;
import uk.ac.ebi.ddi.extservices.uniprot.UniprotIdentifier;
import uk.ac.ebi.pride.tools.protein_details_fetcher.ProteinDetailFetcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 03/12/2015
 */
public class AnnotateProteinIdentifierService {

    public static Map<String, String> mapProteinToDatabase(List<String> proteinIds){

        Map<String, String> mapIdentifiers = new HashMap<String, String>();
        if(proteinIds != null && !proteinIds.isEmpty()){
            List<String> accUniprot = new ArrayList<String>();
            for(String accession: proteinIds){
                ProteinDetailFetcher accessionResolver = new ProteinDetailFetcher();
                ProteinDetailFetcher.AccessionType accessionType = accessionResolver.getAccessionType(accession);
                if(accessionType == ProteinDetailFetcher.AccessionType.ENSEMBL) {
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
