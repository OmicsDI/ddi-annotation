package uk.ac.ebi.ddi.annotation.utils;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 20/10/15
 */
public enum DataType {

    PROTEOMICS_DATA("Proteomics"),
    GENOMICS_DATA("Genomics"),
    METABOLOMICS_DATA("Metabolomics"),
    TRANSCRIPTOMIC_DATA("Transcriptomics");

    private final String name;

    DataType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
