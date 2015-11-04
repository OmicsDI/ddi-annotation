package uk.ac.ebi.ddi.annotation.service;

import uk.ac.ebi.ddi.annotation.utils.DOIUtils;
import uk.ac.ebi.ddi.extservices.pubmed.client.PubmedWsClient;
import uk.ac.ebi.ddi.extservices.pubmed.config.PubmedWsConfigProd;
import uk.ac.ebi.ddi.extservices.pubmed.model.PubmedJSON;
import uk.ac.ebi.ddi.extservices.pubmed.model.Record;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class help to lookup for doi in text and get the pubmed if
 * a doi is founded.
 *  - Get a list of text and try to look for DOI's to retrieve
 *    the corresponding publication pubmed id.
 *
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 03/11/15
 */
public class DDIPublicationAnnotationService {

    private static DDIPublicationAnnotationService instance;

    PubmedWsClient clientPMC = new PubmedWsClient(new PubmedWsConfigProd());


    /**
     * Private Constructor
     */
    private DDIPublicationAnnotationService(){}

    /**
     * Public instance to be retrieved
     * @return Public-Unique instance
     */
    public static DDIPublicationAnnotationService getInstance(){
        if(instance == null){
            instance = new DDIPublicationAnnotationService();
        }
        return instance;
    }


    /**
     * This function find a set of no n-redundant DOI ids inside free-text, it can be use in high-troughput
     * for the annotation of public DOI
     *
     * @param textList The list of free text
     * @return A list of DOI ids
     */
    public List<String> getDOIListFromText(List<String> textList){

        Set<String> doiSet = new HashSet<>();

        String fullText = "";

        for(String text: textList)
            fullText = fullText + text + " ";

        if(DOIUtils.containsDOI(fullText))
            doiSet.addAll(DOIUtils.extractDOIs(fullText));

        if(doiSet != null && doiSet.size() > 0){
            Set results = new HashSet();
            for(String doID: doiSet){
                doID = DOIUtils.cleanDOI(doID);
                doID = DOIUtils.cleanDOITrail(doID);
                results.add(doID);
            }

            doiSet = results;
        }
        return new ArrayList<>(doiSet);
    }

    /**
     * Return a list of pubmed ids from the doi list for those doi ids that cab be found in pubmed
     *
     * @param doiList
     * @return
     */
    public List<String> getPubMedIDsFromDOIList(List<String> doiList){
        List<String> pubmedIds = new ArrayList<>();
        if(doiList != null && doiList.size() > 0){
            PubmedJSON resultJSON = clientPMC.getPubmedIds(doiList);
            if(resultJSON != null && resultJSON.getRecords() != null && resultJSON.getRecords().length > 0){
                for(Record record: resultJSON.getRecords()){
                    if(record != null && record.getPmid() != null && !record.getPmid().isEmpty())
                        pubmedIds.add(record.getPmid());
                }
            }
        }

        return pubmedIds;
    }


}
