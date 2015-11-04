package uk.ac.ebi.ddi.extservices.pubmed.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 18/05/2015
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class Record {

    @JsonProperty("pmcid")
    String pmcid;

    @JsonProperty("pmid")
    String pmid;

    @JsonProperty("doi")
    String doi;

    public String getPmcid() {
        return pmcid;
    }

    public void setPmcid(String pmcid) {
        this.pmcid = pmcid;
    }

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }
}
