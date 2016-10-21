
package uk.ac.ebi.ddi.annotation.service.synonyms;


import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientException;
import uk.ac.ebi.ddi.annotation.model.DatasetTobeEnriched;
import uk.ac.ebi.ddi.annotation.model.EnrichedDataset;
import uk.ac.ebi.ddi.annotation.utils.Constants;
import uk.ac.ebi.ddi.extservices.annotator.client.BioOntologyClient;
import uk.ac.ebi.ddi.extservices.annotator.config.BioOntologyWsConfigProd;
import uk.ac.ebi.ddi.extservices.annotator.model.*;
import uk.ac.ebi.ddi.service.db.model.enrichment.DatasetEnrichmentInfo;
import uk.ac.ebi.ddi.service.db.model.enrichment.WordInField;
import uk.ac.ebi.ddi.service.db.model.enrichment.Synonym;
import uk.ac.ebi.ddi.service.db.service.enrichment.EnrichmentInfoService;
import uk.ac.ebi.ddi.service.db.service.enrichment.SynonymsService;
import uk.ac.ebi.ddi.xml.validator.exception.DDIException;


/**
 * Provide service for synonym annotation
 *
 * @author Mingze
 */
@SuppressWarnings("UnusedAssignment")
public class DDIAnnotationService {

    private static final Logger logger = LoggerFactory.getLogger(DDIAnnotationService.class);

    @Autowired
    SynonymsService synonymsService;
    @Autowired
    EnrichmentInfoService enrichmentInfoService;

    BioOntologyClient recommenderClient = new BioOntologyClient(new BioOntologyWsConfigProd());

    Map<String, String> cachedSynonymUrlForWords = new HashMap<>();

    /**
     * Enrichment on the dataset, includes title, abstraction, sample protocol, data protocol.
     *
     * @param datasetTobeEnriched the dataset to be enrich by the service
     * @return and enriched dataset
     */

