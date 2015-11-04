package uk.ac.ebi.ddi.annotation.service;

import uk.ac.ebi.ddi.annotation.utils.DOIUtils;

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

    private DDIPublicationAnnotationService(){

    }

    public static DDIPublicationAnnotationService getInstance(){
        if(instance == null){
            instance = new DDIPublicationAnnotationService();
        }
        return instance;
    }


    List<String> getPubMedIDs(List<String> textList){

        Set<String> pubmedSet = new HashSet<>();

        String fullText = "";

        for(String text: textList)
            fullText = fullText + text + " ";

        if(DOIUtils.containsDOI(fullText))
            pubmedSet.addAll(DOIUtils.extractDOIs(fullText));

        if(pubmedSet != null && pubmedSet.size() > 0){
            Set results = new HashSet();
            for(String pubmedid: pubmedSet){
                pubmedid = DOIUtils.cleanDOI(pubmedid);
                pubmedid = DOIUtils.cleanDOITrail(pubmedid);
                results.add(pubmedid);
            }

            pubmedSet = results;
        }
        return new ArrayList<>(pubmedSet);

    }
}
