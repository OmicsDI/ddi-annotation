package uk.ac.ebi.ddi.biostudies.model;

import java.util.List;

public class Subsection {

    String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Attributes> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attributes> attributes) {
        this.attributes = attributes;
    }

    List<Attributes> attributes;

}
