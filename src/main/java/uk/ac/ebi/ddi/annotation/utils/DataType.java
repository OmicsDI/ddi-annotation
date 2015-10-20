package uk.ac.ebi.ddi.annotation.utils;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 20/10/15
 */
public enum DataType {

    PROTEOMICS_DATA("ProteomicsData"),
    METABOLOMICS_DATA("MetabolomicsData");

    private final String name;

    private DataType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
