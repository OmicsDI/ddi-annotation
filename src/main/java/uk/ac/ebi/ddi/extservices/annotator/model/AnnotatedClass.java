package uk.ac.ebi.ddi.extservices.annotator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Created by yperez on 29/05/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnnotatedClass {

    @JsonProperty("@id")
    String id;

    @JsonProperty("@type")
    String type;

    @JsonProperty("links")
    Links links;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }
}
