package uk.ac.ebi.ddi.annotation.model;

/**
 * Created by mingze on 05/10/15.
 */
public class DatasetTobeEnriched {
    private String accession;
    private String database;

    private String title;
    private String abstractDescription;
    private String sampleProtocol;
    private String dataProtocol;

    public DatasetTobeEnriched(String accession, String database) {
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstractDescription() {
        return abstractDescription;
    }

    public void setAbstractDescription(String abstractDescription) {
        this.abstractDescription = abstractDescription;
    }

    public String getSampleProtocol() {
        return sampleProtocol;
    }

    public void setSampleProtocol(String sampleProtocol) {
        this.sampleProtocol = sampleProtocol;
    }

    public String getDataProtocol() {
        return dataProtocol;
    }

    public void setDataProtocol(String dataProtocol) {
        this.dataProtocol = dataProtocol;
    }
}
