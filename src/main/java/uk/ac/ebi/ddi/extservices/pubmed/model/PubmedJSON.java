package uk.ac.ebi.ddi.extservices.pubmed.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 18/05/2015
 */

@JsonIgnoreProperties(ignoreUnknown = true)

public class PubmedJSON {

    @JsonProperty("status")
    String status;

    @JsonProperty("responseDate")
    String responseDate;

    @JsonProperty("request")
    String request;

    @JsonProperty("warning")
    String warning;

    @JsonProperty("records")
    Record[] records;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(String responseDate) {
        this.responseDate = responseDate;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public Record[] getRecords() {
        return records;
    }

    public void setRecords(Record[] records) {
        this.records = records;
    }
}
