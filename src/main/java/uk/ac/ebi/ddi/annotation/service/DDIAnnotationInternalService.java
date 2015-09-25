package uk.ac.ebi.ddi.annotation.service;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.*;

import uk.ac.ebi.ddi.annotation.model.AnnotedWord;
import uk.ac.ebi.ddi.annotation.model.WordInField;

public class DDIAnnotationInternalService {

    /**
     *
     * @param fieldText
     * @return the words which are identified in the fieldText by recommender API from bioontology.org
     */
    public String getWordsInFiled(String fieldText) {
        List<WordInField> matchedWords = null;
        JSONArray annotationResults;
        //        try {
        String recommenderPreUrl = "http://data.bioontology.org/recommender?ontologies=MESH,MS&apikey=807fa818-0a7c-43be-9bac-51576e8795f5&input=";
        String recommenderUrl = recommenderPreUrl + fieldText.replace(" ", "%20");
//        String recommenderUrl = recommenderPreUrl + "\'" + fieldText + "\'";
        String output = "";
        try {
            output = getFromWSAPI(recommenderUrl);
        } catch (IOException e) {
            System.out.println("error in getting words in fieldText:" + fieldText + "at this url" + recommenderUrl);
            e.printStackTrace();
        }

//        String output;
//        try {
//            br = new BufferedReader(new FileReader("/home/mingze/work/ddi-annotation/src/main/java/uk/ac/ebi/ddi/annotation/service/annotationResults.txt"));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        try {
//            StringBuilder sb = new StringBuilder();
//            String line = null;
//            try {
//                line = br.readLine();
//                sb.append(line);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            output = sb.toString();
//        } finally {
//            try {
//                br.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        annotationResults = new JSONArray(output);

        for (int i = 0; i < annotationResults.length(); i++) {
            JSONObject annotationResult = (JSONObject) annotationResults.get(i);

            if (annotationResult.getJSONArray("ontologies").length() > 1) {
                System.out.println("There are more than one ontologies here, something must be wrong");
                return "error";
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

        if(matchedWords != null) {
            return matchedWords.toString();
        }
        else{
            return null;
        }
    }

    /**
     *
     * @param word
     * @return get synonyms of the word, by annotator API from bioontology.org
     */
    public ArrayList<String> getSynonymsForWord(String word) {
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
     *
     * @param matchedWord chosen from the first annotation result from annotator API as the matched ontology word
     * @param annotationResults annotation results from annotator API, may contain multiple matched classes
     * @return those clasess which has the same matched word as matchedWord
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
     *
     * @param word
     * @param overlappedWordInList
     * @param matchedWords choose the longer one between word and overlapped word, write it in the matchedWords
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
     *
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


