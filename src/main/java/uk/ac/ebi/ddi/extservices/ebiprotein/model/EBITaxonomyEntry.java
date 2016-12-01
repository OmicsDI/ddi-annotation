package uk.ac.ebi.ddi.extservices.ebiprotein.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * == General Description ==
 * <p>
 * This class Provides a general information or functionalities for
 * <p>
 * ==Overview==
 * <p>
 * How to used
 * <p>
 * Created by yperez (ypriverol@gmail.com) on 20/10/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class EBITaxonomyEntry {

    @JsonProperty("taxonomyId")
    String taxonomyId;

    @JsonProperty("rank")
    String rank;

    @JsonProperty("scientificName")
    String scientificName;

    @JsonProperty("parentLink")
    String parentLink;

    @JsonProperty("siblingsLinks")
    String[] siblingsLinks;

    public String getTaxonomyId() {
        return taxonomyId;
    }

    public void setTaxonomyId(String taxonomyId) {
        this.taxonomyId = taxonomyId;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getParentLink() {
        return parentLink;
    }

    public void setParentLink(String parentLink) {
        this.parentLink = parentLink;
    }

    public String[] getSiblingsLinks() {
        return siblingsLinks;
    }

    public void setSiblingsLinks(String[] siblingsLinks) {
        this.siblingsLinks = siblingsLinks;
    }
}
