package uk.ac.ebi.ddi.biostudies.model;

public class Attributes {

    String name;
    String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }




}
