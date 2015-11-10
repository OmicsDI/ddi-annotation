
package uk.ac.ebi.ddi.annotation.service;


import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ddi.annotation.model.DatasetTobeEnriched;
import uk.ac.ebi.ddi.annotation.model.EnrichedDataset;
import uk.ac.ebi.ddi.annotation.utils.Constants;
import uk.ac.ebi.ddi.service.db.model.enrichment.DatasetEnrichmentInfo;
import uk.ac.ebi.ddi.service.db.model.enrichment.WordInField;
import uk.ac.ebi.ddi.service.db.model.enrichment.Synonym;
import uk.ac.ebi.ddi.service.db.service.enrichment.EnrichmentInfoService;
import uk.ac.ebi.ddi.service.db.service.enrichment.SynonymsService;
import uk.ac.ebi.ddi.xml.validator.exception.DDIException;


/**
 * Provide service for synonym annotation
 * @author Mingze
 */
public class DDIAnnotationService {

    private static final Logger logger = LoggerFactory.getLogger(DDIAnnotationService.class);

    @Autowired
    SynonymsService synonymsService;
    @Autowired
    EnrichmentInfoService enrichmentInfoService;

    /**
     * Enrichment on the dataset, includes title, abstraction, sample protocol, data protocol.
     *
     * @param datasetTobeEnriched the dataset to be enrich by the service
     * @return and enriched dataset
     */

    public EnrichedDataset enrichment(DatasetTobeEnriched datasetTobeEnriched) throws JSONException, UnsupportedEncodingException, DDIException {

        String accession = datasetTobeEnriched.getAccession();
        String database = datasetTobeEnriched.getDatabase();

        EnrichedDataset enrichedDataset = new EnrichedDataset(accession,database);
        DatasetEnrichmentInfo datasetEnrichmentInfo = new DatasetEnrichmentInfo(accession,database);

        String title = datasetTobeEnriched.getTitle();
        String abstractDescription = datasetTobeEnriched.getAbstractDescription();
        String sampleProtocol = datasetTobeEnriched.getSampleProtocol();
        String dataProtocol = datasetTobeEnriched.getDataProtocol();

        DatasetEnrichmentInfo prevDatasetInfo = enrichmentInfoService.readByAccession(accession, database);

        List<WordInField> wordsInTitle = null;
        List<WordInField> wordsInAbstractDesc = null;
        List<WordInField> wordsInSampleProtocol = null;
        List<WordInField> wordsInDataProtocol = null;
        if(prevDatasetInfo == null) {
            wordsInTitle = getWordsInFiledFromWS(title);
            wordsInAbstractDesc = getWordsInFiledFromWS(abstractDescription);
            wordsInSampleProtocol = getWordsInFiledFromWS(sampleProtocol);
            wordsInDataProtocol = getWordsInFiledFromWS(dataProtocol);
        }
        else {
            if (title!=null && !title.equals(prevDatasetInfo.getTitleString())) {
                wordsInTitle = getWordsInFiledFromWS(title);
            } else {
                wordsInTitle = prevDatasetInfo.getTitle();
            }
            if (abstractDescription!=null && !abstractDescription.equals(prevDatasetInfo.getAbstractString())) {
                wordsInAbstractDesc = getWordsInFiledFromWS(abstractDescription);
            } else {
                wordsInAbstractDesc = prevDatasetInfo.getAbstractDescription();
            }
            if (sampleProtocol!=null && !sampleProtocol.equals(prevDatasetInfo.getSampleProtocolString())) {
                wordsInSampleProtocol = getWordsInFiledFromWS(sampleProtocol);
            } else {
                wordsInSampleProtocol = prevDatasetInfo.getSampleProtocol();
            }
            if (dataProtocol!=null && !dataProtocol.equals(prevDatasetInfo.getDataProtocolString())) {
                wordsInDataProtocol = getWordsInFiledFromWS(dataProtocol);
            } else {
                wordsInDataProtocol = prevDatasetInfo.getDataProtocol();
            }
        }
        datasetEnrichmentInfo.setTitle(wordsInTitle);
        datasetEnrichmentInfo.setAbstractDescription(wordsInAbstractDesc);
        datasetEnrichmentInfo.setSampleProtocol(wordsInSampleProtocol);
        datasetEnrichmentInfo.setDataProtocol(wordsInDataProtocol);
        datasetEnrichmentInfo.setEnrichTime(new Date());

        datasetEnrichmentInfo.setTitleString(datasetTobeEnriched.getTitle());
        datasetEnrichmentInfo.setAbstractString(datasetTobeEnriched.getAbstractDescription());
        datasetEnrichmentInfo.setSampleProtocolString(datasetTobeEnriched.getSampleProtocol());
        datasetEnrichmentInfo.setDataProtocolString(datasetTobeEnriched.getDataProtocol());
        enrichmentInfoService.insert(datasetEnrichmentInfo);

        enrichedDataset.setEnrichedTitle(EnrichField(wordsInTitle));
        enrichedDataset.setEnrichedAbstractDescription(EnrichField(wordsInAbstractDesc));
        enrichedDataset.setEnrichedSampleProtocol(EnrichField(wordsInSampleProtocol));
        enrichedDataset.setEnrichedDataProtocol(EnrichField(wordsInDataProtocol));

        return enrichedDataset;
    }

