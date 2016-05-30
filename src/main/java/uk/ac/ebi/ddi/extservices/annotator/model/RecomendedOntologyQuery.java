package uk.ac.ebi.ddi.extservices.annotator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

/**
 * Created by yperez on 29/05/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecomendedOntologyQuery {

    @JsonProperty("evaluationScore")
    Double evaluationScore;

    @JsonProperty("ontologies")
    Ontology[] ontologies;

    @JsonProperty("coverageResult")
    AnnotationResult results;

    public Double getEvaluationScore() {
        return evaluationScore;
    }

    public void setEvaluationScore(Double evaluationScore) {
        this.evaluationScore = evaluationScore;
    }

    public Ontology[] getOntologies() {
        return ontologies;
    }

    public void setOntologies(Ontology[] ontologies) {
        this.ontologies = ontologies;
    }

    public AnnotationResult getResults() {
        return results;
    }

    public void setResults(AnnotationResult results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "RecomendedOntologyQuery{" +
                "evaluationScore=" + evaluationScore +
                ", ontologies=" + Arrays.toString(ontologies) +
                ", results=" + results +
                '}';
    }
}
