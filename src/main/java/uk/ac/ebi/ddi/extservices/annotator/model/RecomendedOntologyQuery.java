package uk.ac.ebi.ddi.extservices.annotator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 *  ==Overview==
 *
 *  This class
 *
 * Created by ypriverol (ypriverol@gmail.com) on 29/05/2016.
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
