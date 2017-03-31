package uk.ac.ebi.ddi.annotation.service.synonyms;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ddi.annotation.utils.DataType;
import uk.ac.ebi.ddi.service.db.model.similarity.TermInList;
import uk.ac.ebi.ddi.service.db.model.similarity.ExpOutputDataset;
import uk.ac.ebi.ddi.service.db.model.similarity.TermInDB;
import uk.ac.ebi.ddi.service.db.service.similarity.ExpOutputDatasetService;
import uk.ac.ebi.ddi.service.db.service.similarity.TermInDBService;
import uk.ac.ebi.ddi.xml.validator.parser.model.Reference;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 *  ==Overview==
 *
 *  This class
 *
 * Created by ypriverol (ypriverol@gmail.com) on 09/09/15.
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
    @Deprecated
    public String importDatasetTerms(String dataType, String datasetAcc, String database, List<Reference> refs) {

//        ExpOutputDataset importedExpDataset = expOutputDatasetService.readByAccession(datasetAcc, database);
//        List<TermInList> terms = getTermsInDataset(dataType, refs);
//        if(importedExpDataset!=null){
//            if(isTermsChanged(importedExpDataset.getTerms(),terms)){
//                importedExpDataset.setTerms(terms);
//                expOutputDatasetService.update(importedExpDataset);
//                return "Updated dataset successfully";
//            }
//            else return "Unchanged, did nothing";
//        }
//        else {
//            ExpOutputDataset newExpDataset = new ExpOutputDataset(datasetAcc, database, dataType, terms);
//            expOutputDatasetService.insert(newExpDataset);
//            return "Inserted new dataset successfully";
//        }
        return null;

    }

    /**
     * Import into the database all the termns
     * @param dataType Dataset Type Metabolomics, Proteomics, Genomics
     * @param datasetAcc Dataset Accession
     * @param database   Database Accession and Name
     * @param refs       References of the terms
     */
    public void importDatasetTerms(String dataType, String datasetAcc, String database, Map<String, Set<String>> refs) {
        List<String> terms = getTermsInDataset(dataType, refs);
        if(terms!=null && !terms.isEmpty()){
            List<TermInDB> termsDB = terms.parallelStream()
                    .map(x -> new TermInDB(datasetAcc, database, x, dataType))
                    .collect(Collectors.toList());
            termInDBService.insert(termsDB);
            expOutputDatasetService.update(new ExpOutputDataset(datasetAcc, database, dataType, new HashSet<>(terms)));
        }
    }

    private boolean isTermsChanged(List<TermInList> importedTerms, List<TermInList> terms) {

        if(terms.size()!=importedTerms.size()){
            return false;
        } else {
            importedTerms.retainAll(terms);
            return terms.size() == importedTerms.size();
        }
    }

    //    @Deprecated
//    private List<TermInList> getTermsInDataset(String dataType, List<Reference> refs) {
//        List<TermInList> terms = new ArrayList<>();
//        String refKeyWord = null;
//        String refKeyWord2 = null;
//        if (dataType.equals(DataType.PROTEOMICS_DATA.getName())) {
//            refKeyWord = "uniprot";
//            refKeyWord2 = "ensembl";
//        } else if (dataType.equals(DataType.METABOLOMICS_DATA.getName())) {
//            refKeyWord = "ChEBI";
//        }
//
//        refs = removeRedundancy(refs);
//        for (Reference ref : refs) {
//            if (ref.getDbname().equals(refKeyWord) || ref.getDbname().equals(refKeyWord2)) {
//                String dbkey = ref.getDbkey();
//                dbkey = dbkey.replace("CHEBI:", "");
//                if (termInDBService.isTermExist(dbkey)) {
//                    TermInDB tempTermInDB = termInDBService.readByName(dbkey);
//                    tempTermInDB.increaseTimeOfAccurrenceInDB();
//                    tempTermInDB.increaseDatasetFrequency();
//                    termInDBService.update(tempTermInDB);
//
//                    TermInList tempTermInList = new TermInList(dbkey); //here we assume one ref/term only occurrence 1 time in a dataset.
//                    tempTermInList.setIdInDB(tempTermInDB.getId());
//                    terms.add(tempTermInList);
//                } else {
//                    TermInDB newTermInDB = new TermInDB(dbkey, dataType);
//                    termInDBService.insert(newTermInDB);
////                    System.out.println("inserted new term" + newTermInDB.getTermName());
//
//                    TermInList tempTermInList = new TermInList(dbkey); //here we assume one ref/term only occurrence 1 time in a dataset.
//                    tempTermInList.setIdInDB(newTermInDB.getId());
//                    terms.add(tempTermInList);
//                }
//            }
//        }
//        return terms;
//    }

    /**
     * This computes the list of the terms that are interesting and will be use by OmicsDI to the
     * similarity.
     *
     * @param dataType the Data type
     * @param refs
     * @return
     */
    private List<String> getTermsInDataset(String dataType, Map<String, Set<String>> refs) {

        CopyOnWriteArrayList<String> terms = new CopyOnWriteArrayList<>();
        String refKeyWord = null;
        String refKeyWord2 = null;
        if (dataType.equals(DataType.PROTEOMICS_DATA.getName())) {
            refKeyWord  = "uniprot";
            refKeyWord2 = "ensembl";
        } else if (dataType.equals(DataType.METABOLOMICS_DATA.getName())) {
            refKeyWord = "ChEBI";
        } else if (dataType.equals(DataType.TRANSCRIPTOMIC_DATA.getName())){
            refKeyWord = "ensembl";
        }
        String finalRefKeyWord = refKeyWord;
        String finalRefKeyWord1 = refKeyWord2;
        refs.entrySet().parallelStream().forEach(dbkey -> {
            if (dbkey.getKey().equalsIgnoreCase(finalRefKeyWord) || dbkey.getKey().equalsIgnoreCase(finalRefKeyWord1)) {
                dbkey.getValue().parallelStream().forEach( identifier ->{
                    identifier  = identifier.replace("CHEBI:", "");
                    if(identifier != null && !identifier.isEmpty())
                        terms.add(identifier);
            });
        }});
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

        refs.stream().filter(ref -> !refTerms.contains(ref.getDbkey())).forEach(ref -> {
            tempRefs.add(ref);
            refTerms.add(ref.getDbkey());
        });
        return tempRefs;
    }
}
