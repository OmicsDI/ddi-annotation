package uk.ac.ebi.ddi.extservices.annotator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

/**
 * Created by yperez on 29/05/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SynonymQuery {

    @JsonProperty("prefLabel")
    String prefLabel;

    @JsonProperty("synonym")
    String[] synonyms;

    @JsonProperty("definition")
    String[] definition;

    public String getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(String prefLabel) {
        this.prefLabel = prefLabel;
    }

    public String[] getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String[] synonyms) {
        this.synonyms = synonyms;
    }

    public String[] getDefinition() {
        return definition;
    }

    public void setDefinition(String[] definition) {
        this.definition = definition;
    }

    @Override
    public String toString() {
        return "SynonymQuery{" +
                "prefLabel='" + prefLabel + '\'' +
                ", synonyms=" + Arrays.toString(synonyms) +
                ", definition='" + Arrays.toString(definition) + '\'' +
                '}';
    }
}
