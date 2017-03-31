package uk.ac.ebi.ddi.annotation.utils;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 20/10/15
 * Data Types on OmicsDI
 */
public enum DataType {

    PROTEOMICS_DATA("Proteomics"),
    GENOMICS_DATA("Genomics"),
    METABOLOMICS_DATA("Metabolomics"),
    TRANSCRIPTOMIC_DATA("Transcriptomics"),
    MODELS_DATA("Models"),
    INTERACTIONS_DATA("Interactions"),
    PATHWAYS_DATA("Pathways");

    private final String name;

    DataType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
