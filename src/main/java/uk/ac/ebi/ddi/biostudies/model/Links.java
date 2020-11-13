package uk.ac.ebi.ddi.biostudies.model;

import java.util.List;

public class Links {

    String url;

    List<Attributes> attributes;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Attributes> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attributes> attributes) {
        this.attributes = attributes;
    }


}
