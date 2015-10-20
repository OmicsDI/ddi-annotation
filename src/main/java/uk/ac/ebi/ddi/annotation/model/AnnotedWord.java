package uk.ac.ebi.ddi.annotation.model;

import java.util.ArrayList;

/**
 * Created by mingze on 23/07/15.
 * annotated word which has its own synonym list
 * ClusterNo is used to cluster the synonyms into a single set
 */
public class AnnotedWord {
    private String label;
    private ArrayList<String> synonymsOfWord;
    private int ClusterNo;

    public int getFrequent() {
        return frequent;
    }

    public void setFrequent(int frequent) {
        this.frequent = frequent;
    }

    public int getClusterNo() {
        return ClusterNo;
    }

    public void setClusterNo(int clusterNo) {
        ClusterNo = clusterNo;
    }

    public ArrayList<String> getSynonymsOfWord() {
        return synonymsOfWord;
    }

    public void setSynonymsOfWord(ArrayList<String> synonymsOfWord) {
        this.synonymsOfWord = synonymsOfWord;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    private int frequent;
}