    @SuppressWarnings("UnusedAssignment")
    public EnrichedDataset enrichment(DatasetTobeEnriched datasetTobeEnriched, boolean overwrite) throws JSONException, UnsupportedEncodingException, RestClientException,DDIException {

        String accession = datasetTobeEnriched.getAccession();
        String database = datasetTobeEnriched.getDatabase();

        EnrichedDataset enrichedDataset = new EnrichedDataset(accession, database);
        DatasetEnrichmentInfo datasetEnrichmentInfo = new DatasetEnrichmentInfo(accession, database);

        DatasetEnrichmentInfo prevDatasetInfo = enrichmentInfoService.readByAccession(accession, database);

        Map<String, List<WordInField>> synonyms = new HashMap<String, List<WordInField>>();

        if (prevDatasetInfo == null || overwrite)
            synonyms     = getWordsInFiledFromWS(datasetTobeEnriched.getAttributes());
        else {
            for (String key : datasetTobeEnriched.getAttributes().keySet()) {
                if (prevDatasetInfo.getSynonyms() != null && prevDatasetInfo.getSynonyms().containsKey(key) && prevDatasetInfo.getOriginalAttributes().get(key).equals(datasetTobeEnriched.getAttributes().get(key)))
                    synonyms.put(key, prevDatasetInfo.getSynonyms().get(key));
                else {
                    List<WordInField> words = getWordsInFiledFromWS(datasetTobeEnriched.getAttributes().get(key));
                    if (words != null && !words.isEmpty())
                        synonyms.put(key, words);
                }
            }
        }

        datasetEnrichmentInfo.setSynonyms(synonyms);
        datasetEnrichmentInfo.setEnrichTime(new Date());

        datasetEnrichmentInfo.setOriginalAttributes(datasetTobeEnriched.getAttributes());
        enrichmentInfoService.insert(datasetEnrichmentInfo);
        Map<String, String> fields = new HashMap<>();
        synonyms.entrySet().stream().forEach(x -> {
            try {
                fields.put(x.getKey(), EnrichField(x.getValue()));
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
        enrichedDataset.setEnrichedAttributes(fields);

        return enrichedDataset;
    }

    /**
     * Transfer the words found in field to the synonyms String
     *
     * @param wordsInField the words provided by the service
     * @return the final string of the enrichment
     */
    private String EnrichField(List<WordInField> wordsInField) throws JSONException, UnsupportedEncodingException, RestClientException {
        if (wordsInField == null || wordsInField.isEmpty()) {
            return null;
        }
        String enrichedField = "";
        for (WordInField word : wordsInField) {
            List<String> synonymsForWord = getSynonymsForWord(word.getText());
            if (synonymsForWord != null) {
                for (String synonym : synonymsForWord) {
                    enrichedField += synonym + ", ";
                }
                logger.debug("synonymsForWord:" + synonymsForWord.toString());
                if (enrichedField != null && !enrichedField.isEmpty()) {
                    enrichedField = enrichedField.substring(0, enrichedField.length() - 2); //remove the last comma
                    enrichedField += "; ";
                }
            }
        }
        if (enrichedField != null && !enrichedField.isEmpty()) {
            enrichedField = enrichedField.substring(0, enrichedField.length() - 2); //remove the last comma
            enrichedField += ".";
        }
        return enrichedField;
    }


    /**
     * Get the biology related words in one field from WebService at bioontology.org
     *
     * @param fieldText a field Text
     * @return the words which are identified in the fieldText by recommender API from bioontology.org
     */
//    private List<WordInField> getWordsInFiledFromWS(String fieldText) throws JSONException, UnsupportedEncodingException, DDIException {
//
//        if (fieldText == null || fieldText.equals(Constants.NOT_AVAILABLE)) {
//            return null;
//        }
//
//        List<WordInField> matchedWords = new ArrayList<>();
//        JSONArray annotationResults;
//        String recommenderPreUrl = Constants.OBO_INPUT_URL;
//        fieldText = fieldText.replace("%", " ");//to avoid malformed error
//        String recommenderUrl = recommenderPreUrl + URLEncoder.encode(fieldText, "UTF-8");
//        String output = getFromWSAPI(recommenderUrl);
//        if (output == null)
//            return null;
//        System.out.print(output);
//        annotationResults = new JSONArray(output);
//
//        for (int i = 0; i < annotationResults.length(); i++) {
//            JSONObject annotationResult = (JSONObject) annotationResults.get(i);
//
//            if (annotationResult.getJSONArray(Constants.ONTOLOGIES).length() > 1) {
//                logger.debug("There are more than one ontologies here, something must be wrong");
//                throw new DDIException("There are more than one ontologies here, something must be wrong");
//            }
//
//            JSONObject ontology = annotationResult.getJSONArray(Constants.ONTOLOGIES).getJSONObject(0);
//
//            JSONObject coverageResult = annotationResult.getJSONObject(Constants.COVERAGE_RESULT);
//            JSONArray matchedTerms = coverageResult.getJSONArray(Constants.ANNOTATIONS);
//
//            matchedWords.addAll(getDistinctWordList(matchedTerms));
//        }
//
//        Collections.sort(matchedWords);
//        return matchedWords;
//    }

    /**
     * Get the biology related words in one field from WebService at bioontology.org
     *
     * @param fieldText a field Text
     * @return the words which are identified in the fieldText by recommender API from bioontology.org
     */

    private List<WordInField> getWordsInFiledFromWS(String fieldText) throws JSONException, UnsupportedEncodingException, DDIException {

        if (fieldText == null || fieldText.equals(Constants.NOT_AVAILABLE)) {
            return null;
        }

        List<WordInField> matchedWords = new ArrayList<>();
        fieldText = fieldText.replace("%", " ");//to avoid malformed error
        try{
            if(!fieldText.isEmpty()){
                JsonNode annotations = recommenderClient.getAnnotatedSynonyms(fieldText);
                Map<WordInField, Set<String>> synonymsMap = new HashMap<WordInField, Set<String>>();
                if (annotations != null){
                    for(JsonNode annotation: annotations){
                        if(annotation.get("annotations") != null){
                            if(annotation.get("annotations").get("text") != null){
                                String actualWord = annotation.get("annotations").get("text").textValue();
                                int from = annotation.get("annotations").get("from").intValue();
                                int to   = annotation.get("annotations").get("to").intValue();
                                Set<String> synonyms = new HashSet<String>();
                                if(annotation.get("annotatedClass") != null){
                                    actualWord = annotation.get("annotatedClass").get("prefLabel").textValue();
                                    if(annotation.get("annotatedClass").get("synonym") != null){
                                        for(JsonNode synonym: annotation.get("annotatedClass").get("synonym"))
                                            synonyms.add(synonym.textValue());
                                    }
                                }
                                synonymsMap.put(new WordInField(actualWord,from, to), synonyms);
                            }
                        }
                    }
                }
                matchedWords.addAll(getDistinctWordList(synonymsMap));
            }

        }catch (UnsupportedEncodingException | JSONException | RestClientException ex){
            logger.debug(ex.getMessage());
        }

        return matchedWords;
    }

    private Map<String, List<WordInField>> getWordsInFiledFromWS(Map<String, String> fields) throws JSONException, UnsupportedEncodingException, DDIException {

        ConcurrentHashMap<String, List<WordInField>> results = new ConcurrentHashMap<>();

        if (fields == null || fields.isEmpty()) {
            return results;
        }

        fields.entrySet().parallelStream().forEach( field ->{
            if(field.getValue() != null && !field.getValue().equalsIgnoreCase(Constants.NOT_AVAILABLE)){
                List<WordInField> matchedWords = new ArrayList<>();
                String fieldText = field.getValue().replace("%", " ");//to avoid malformed error
                try{
                    if(!fieldText.isEmpty()){
                        JsonNode annotations = recommenderClient.getAnnotatedSynonyms(fieldText);
                        Map<WordInField, Set<String>> synonymsMap = new HashMap<WordInField, Set<String>>();
                        if (annotations != null){
                            for(JsonNode annotation: annotations){
                                if(annotation.get("annotatedClass") != null && annotation.get("annotations") != null){
                                    Set<String> synonyms = new HashSet<String>();
                                    if(annotation.get("annotatedClass") != null){
                                        //actualWord = annotation.get("annotatedClass").get("prefLabel").textValue();
                                        if(annotation.get("annotatedClass").get("synonym") != null){
                                            for(JsonNode synonym: annotation.get("annotatedClass").get("synonym"))
                                                synonyms.add(synonym.textValue());
                                        }
                                    }
                                    List<WordInField> words = new ArrayList<WordInField>();
                                    for(JsonNode annotationValue: annotation.get("annotations")){
                                        String actualWord = annotationValue.get("text").textValue();
                                        int from = annotationValue.get("from").intValue();
                                        int to   = annotationValue.get("to").intValue();
                                        WordInField word = new WordInField(actualWord,from, to);
                                        if(synonymsMap.containsKey(word)){
                                            Set<String> currentSynonyms = synonymsMap.get(word);
                                            currentSynonyms.addAll(synonyms);
                                            synonymsMap.put(word, currentSynonyms);
                                        }else
                                            synonymsMap.put(word, synonyms);
                                    }
                                }
                            }
                        }
                        matchedWords.addAll(getDistinctWordList(synonymsMap));
                    }

                }catch (UnsupportedEncodingException | JSONException | RestClientException ex){
                   logger.debug(ex.getMessage());
                }
                Collections.sort(matchedWords);
                results.put(field.getKey(), matchedWords);
            }
        });

        return results;
    }

    /**
     * Get all synonyms for a word from mongoDB. If this word is not in the DB, then get it's synonyms from Web Service,
     * and insert them into the mongoDB. One assumption: if word1 == word2, word2 == word3, then word1 == word3, == means
     * synonym.
     *
     * @param word to retrieve the given synonyms
     * @return the list of synonyms
     */
    public List<String> getSynonymsForWord(String word) throws JSONException, UnsupportedEncodingException, RestClientException {

        List<String> synonyms;

        if (synonymsService.isWordExist(word)) {

            synonyms = synonymsService.getAllSynonyms(word);

        } else {

            synonyms = getSynonymsForWordFromWS(word);
            Synonym synonym = synonymsService.insert(word, synonyms);
            if (synonym != null && synonym.getSynonyms() != null)
                synonyms = synonym.getSynonyms();
        }

        return synonyms;
    }


    /**
     * get synonyms for a word, from the BioPortal web service API
     * the cachedSynonymRul come from the annotation process
     *
     * @param word the word to look for synonyms
     * @return get synonyms of the word, by annotator API from bioontology.org
     */
    protected ArrayList<String> getSynonymsForWordFromWS(String word) throws JSONException, UnsupportedEncodingException, RestClientException {
        String lowerWord = word.toLowerCase();
        ArrayList<String> synonyms = new ArrayList<>();

        //Todo: Mingze the cache system is not working
//        String wordDetailUrl = cachedSynonymUrlForWords.get(lowerWord);
//        wordDetailUrl = null;
//        if (wordDetailUrl != null) {
//            try {
//                SynonymQuery output = recommenderClient.getAllSynonymByURL(wordDetailUrl);
//                if (output == null)
//                    return null;
//
//                String[] synonymsInCls = output.getSynonyms();
//                Collections.addAll(synonyms, synonymsInCls);
//
//            }catch (RestClientException ex){
//                logger.debug(ex.getMessage());
//                ex.printStackTrace();
//                return null;
//            }
//        } else {
            AnnotatedOntologyQuery[] annotatedTerms = recommenderClient.getAnnotatedTerms(lowerWord, Constants.OBO_ONTOLOGIES);
            if (annotatedTerms == null)
                return null;

            if (annotatedTerms == null || annotatedTerms.length == 0) {
                synonyms.add(Constants.NOT_ANNOTATION_FOUND);
                return synonyms;
            }

            Annotation[] annotations = annotatedTerms[0].getAnnotations();
            Annotation annotation = annotations[0];

            int startPos = annotation.getFromPosition();

            if (startPos > 1) {
                synonyms.add(Constants.NOT_ANNOTATION_FOUND);
                return synonyms;
            }

            String matchedWord = annotation.getText().toLowerCase();

            JSONArray matchedClasses = findBioOntologyMatchclasses(matchedWord, annotatedTerms);

            for (int i = 0; i < matchedClasses.length(); i++) {

                JSONObject matchedClass = (JSONObject) matchedClasses.get(i);
                String wordId = matchedClass.getString(Constants.WORD_ID);
                String ontologyName = matchedClass.getString(Constants.ONTOLOGY_NAME);

                SynonymQuery output = recommenderClient.getAllSynonyms(ontologyName, wordId);
                if (output == null)
                    return null;

                String[] synonymsInCls = output.getSynonyms();

                for (int j = 0; j < synonymsInCls.length; j++) {
                    String synonymInCls = synonymsInCls[j];
                    synonyms.add(synonymInCls);
                }
            }
//        }

        return synonyms;


    }

//    @Deprecated
//    protected ArrayList<String> getSynonymsForWordFromWS(String word) throws JSONException, UnsupportedEncodingException {
//        String lowerWord = word.toLowerCase();
//        ArrayList<String> synonyms = new ArrayList<>();
//
//        String wordDetailUrl = cachedSynonymUrlForWords.get(lowerWord);
//        if (wordDetailUrl != null) {
//            String output = getFromWSAPI(wordDetailUrl + Constants.OBO_API_KEY);
//            if (output == null)
//                return null;
//
//            JSONObject wordDetailsInCls = new JSONObject(output);
//            JSONArray synonymsInCls = wordDetailsInCls.getJSONArray(Constants.SYNONYM);
//
//            for (int i = 0; i < synonymsInCls.length(); i++) {
//                String synonymInCls = synonymsInCls.getString(i);
//                synonyms.add(synonymInCls);
//            }
//        } else {
//            String annotationPreUrl = Constants.OBO_LONG_URL;
//            String annotatorUrl = annotationPreUrl + URLEncoder.encode(lowerWord, "UTF-8");
//            String output = "";
//            output = getFromWSAPI(annotatorUrl);
//            if (output == null)
//                return null;
//
//
//            JSONArray annotationResults = new JSONArray(output);
//
//            if (annotationResults.length() == 0) {
//                synonyms.add(Constants.NOT_ANNOTATION_FOUND);
//                return synonyms;
//            }
//
//            JSONArray annotations = annotationResults.getJSONObject(0).getJSONArray(Constants.ANNOTATIONS);
//            JSONObject annotation = annotations.getJSONObject(0);
//
//            String matchType = annotation.getString(Constants.MATCH_TYPE);
//            int startPos = annotation.getInt(Constants.FROM);
//
//            if (startPos > 1) {
//                synonyms.add(Constants.NOT_ANNOTATION_FOUND);
//                return synonyms;
//            }
//
//            String matchedWord = annotation.getString(Constants.TEXT).toLowerCase();
//
//            JSONArray matchedClasses = findBioOntologyMatchclasses(matchedWord, annotationResults);
//
////        synonyms.add(lowerWord);
//            for (int i = 0; i < matchedClasses.length(); i++) {
//                JSONObject matchedClass = (JSONObject) matchedClasses.get(i);
//                String wordId = matchedClass.getString(Constants.WORD_ID);
//                String ontologyName = matchedClass.getString(Constants.ONTOLOGY_NAME);
//
//                wordDetailUrl = Constants.OBO_URL + ontologyName + Constants.CLASSES + wordId + Constants.OBO_API_KEY;
//                output = getFromWSAPI(wordDetailUrl);
//                if (output == null)
//                    return null;
//
//                JSONObject wordDetailsInCls = new JSONObject(output);
//                JSONArray synonymsInCls = wordDetailsInCls.getJSONArray(Constants.SYNONYM);
//
//                for (i = 0; i < synonymsInCls.length(); i++) {
//                    String synonymInCls = synonymsInCls.getString(i);
//                    synonyms.add(synonymInCls);
//                }
//            }
//        }
//
//        return synonyms;
//
//
//    }

    /**
     * get WebService output from bioportal
     *
     * @param url the url to retrieve the information form the web service
     * @return access url by HTTP client
     */
    private String getFromWSAPI(String url) {
        String output = null;
        try {
            //Todo: This function is not working properly
            final RequestConfig params = RequestConfig.custom().setConnectTimeout(60 * 1000).setSocketTimeout(60 * 1000).build();
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();

            logger.debug("Getting from: " + url);

            HttpGet getRequest = new HttpGet(url);
            getRequest.setConfig(params);
            getRequest.addHeader("accept", "text/html, application/json;");
            HttpResponse response;
            response = httpClient.execute(getRequest);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));
            if (response.getStatusLine().getStatusCode() != 200) {
                logger.error("Failed: HTTP error code:" + response.getStatusLine().toString() + " at " + url);
            } else
                output = br.readLine();

        } catch (IOException e) {
            logger.error("Failed: HTTP error code:" + e.getMessage()+ " at " + url);
        }
        return output;
    }

    @Deprecated
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
     * get the clasess which has the same matched word as matchedWord
     *
     * @param matchedWord       chosen from the first annotation result from annotator API as the matched ontology word
     * @param annotationResults annotation results from annotator API, may contain multiple matched classes
     * @return a JSONArray with all the terms and annotations
     */
    private JSONArray findBioOntologyMatchclasses(String matchedWord, AnnotatedOntologyQuery[] annotationResults) throws JSONException {
        JSONArray matchedClasses = new JSONArray();
        for (AnnotatedOntologyQuery annotationResult : annotationResults) {

            Annotation[] annotations = annotationResult.getAnnotations();
            Annotation annotation = annotations[0];

            String matchedWordHere = annotation.getText().toLowerCase();
            if (!matchedWordHere.equals(matchedWord)) {
                continue;
            }

            String wordIdString = annotationResult.getAnnotatedClass().getId();
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
    @Deprecated
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

                if (!synonymsService.isWordExist(word.getText())) {
                    JSONObject annotatedClass = (JSONObject) matchedTerm.get(Constants.ANNOTATEDCLASS);
                    JSONObject links = (JSONObject) annotatedClass.get(Constants.LINKS);
                    String word_url = (String) links.get(Constants.SELF);
                    cachedSynonymUrlForWords.put(word.getText().toLowerCase(), word_url);
                }
            } else {
                modifyWordList(word, overlappedWordInList, matchedWords);
            }
        }

        return matchedWords;
    }

