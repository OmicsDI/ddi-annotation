package uk.ac.ebi.ddi.annotation.model;

/**
 * Created by mingze on 05/10/15.
 */
public class EnrichedDataset {

    private String accession;
    private String database;

    private String enrichedTitle;
    private String enrichedAbstractDescription;
    private String enrichedSampleProtocol;
    private String enrichedDataProtocol;

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

    public String getEnrichedTitle() {
        return enrichedTitle;
    }

    public void setEnrichedTitle(String enrichedTitle) {
        this.enrichedTitle = enrichedTitle;
    }

    public String getEnrichedAbstractDescription() {
        return enrichedAbstractDescription;
    }

    public void setEnrichedAbstractDescription(String enrichedAbstractDescription) {
        this.enrichedAbstractDescription = enrichedAbstractDescription;
    }

    public String getEnrichedSampleProtocol() {
        return enrichedSampleProtocol;
    }

    public void setEnrichedSampleProtocol(String enrichedSampleProtocol) {
        this.enrichedSampleProtocol = enrichedSampleProtocol;
    }

    public String getEnrichedDataProtocol() {
        return enrichedDataProtocol;
    }

    public void setEnrichedDataProtocol(String enrichedDataProtocol) {
        this.enrichedDataProtocol = enrichedDataProtocol;
    }
}
