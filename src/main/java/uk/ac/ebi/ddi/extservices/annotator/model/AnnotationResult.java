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
public class AnnotationResult {

    @JsonProperty("score")
    Double score;

    @JsonProperty("annotations")
    Annotation[] annotations;

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Annotation[] annotations) {
        this.annotations = annotations;
    }

    @Override
    public String toString() {
        return "AnnotationResult{" +
                "score=" + score +
                ", annotations=" + Arrays.toString(annotations) +
                '}';
    }
}
