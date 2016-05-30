package uk.ac.ebi.ddi.extservices.annotator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by yperez on 29/05/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Links {

    @JsonProperty("self")
    String self;

    @JsonProperty("ontology")
    String ontology;

    public String getOntology() {
        return ontology;
    }

    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    @Override
    public String toString() {
        return "Links{" +
                "self='" + self + '\'' +
                ", ontology='" + ontology + '\'' +
                '}';
    }
}