    private List<WordInField> getDistinctWordList(Annotation[] matchedTerms) throws JSONException {
        List<WordInField> matchedWords = new ArrayList<>();
        if(matchedTerms != null && matchedTerms.length > 0){
            for (Annotation matchedTerm : matchedTerms) {
                String text = matchedTerm.getText();
                int from = matchedTerm.getFromPosition();
                int to = matchedTerm.getToPosition();
                WordInField word = new WordInField(text.toLowerCase(), from, to);

                WordInField overlappedWordInList = findOverlappedWordInList(word, matchedWords);

                if (null == overlappedWordInList) {
                    matchedWords.add(word);

                    if (!synonymsService.isWordExist(word.getText())) {
                        if (matchedTerm.getAnnotatedClass() != null && matchedTerm.getAnnotatedClass().getLinks() != null
                                && matchedTerm.getAnnotatedClass().getLinks().getSelf() != null) {
                            String word_url = matchedTerm.getAnnotatedClass().getLinks().getSelf();
                            cachedSynonymUrlForWords.put(word.getText().toLowerCase(), word_url);
                        }
                    }
                } else {
                    modifyWordList(word, overlappedWordInList, matchedWords);
                }
            }
        }


        return matchedWords;
    }

    private List<WordInField> getDistinctWordList(Map<WordInField, Set<String>> synonyms) throws JSONException {
        List<WordInField> matchedWords = new ArrayList<>();
        if(synonyms != null && synonyms.size() > 0){
            for (Map.Entry matchedTerm : synonyms.entrySet()) {
                WordInField key = (WordInField) matchedTerm.getKey();
                WordInField word = new WordInField(key.getText().toLowerCase(), key.getFrom(), key.getTo());
                WordInField overlappedWordInList = findOverlappedWordInList(word, matchedWords);

                if (null == overlappedWordInList) {
                    matchedWords.add(word);
                }else
                    modifyWordList(word, overlappedWordInList, matchedWords);

                synonymsService.update(new Synonym(word.getText(), (new ArrayList<String>((Set<String>) matchedTerm.getValue()))));

            }
        }
        return matchedWords;
    }


    /**
     * Choose the longer one between word and overlapped word, write it in the matchedWords
     *
     * @param word                 the word to be search in the
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
     *
     * @param word
     * @param matchedWords
     * @return
     */
    private WordInField findOverlappedWordInList(WordInField word, List<WordInField> matchedWords) {
        WordInField overlappedWord = null;

        for (WordInField wordInList : matchedWords) {

            if (word.getFrom() == wordInList.getFrom() && word.getTo() == wordInList.getTo()) {
                logger.debug("find same word for '" + word + "':" + wordInList);
                overlappedWord = wordInList;
                break;
            }
            if (word.getFrom() <= wordInList.getTo() && word.getTo() >= wordInList.getTo()) {
                logger.debug("find an overlapped word for '" + word + "':" + wordInList);
                overlappedWord = wordInList;
                break;
            }
            if (word.getTo() >= wordInList.getFrom() && word.getTo() <= wordInList.getTo()) {
                logger.debug("find an overlapped word for '" + word + "':" + wordInList);
                overlappedWord = wordInList;
                break;
            }
        }

        return overlappedWord;
    }

}


