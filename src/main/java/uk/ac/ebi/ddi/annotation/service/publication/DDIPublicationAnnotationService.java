package uk.ac.ebi.ddi.annotation.service.publication;

import org.springframework.web.client.RestClientException;
import uk.ac.ebi.ddi.annotation.utils.DOIUtils;
import uk.ac.ebi.ddi.ebe.ws.dao.client.publication.PublicationWsClient;
import uk.ac.ebi.ddi.ebe.ws.dao.config.EbeyeWsConfigProd;
import uk.ac.ebi.ddi.ebe.ws.dao.model.common.Entry;
import uk.ac.ebi.ddi.ebe.ws.dao.model.common.QueryResult;
import uk.ac.ebi.ddi.extservices.pubmed.client.PubmedWsClient;
import uk.ac.ebi.ddi.extservices.pubmed.config.PubmedWsConfigProd;
import uk.ac.ebi.ddi.extservices.pubmed.model.PubmedJSON;
import uk.ac.ebi.ddi.extservices.pubmed.model.Record;

import java.util.*;
import java.util.stream.Collectors;

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

    PublicationWsClient publicationWsClient = new PublicationWsClient(new EbeyeWsConfigProd());

    /**
     * Private Constructor
     */
    private DDIPublicationAnnotationService() {
    }

    /**
     * Public instance to be retrieved
     * @return Public-Unique instance
     */
    public static DDIPublicationAnnotationService getInstance() {
        if (instance == null) {
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
    public List<String> getDOIListFromText(List<String> textList) {

        Set<String> doiSet = new HashSet<>();

        StringBuilder fullText = new StringBuilder();

        for (String text: textList) {
            fullText.append(text).append(" ");
        }

        if (DOIUtils.containsDOI(fullText.toString())) {
            doiSet.addAll(DOIUtils.extractDOIs(fullText.toString()));
        }

        if (doiSet.size() > 0) {
            Set<String> results = new HashSet<>();
            for (String doID: doiSet) {
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
    public List<String> getPubMedIDsFromDOIList(List<String> doiList) throws RestClientException {
        List<String> pubmedIds = new ArrayList<>();
        if (doiList != null && doiList.size() > 0) {
            PubmedJSON resultJSON = clientPMC.getPubmedIds(doiList);
            if (resultJSON != null && resultJSON.getRecords() != null && resultJSON.getRecords().length > 0) {
                for (Record record: resultJSON.getRecords()) {
                    if (record != null && record.getPmid() != null && !record.getPmid().isEmpty()) {
                        pubmedIds.add(record.getPmid());
                    }
                }
            }
        }

        return pubmedIds;
    }

    /**
     * This function retrieve from the web service the publication information to be index in the database,
     * also we will generate all the information
     * about the publication reference from the dataset.
     * @param idList
     * @return
     */
    public List<Map<String, String[]>> getAbstractPublication(List<String> idList) throws RestClientException {
        String[] fields = {"description", "name", "author"};
        List<Map<String, String[]>> publications = new ArrayList<>();
        Set<String> finalIds = new HashSet<>(idList);
        finalIds = finalIds.stream().filter(x -> !x.trim().isEmpty()).collect(Collectors.toSet());
        QueryResult pride = publicationWsClient.getPublications(fields, finalIds);
        if (pride != null && pride.getEntries() != null && pride.getEntries().length > 0) {
            for (Entry entry: pride.getEntries()) {
                if (entry.getFields() != null) {
                    publications.add(entry.getFields());
                }
            }
        }
        return publications;
    }
}