    /**
     * Transfer the words found in field to the synonyms String
     *
     * @param wordsInField the words provided by the service
     * @return the final string of the enrichment
     */
    private String EnrichField(List<WordInField> wordsInField) throws JSONException, UnsupportedEncodingException {
        if (wordsInField == null || wordsInField.isEmpty()) {
           return null;
        }
        String enrichedField = "";
        for (WordInField word : wordsInField) {
            List<String> synonymsForWord = getSynonymsForWord(word.getText());
            if(synonymsForWord==null) return null;
            for (String synonym : synonymsForWord) {
                enrichedField += synonym + ", ";
            }
            logger.debug("synonymsForWord:" + synonymsForWord.toString());
            enrichedField = enrichedField.substring(0, enrichedField.length() - 2); //remove the last comma
            enrichedField += "; ";
        }
        enrichedField = enrichedField.substring(0, enrichedField.length() - 2); //remove the last comma
        enrichedField += ".";
        return enrichedField;
    }


    /**
     * Get the biology related words in one field from WebService at bioontology.org
     *
     * @param fieldText a field Text
     * @return the words which are identified in the fieldText by recommender API from bioontology.org
     */
    private List<WordInField> getWordsInFiledFromWS(String fieldText) throws JSONException, UnsupportedEncodingException, DDIException {

        if(fieldText ==null || fieldText.equals(Constants.NOT_AVAILABLE)){
            return null;
        }

        List<WordInField> matchedWords = new ArrayList<>();
        JSONArray annotationResults;
        String recommenderPreUrl = Constants.OBO_INPUT_URL;
        fieldText = fieldText.replace("%", " ");//to avoid malformed error
        String recommenderUrl = recommenderPreUrl + URLEncoder.encode(fieldText, "UTF-8");
        String output = getFromWSAPI(recommenderUrl);
        if(output == null)
            return null;

        annotationResults = new JSONArray(output);

        for (int i = 0; i < annotationResults.length(); i++) {
            JSONObject annotationResult = (JSONObject) annotationResults.get(i);

            if (annotationResult.getJSONArray(Constants.ONTOLOGIES).length() > 1) {
                logger.debug("There are more than one ontologies here, something must be wrong");
                throw new DDIException("There are more than one ontologies here, something must be wrong");
            }

            JSONObject ontology = annotationResult.getJSONArray(Constants.ONTOLOGIES).getJSONObject(0);

            JSONObject coverageResult = annotationResult.getJSONObject(Constants.COVERAGE_RESULT);
            JSONArray matchedTerms = coverageResult.getJSONArray(Constants.ANNOTATIONS);

            matchedWords.addAll(getDistinctWordList(matchedTerms));
            for (WordInField matchedWord : matchedWords) {
                System.out.println(matchedWord.getText() + ":" + matchedWord.getFrom() + "-" + matchedWord.getTo());
            }

        }

        Collections.sort(matchedWords);
        return matchedWords;
    }



