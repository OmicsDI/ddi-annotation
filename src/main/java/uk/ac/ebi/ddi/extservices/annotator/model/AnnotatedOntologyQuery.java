package uk.ac.ebi.ddi.extservices.annotator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by yperez on 29/05/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnnotatedOntologyQuery {

    @JsonProperty("annotatedClass")
    AnnotatedClass annotatedClass;

    @JsonProperty("annotations")
    Annotation[] annotations;

    public AnnotatedClass getAnnotatedClass() {
        return annotatedClass;
    }

    public void setAnnotatedClass(AnnotatedClass annotatedClass) {
        this.annotatedClass = annotatedClass;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Annotation[] annotations) {
        this.annotations = annotations;
    }
}
