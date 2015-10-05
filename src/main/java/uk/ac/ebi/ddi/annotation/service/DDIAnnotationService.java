package uk.ac.ebi.ddi.annotation.service;


import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.*;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ddi.annotation.model.AnnotedWord;
import uk.ac.ebi.ddi.annotation.model.DatasetTobeEnriched;
import uk.ac.ebi.ddi.annotation.model.EnrichedDataset;
import uk.ac.ebi.ddi.service.db.model.enrichment.DatasetEnrichmentInfo;
import uk.ac.ebi.ddi.service.db.model.enrichment.WordInField;
import uk.ac.ebi.ddi.service.db.model.enrichment.Synonym;
import uk.ac.ebi.ddi.service.db.service.enrichment.EnrichmentInfoService;
import uk.ac.ebi.ddi.service.db.service.enrichment.SynonymsService;


/**
 * Provide service for synonym annotation
 * @author Mingze
 */
public class DDIAnnotationService {

    @Autowired
    SynonymsService synonymsService = new SynonymsService();
    @Autowired
    EnrichmentInfoService enrichmentInfoService = new EnrichmentInfoService();

    /**
     * Enrichment on the dataset, includes title, abstraction, sample protocol, data protocol.
     * @param datasetTobeEnriched
     * @return
     */

    public EnrichedDataset enrichment(DatasetTobeEnriched datasetTobeEnriched) {

        String accession = datasetTobeEnriched.getAccession();
        String database = datasetTobeEnriched.getDatabase();

        EnrichedDataset enrichedDataset = new EnrichedDataset(accession,database);
        DatasetEnrichmentInfo datasetEnrichmentInfo = new DatasetEnrichmentInfo(accession,database);

        String title = datasetTobeEnriched.getTitle();
        String abstractDescription = datasetTobeEnriched.getAbstractDescription();
        String sampleProtocol = datasetTobeEnriched.getSampleProtocol();
        String dataProtocol = datasetTobeEnriched.getDataProtocol();

        List<WordInField> wordsInTitle = getWordsInFiledFromWS(title);
        List<WordInField> wordsInAbstractDesc = getWordsInFiledFromWS(abstractDescription);
        List<WordInField> wordsInSampleProtocol = getWordsInFiledFromWS(sampleProtocol);
        List<WordInField> wordsInDataProtocol = getWordsInFiledFromWS(dataProtocol);

        datasetEnrichmentInfo.setTitle(wordsInTitle);
        datasetEnrichmentInfo.setAbstractDescription(wordsInAbstractDesc);
        datasetEnrichmentInfo.setSampleProtocol(wordsInSampleProtocol);
        datasetEnrichmentInfo.setDataProtocol(wordsInDataProtocol);
        datasetEnrichmentInfo.setEnrichTime(new Date());
        enrichmentInfoService.insert(datasetEnrichmentInfo);

        enrichedDataset.setEnrichedTitle(EnrichField(wordsInTitle));
        enrichedDataset.setEnrichedAbstractDescription(EnrichField(wordsInAbstractDesc));
        enrichedDataset.setEnrichedSampleProtocol(EnrichField(wordsInSampleProtocol));
        enrichedDataset.setEnrichedDataProtocol(EnrichField(wordsInDataProtocol));

        return enrichedDataset;
    }

