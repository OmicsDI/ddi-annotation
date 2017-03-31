package uk.ac.ebi.ddi.annotation.model;

import java.util.HashMap;
import java.util.Map;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 *  ==Overview==
 *
 *  This class
 *
 * Created by ypriverol (ypriverol@gmail.com) on 05/10/15.
 */
public class DatasetTobeEnriched {

    private String accession;
    private String database;
    private String dataType;

    Map<String, String> attributes;

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
     */
    public DatasetTobeEnriched(String accession,
                               String database,
                               Map<String, String> attributes) {
        this.accession = accession;
        this.database = database;
        this.attributes = attributes;
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

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String key, String value){
        if(attributes == null)
            attributes = new HashMap<>();
        attributes.put(key, value);
    }
}
