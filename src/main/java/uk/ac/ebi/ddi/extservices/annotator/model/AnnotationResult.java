package uk.ac.ebi.ddi.extservices.annotator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

/**
 * Created by yperez on 29/05/2016.
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