    /**
     * Transfer the words found in field to the synonyms String
     * @param wordsInField
     * @return
     */
    private String EnrichField(List<WordInField> wordsInField) {
        if (wordsInField == null) {
           return null;
        }
        String enrichedField = "";
        for (WordInField word : wordsInField) {
            List<String> synonymsForWord = getSynonymsForWord(word.getText());
            for (String synonym : synonymsForWord) {
                enrichedField += synonym + ", ";
            }
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
     * @param fieldText
     * @return the words which are identified in the fieldText by recommender API from bioontology.org
     */
    private List<WordInField> getWordsInFiledFromWS(String fieldText) {

        if(fieldText ==null || fieldText.equals("Not availabel")){
            return null;
        }

        List<WordInField> matchedWords = null;
        JSONArray annotationResults;
        String recommenderPreUrl = "http://data.bioontology.org/recommender?ontologies=MESH,MS&apikey=807fa818-0a7c-43be-9bac-51576e8795f5&input=";
        String recommenderUrl = recommenderPreUrl + fieldText.replace(" ", "%20");
        String output = "";
        try {
            output = getFromWSAPI(recommenderUrl);
        } catch (IOException e) {
            System.out.println("error in getting words in fieldText:" + fieldText + "at this url" + recommenderUrl);
            e.printStackTrace();
        }

        annotationResults = new JSONArray(output);

        for (int i = 0; i < annotationResults.length(); i++) {
            JSONObject annotationResult = (JSONObject) annotationResults.get(i);

            if (annotationResult.getJSONArray("ontologies").length() > 1) {
                System.out.println("There are more than one ontologies here, something must be wrong");
                System.exit(1);
            }

            JSONObject ontology = annotationResult.getJSONArray("ontologies").getJSONObject(0);

            String ontologyName = (String) ontology.get("acronym");
            JSONObject coverageResult = annotationResult.getJSONObject("coverageResult");
            int numberOfTerms = coverageResult.getInt("numberTermsCovered");
            JSONArray matchedTerms = coverageResult.getJSONArray("annotations");


//            System.out.println(ontologyName);
//            System.out.println("number of terms" + numberOfTerms);
//
//            System.out.println(recommenderUrl);
//            System.out.println(matchedTerms);
            matchedWords = getDistinctWordList(matchedTerms);
            for (WordInField matchedWord : matchedWords) {
                System.out.println(matchedWord.getText() + ":" + matchedWord.getFrom() + "-" + matchedWord.getTo());
            }

        }

        if (matchedWords != null) {
            return matchedWords;
        } else {
            return null;
        }
    }



    /**
     * Get all synonyms for a word from mongoDB. If this word is not in the DB, then get it's synonyms from Web Service,
     * and insert them into the mongoDB. One assumption: if word1 == word2, word2 == word3, then word1 == word3, == means
     * synonym.
     * @param word
     * @return
     */
    public ArrayList<String> getSynonymsForWord(String word) {

        if (synonymsService.isWordExist(word)) {
            return synonymsService.getAllSynonyms(word);
        } else {
            ArrayList<String> synonyms = getSynonymsForWordFromWS(word);
            String mainWordLabel = null;
            for (String synonym : synonyms) {
                if (synonymsService.isWordExist(synonym)) {
                    mainWordLabel = synonym;
                    break;
                }
            }

            if (mainWordLabel == null) {
                mainWordLabel = word;
                Synonym mainWordSynonym = synonymsService.insert(mainWordLabel);
                for (String synonym : synonyms) {
                    if (synonym != mainWordLabel && !synonymsService.isWordExist(synonym)) {
                        synonymsService.insertAsSynonym(mainWordSynonym, synonym);
                    }
                }

            }else {  //main word already exist, insert others as main word's synonyms
                System.out.println("We have a special situation: " + mainWordLabel + "is a synonym of " + word + ", " + mainWordLabel + "exists but not" + word );

                Synonym mainWordSynonym = synonymsService.readByLabel(mainWordLabel);
                for (String synonym : synonyms) {
                    if (!synonym.equals(mainWordLabel) && !synonymsService.isWordExist(synonym)) {
                        synonymsService.insertAsSynonym(mainWordSynonym, synonym);
                    }
                }
            }

        }

        return synonymsService.getAllSynonyms(word);
    }


    /**
     * get synonyms for a word, from the bioportal web service API
     *
     * @param word
     * @return get synonyms of the word, by annotator API from bioontology.org
     */
    protected ArrayList<String> getSynonymsForWordFromWS(String word) {
        String lowerWord = word.toLowerCase();
        ArrayList<String> synonyms = new ArrayList<String>();

        String annotationPreUrl = "http://data.bioontology.org/annotator?ontologies=MESH,MS&longest_only=true&whole_word_only=false&apikey=807fa818-0a7c-43be-9bac-51576e8795f5&text=";
        String annotatorUrl = annotationPreUrl + lowerWord;
        String output = "";
        try {
            output = getFromWSAPI(annotatorUrl);
        } catch (IOException e) {
            System.out.println("error to get synonyms for word:" + word + "at this url" + annotatorUrl);
            System.out.println("error info:" + e);
        }

        JSONArray annotationResults = new JSONArray(output);

        if (annotationResults.length() == 0) {
            synonyms.add("NoAnnotationFound");
            return synonyms;
        }

        JSONArray annotations = annotationResults.getJSONObject(0).getJSONArray("annotations");
        JSONObject annotation = annotations.getJSONObject(0);

        String matchType = annotation.getString("matchType");
        int startPos = annotation.getInt("from");

        if (startPos > 1) {
            synonyms.add("NoAnnotationFound");
            return synonyms;
        }

        String matchedWord = annotation.getString("text").toLowerCase();
        int suffixStartPos = annotation.getInt("to");
        String suffixString = word.substring(suffixStartPos, word.length());

        int wordLength = word.length();

        JSONArray matchedClasses = findBioOntologyMatchclasses(matchedWord, annotationResults);

        synonyms.add(lowerWord);
        for (int i = 0; i < matchedClasses.length(); i++) {
            JSONObject matchedClass = (JSONObject) matchedClasses.get(i);
            String wordId = matchedClass.getString("wordId");
            String ontologyName = matchedClass.getString("ontologyName");

            String wordDetailUrl = "http://data.bioontology.org/ontologies/" + ontologyName + "/classes/" + wordId + "?apikey=807fa818-0a7c-43be-9bac-51576e8795f5";
            try {
                output = getFromWSAPI(wordDetailUrl);
            } catch (IOException e) {
                System.out.println("error in getting synonyms for word:" + word + "at this url" + wordDetailUrl);
                e.printStackTrace();
            }
            JSONObject wordDetailsInCls = new JSONObject(output);
            JSONArray synonymsInCls = wordDetailsInCls.getJSONArray("synonym");

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
     * @param url
     * @return access url by HTTP client
     * @throws IOException
     */
    private String getFromWSAPI(String url) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet getRequest = new HttpGet(url);
        getRequest.addHeader("accept", "application/json");
        HttpResponse response = httpClient.execute(getRequest);
//        System.out.println("Trying to accessing webservice from:" + url);
        if (response.getStatusLine().getStatusCode() != 200) {
//            throw new RuntimeException("Failed : HTTP error code : "
//                    + response.getStatusLine().getStatusCode());
            System.out.println("Failed: HTTP error code:" + response.getStatusLine().getStatusCode());
        }

        BufferedReader br = new BufferedReader(
                new InputStreamReader((response.getEntity().getContent())));

        String output = br.readLine();
        return output;
    }

    /**
     * get the clasess which has the same matched word as matchedWord
     *
     * @param matchedWord       chosen from the first annotation result from annotator API as the matched ontology word
     * @param annotationResults annotation results from annotator API, may contain multiple matched classes
     * @return
     */
    private JSONArray findBioOntologyMatchclasses(String matchedWord, JSONArray annotationResults) {
        JSONArray matchedClasses = new JSONArray();
//        System.out.print(annotationResults);
        for (int i = 0; i < annotationResults.length(); i++) {
            JSONObject annotationResult = annotationResults.getJSONObject(i);
            JSONArray annotations = annotationResult.getJSONArray("annotations");
            JSONObject annotation = annotations.getJSONObject(0);

            String matchedWordHere = annotation.getString("text").toLowerCase();
            if (!matchedWordHere.equals(matchedWord)) {
                continue;
            }

            String wordIdString = annotationResult.getJSONObject("annotatedClass").getString("@id");
            if (Pattern.matches("http:\\/\\/purl\\.bioontology\\.org\\/ontology\\/(.*?)\\/(.*?)", wordIdString)) {
                String ontologyName = wordIdString.replaceAll("http:\\/\\/purl\\.bioontology\\.org\\/ontology\\/(.*)\\/(.*)", "$1");
                String wordId = wordIdString.replaceAll("http:\\/\\/purl\\.bioontology\\.org\\/ontology\\/(.*)\\/(.*)", "$2");
                JSONObject matchedClass = new JSONObject();
                matchedClass.put("wordId", wordId);
                matchedClass.put("ontologyName", ontologyName);
                matchedClasses.put(matchedClass);
                System.out.println("wordId " + matchedClass.get("wordId"));
            }

        }
        return matchedClasses;
    }

    private ArrayList<AnnotedWord> annotate(JSONArray annotationResults, ArrayList<String> removedWords) {

        return null;
    }


    /**
     * @param matchedTerms got from annotation results, which may overlap with other terms
     * @return matchedWords chosen word, which is the longest term in the overlapped terms
     */
    private List<WordInField> getDistinctWordList(JSONArray matchedTerms) {
        List<WordInField> matchedWords = new ArrayList<WordInField>();
        for (int i = 0; i < matchedTerms.length(); i++) {
            JSONObject matchedTerm = (JSONObject) matchedTerms.get(i);

            String text = (String) matchedTerm.get("text");
            int from = (int) matchedTerm.get("from");
            int to = (int) matchedTerm.get("to");
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
     * @param word
     * @param overlappedWordInList
     * @param matchedWords         choose the longer one between word and overlapped word, write it in the matchedWords
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
     * @param word
     * @param matchedWords
     * @return find the words in matchedWords which is overlapped with "word"
     */
    private WordInField findOverlappedWordInList(WordInField word, List<WordInField> matchedWords) {
        WordInField overlappedWord = null;

        for (int i = 0; i < matchedWords.size(); i++) {
            WordInField wordInList = matchedWords.get(i);

            if (word.getFrom() <= wordInList.getTo() && word.getTo() >= wordInList.getTo()) {
                System.out.println("find a overlapped word for '" + word + "':" + wordInList);
                overlappedWord = wordInList;
                break;
            }
            if (word.getTo() >= wordInList.getFrom() && word.getTo() <= wordInList.getTo()) {
                System.out.println("find a overlapped word for '" + word + "':" + wordInList);
                overlappedWord = wordInList;
                break;
            }
        }

        return overlappedWord;
    }

}


