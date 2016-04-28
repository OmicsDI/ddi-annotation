package uk.ac.ebi.ddi.annotation.service;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ddi.annotation.utils.DataType;
import uk.ac.ebi.ddi.service.db.model.similarity.TermInList;
import uk.ac.ebi.ddi.service.db.model.similarity.ExpOutputDataset;
import uk.ac.ebi.ddi.service.db.model.similarity.TermInDB;
import uk.ac.ebi.ddi.service.db.service.similarity.ExpOutputDatasetService;
import uk.ac.ebi.ddi.service.db.service.similarity.TermInDBService;
import uk.ac.ebi.ddi.xml.validator.parser.model.Reference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mingze on 09/09/15.
 */
public class DDIExpDataImportService {

    @Autowired
    TermInDBService termInDBService = new TermInDBService();

    @Autowired
    ExpOutputDatasetService expOutputDatasetService = new ExpOutputDatasetService();

    /**
     * Import dataset from the reference data in XML files
     * @param dataType omics type of the dataset
     * @param datasetAcc Accession of the dataset
     * @param refs cross reference molecule data in XML files, contains cross ref id and DB
     * @return sucess
     */
    public String importDatasetTerms(String dataType, String datasetAcc, String database, List<Reference> refs) {

        ExpOutputDataset importedExpDataset = expOutputDatasetService.readByAccession(datasetAcc, database);
        List<TermInList> terms = getTermsInDataset(dataType, datasetAcc, refs);
        if(importedExpDataset!=null){
            if(isTermsChanged(importedExpDataset.getTerms(),terms)){
                importedExpDataset.setTerms(terms);
                expOutputDatasetService.update(importedExpDataset);
                return "Updated dataset successfully";
            }
            else return "Unchanged, did nothing";
        }
        else {
            ExpOutputDataset newExpDataset = new ExpOutputDataset(datasetAcc, database, dataType, terms);
            expOutputDatasetService.insert(newExpDataset);
            return "Inserted new dataset successfully";
        }

    }

    private boolean isTermsChanged(List<TermInList> importedTerms, List<TermInList> terms) {

        if(terms.size()!=importedTerms.size()){
            return false;
        } else {
            importedTerms.retainAll(terms);
            if (terms.size() != importedTerms.size()) {
                return false;
            } else{
                return true;
            }
        }


    }

    /**
     * Get terms(molecules: metabolites/peptides in the dataset)
     * @param dataType omics type of the dataset
     * @param datasetAcc Accession of the dataset
     * @param refs cross reference data in XML files, contains cross ref id and DB
     * @return
     */
    private List<TermInList> getTermsInDataset(String dataType, String datasetAcc, List<Reference> refs) {
        List<TermInList> terms = new ArrayList<>();
        String refKeyWord = null;
        String refKeyWord2 = null;
        if (dataType.equals(DataType.PROTEOMICS_DATA.getName())) {
            refKeyWord = "uniprot";
            refKeyWord2 = "ensembl";
        } else if (dataType.equals(DataType.METABOLOMICS_DATA.getName())) {
            refKeyWord = "ChEBI";
        }

        refs = removeRedundancy(refs);
        for (Reference ref : refs) {
            if (ref.getDbname().equals(refKeyWord) || ref.getDbname().equals(refKeyWord2)) {
                String dbkey = ref.getDbkey();
                dbkey = dbkey.replace("CHEBI:", "");
                if (termInDBService.isTermExist(dbkey)) {
                    TermInDB tempTermInDB = termInDBService.readByName(dbkey);
                    tempTermInDB.increaseTimeOfAccurrenceInDB();
                    tempTermInDB.increaseDatasetFrequency();
                    termInDBService.update(tempTermInDB);

                    TermInList tempTermInList = new TermInList(dbkey); //here we assume one ref/term only occurrence 1 time in a dataset.
                    tempTermInList.setIdInDB(tempTermInDB.getId());
                    terms.add(tempTermInList);
                } else {
                    TermInDB newTermInDB = new TermInDB(dbkey, dataType);
                    termInDBService.insert(newTermInDB);
//                    System.out.println("inserted new term" + newTermInDB.getTermName());

                    TermInList tempTermInList = new TermInList(dbkey); //here we assume one ref/term only occurrence 1 time in a dataset.
                    tempTermInList.setIdInDB(newTermInDB.getId());
                    terms.add(tempTermInList);
                }
            }
        }
        return terms;
    }

    /**
     * Remove the redundant terms in list, to be a set
     * @param refs cross reference data in XML files, contains cross ref id and DB
     * @return NonRedundant list
     */
    private List<Reference> removeRedundancy(List<Reference> refs) {
        List<Reference> tempRefs = new ArrayList<>();
        List<String> refTerms = new ArrayList<>();

        for (Reference ref : refs) {
            if (!refTerms.contains(ref.getDbkey())) {
                tempRefs.add(ref);
                refTerms.add(ref.getDbkey());
            }
        }
        return tempRefs;
    }
}