    /**
     * Get all synonyms for a word from mongoDB. If this word is not in the DB, then get it's synonyms from Web Service,
     * and insert them into the mongoDB. One assumption: if word1 == word2, word2 == word3, then word1 == word3, == means
     * synonym.
     * @param word to retrieve the given synonyms
     * @return the list of synonyms
     */
    public List<String> getSynonymsForWord(String word) throws JSONException, UnsupportedEncodingException {

        List<String> synonyms;

        if (synonymsService.isWordExist(word)) {

            synonyms = synonymsService.getAllSynonyms(word);

        } else {

            synonyms = getSynonymsForWordFromWS(word);
            Synonym synonym = synonymsService.insert(word, synonyms);
            if(synonym != null && synonym.getSynonyms() != null)
                synonyms = synonym.getSynonyms();
        }

        return synonyms;
    }


    /**
     * get synonyms for a word, from the BioPortal web service API
     *
     * @param word the word to look for synonyms
     * @return get synonyms of the word, by annotator API from bioontology.org
     */
    protected ArrayList<String> getSynonymsForWordFromWS(String word) throws JSONException, UnsupportedEncodingException {
        String lowerWord = word.toLowerCase();
        ArrayList<String> synonyms = new ArrayList<>();

        String annotationPreUrl = Constants.OBO_LONG_URL;
        String annotatorUrl = annotationPreUrl + URLEncoder.encode(lowerWord, "UTF-8");
        String output = "";
        output = getFromWSAPI(annotatorUrl);
        if(output == null)
            return null;


        JSONArray annotationResults = new JSONArray(output);

        if (annotationResults.length() == 0) {
            synonyms.add(Constants.NOT_ANNOTATION_FOUND);
            return synonyms;
        }

        JSONArray annotations = annotationResults.getJSONObject(0).getJSONArray(Constants.ANNOTATIONS);
        JSONObject annotation = annotations.getJSONObject(0);

        String matchType = annotation.getString(Constants.MATCH_TYPE);
        int startPos = annotation.getInt(Constants.FROM);

        if (startPos > 1) {
            synonyms.add(Constants.NOT_ANNOTATION_FOUND);
            return synonyms;
        }

        String matchedWord = annotation.getString(Constants.TEXT).toLowerCase();

        JSONArray matchedClasses = findBioOntologyMatchclasses(matchedWord, annotationResults);

//        synonyms.add(lowerWord);
        for (int i = 0; i < matchedClasses.length(); i++) {
            JSONObject matchedClass = (JSONObject) matchedClasses.get(i);
            String wordId = matchedClass.getString(Constants.WORD_ID);
            String ontologyName = matchedClass.getString(Constants.ONTOLOGY_NAME);

            String wordDetailUrl = Constants.OBO_URL + ontologyName + Constants.CLASSES + wordId + Constants.OBO_API_KEY;
            output = getFromWSAPI(wordDetailUrl);
            if(output == null)
                return null;

            JSONObject wordDetailsInCls = new JSONObject(output);
            JSONArray synonymsInCls = wordDetailsInCls.getJSONArray(Constants.SYNONYM);

            for (i = 0; i < synonymsInCls.length(); i++) {
                String synonymInCls = synonymsInCls.getString(i);
                synonyms.add(synonymInCls);
            }
        }
        return synonyms;
    }

