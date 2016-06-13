package uk.ac.ebi.ddi.annotation.model;

import java.util.Map;

/**
 * Created by mingze on 05/10/15.
 */
public class EnrichedDataset {

    private String accession;
    private String database;

    private Map<String, String> enrichedAttributes;

    public EnrichedDataset(String accession, String database) {
        this.accession = accession;
        this.database = database;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public Map<String, String> getEnrichedAttributes() {
        return enrichedAttributes;
    }

    public void setEnrichedAttributes(Map<String, String> enrichedAttributes) {
        this.enrichedAttributes = enrichedAttributes;
    }
}
