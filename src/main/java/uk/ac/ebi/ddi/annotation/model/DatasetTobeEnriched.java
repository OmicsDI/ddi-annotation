package uk.ac.ebi.ddi.annotation.model;

import java.util.Map;
/**
 * Created by mingze on 05/10/15.
 */
public class DatasetTobeEnriched {

    private String accession;
    private String database;
    private String dataType;

    private String title;
    private String abstractDescription;
    private String sampleProtocol;
    private String dataProtocol;

    /**
     * Default contructor
     * @param accession accession of the dataaset
     * @param database database or repository where this dataset has been found
     */
    public DatasetTobeEnriched(String accession, String database, String dataType) {
        this.accession = accession;
        this.database = database;
        this.dataType = dataType;
    }

    /**
     * This constructor is more general and can be use to create an object with all the attributes
     * @param accession accession of the dataset
     * @param database  database of the dataset
     * @param title     dataset title
     * @param abstractDescription description of the dataset
     * @param sampleProtocol sample protocol
     * @param dataProtocol   data protocol
     */
    public DatasetTobeEnriched(String accession,
                               String database,
                               String title,
                               String abstractDescription,
                               String sampleProtocol,
                               String dataProtocol) {
        this.accession = accession;
        this.database = database;
        this.title = title;
        this.abstractDescription = abstractDescription;
        this.sampleProtocol = sampleProtocol;
        this.dataProtocol = dataProtocol;
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

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
