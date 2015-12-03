package uk.ac.ebi.ddi.extservices.entrez.ncbiresult;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 18/05/2015
 */

@JsonIgnoreProperties(ignoreUnknown = true)

public class NCBIEResult {

    @JsonProperty("count")
    int count;

    @JsonProperty("idlist")
    String[] idList;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String[] getIdList() {
        return idList;
    }

    public void setIdList(String[] idList) {
        this.idList = idList;
    }
}
