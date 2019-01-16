package uk.ac.ebi.ddi.extservices.entrez.ncbiresult;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 18/05/2015
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class NCBITaxResult {

    @JsonProperty("header")
    NCBIHeader header;

    @JsonProperty("esearchresult")
    NCBIEResult result;

    public NCBIHeader getHeader() {
        return header;
    }

    public void setHeader(NCBIHeader header) {
        this.header = header;
    }

    public NCBIEResult getResult() {
        return result;
    }

    public void setResult(NCBIEResult result) {
        this.result = result;
    }

    public String[] getNCBITaxonomy() {
        if (getResult() != null && getResult().getIdList() != null && getResult().getIdList().length == 1) {
            return getResult().getIdList();
        }
        return null;
    }
}