    /**
     * get WebService output from bioportal
     *
     * @param url the url to retrieve the information form the web service
     * @return access url by HTTP client
     */
    private String getFromWSAPI(String url){
        String output = null;
        try {
            final RequestConfig params = RequestConfig.custom().setConnectTimeout(600*1000).setSocketTimeout(600*1000).build();
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();

            logger.debug("Getting from: "+url);

            HttpGet getRequest = new HttpGet(url);
            getRequest.setConfig(params);
            getRequest.addHeader("accept", "application/json");
            HttpResponse response;
            response = httpClient.execute(getRequest);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));
            if (response.getStatusLine().getStatusCode() != 200) {
                logger.error("Failed: HTTP error code:" + response.getStatusLine().toString());
            }else
                output = br.readLine();

        } catch (IOException e) {
            logger.error("Failed: HTTP error code:" + e.getMessage());
        }
        return output;
    }

    /**
     * get the clasess which has the same matched word as matchedWord
     *
     * @param matchedWord       chosen from the first annotation result from annotator API as the matched ontology word
     * @param annotationResults annotation results from annotator API, may contain multiple matched classes
     * @return a JSONArray with all the terms and annotations
     */
    private JSONArray findBioOntologyMatchclasses(String matchedWord, JSONArray annotationResults) throws JSONException {
        JSONArray matchedClasses = new JSONArray();
        for (int i = 0; i < annotationResults.length(); i++) {
            JSONObject annotationResult = annotationResults.getJSONObject(i);
            JSONArray annotations = annotationResult.getJSONArray(Constants.ANNOTATIONS);
            JSONObject annotation = annotations.getJSONObject(0);

            String matchedWordHere = annotation.getString(Constants.TEXT).toLowerCase();
            if (!matchedWordHere.equals(matchedWord)) {
                continue;
            }

            String wordIdString = annotationResult.getJSONObject(Constants.ANNOTATION_CLASS).getString(Constants.ANNOTATION_ID);
            if (Pattern.matches("http:\\/\\/purl\\.bioontology\\.org\\/ontology\\/(.*?)\\/(.*?)", wordIdString)) {
                String ontologyName = wordIdString.replaceAll("http:\\/\\/purl\\.bioontology\\.org\\/ontology\\/(.*)\\/(.*)", "$1");
                String wordId = wordIdString.replaceAll("http:\\/\\/purl\\.bioontology\\.org\\/ontology\\/(.*)\\/(.*)", "$2");
                JSONObject matchedClass = new JSONObject();
                matchedClass.put(Constants.WORD_ID, wordId);
                matchedClass.put(Constants.ONTOLOGY_NAME, ontologyName);
                matchedClasses.put(matchedClass);
                logger.debug(Constants.WORD_ID + " " + matchedClass.get(Constants.WORD_ID));
            }

        }
        return matchedClasses;
    }

    /**
     * @param matchedTerms got from annotation results, which may overlap with other terms
     * @return matchedWords chosen word, which is the longest term in the overlapped terms
     */
    private List<WordInField> getDistinctWordList(JSONArray matchedTerms) throws JSONException {
        List<WordInField> matchedWords = new ArrayList<>();
        for (int i = 0; i < matchedTerms.length(); i++) {
            JSONObject matchedTerm = (JSONObject) matchedTerms.get(i);

            String text = (String) matchedTerm.get(Constants.TEXT);
            int from = (int) matchedTerm.get(Constants.FROM);
            int to = (int) matchedTerm.get(Constants.TO);
            WordInField word = new WordInField(text.toLowerCase(), from, to);

            WordInField overlappedWordInList = findOverlappedWordInList(word, matchedWords);

            if (null == overlappedWordInList) {
                matchedWords.add(word);
            } else {
                modifyWordList(word, overlappedWordInList, matchedWords);
            }
        }

        return matchedWords;
    }

    /**
     * Choose the longer one between word and overlapped word, write it in the matchedWords
     * @param word the word to be search in the
     * @param overlappedWordInList
     * @param matchedWords
     */
    private void modifyWordList(WordInField word, WordInField overlappedWordInList, List<WordInField> matchedWords) {
        int from = word.getFrom();
        int to = word.getTo();

        int overlappedFrom = overlappedWordInList.getFrom();
        int overlappedTo = overlappedWordInList.getTo();

        if (from - overlappedFrom == 0 && to - overlappedTo == 0) {
            return;
        }

        if (from <= overlappedFrom && to >= overlappedTo) {//word
            int index = matchedWords.indexOf(overlappedWordInList);
            matchedWords.set(index, word);
        }
    }

    /**
     * Find the words in matchedWords which is overlapped with "word"
     * @param word
     * @param matchedWords
     * @return
     */
    private WordInField findOverlappedWordInList(WordInField word, List<WordInField> matchedWords) {
        WordInField overlappedWord = null;

        for (WordInField wordInList : matchedWords) {
            if (word.getFrom() <= wordInList.getTo() && word.getTo() >= wordInList.getTo()) {
                logger.debug("find a overlapped word for '" + word + "':" + wordInList);
                overlappedWord = wordInList;
                break;
            }
            if (word.getTo() >= wordInList.getFrom() && word.getTo() <= wordInList.getTo()) {
                logger.debug("find a overlapped word for '" + word + "':" + wordInList);
                overlappedWord = wordInList;
                break;
            }
        }

        return overlappedWord;
    }

}


