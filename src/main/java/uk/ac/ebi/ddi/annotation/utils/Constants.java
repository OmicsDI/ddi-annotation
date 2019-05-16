package uk.ac.ebi.ddi.annotation.utils;

import uk.ac.ebi.ddi.ddidomaindb.dataset.DSField;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * 20/10/15
 */
public class Constants {
    public static final String OBO_LONG_URL  = "http://data.bioontology.org/annotator?ontologies=MESH,MS&longest_only=true&whole_word_only=false&apikey=807fa818-0a7c-43be-9bac-51576e8795f5&text=";
    public static final String OBO_INPUT_URL = "http://data.bioontology.org/recommender?ontologies=MESH,MS&apikey=807fa818-0a7c-43be-9bac-51576e8795f5&input=";
    public static final String OBO_KEY       = "807fa818-0a7c-43be-9bac-51576e8795f5";
    public static final String[] OBO_ONTOLOGIES = {"MESH", "MS",
                                                   "EFO", "GO-PLUS", "BIOMODELS", "BP",
                                                   "MEDLINEPLUS", "NCBITAXON", "GEXO", "CCO", "CLO",
                                                   "CCONT", "BTO", "OBI", "GO"};
    public static final String ONTOLOGIES    = "ontologies";
    public static final String NOT_AVAILABLE = "Not availabel";
    public static final String COVERAGE_RESULT = "coverageResult";
    public static final String ANNOTATIONS = "annotations";
    public static final String NOT_ANNOTATION_FOUND = "NoAnnotationFound";
    public static final String MATCH_TYPE = "matchType";
    public static final String FROM = "from";
    public static final String TEXT = "text";
    public static final String ANNOTATEDCLASS = "annotatedClass";
    public static final String LINKS = "links";
    public static final String SELF = "self";
    public static final String WORD_ID = "wordId";
    public static final String ONTOLOGY_NAME = "ontologyName";
    public static final String SYNONYM = "synonym";
    public static final String TO = "to";
    public static final String ANNOTATION_CLASS = "annotatedClass";
    public static final String ANNOTATION_ID = "@id";
    public static final String OBO_URL = "http://data.bioontology.org/ontologies/";
    public static final String OBO_API_KEY = "?apikey=807fa818-0a7c-43be-9bac-51576e8795f5";
    public static final String CLASSES = "/classes/";
    public static final String ENSEMBL_DATABASE = "ensembl";
    public static final String UNIPROT_DATABASE = "uniprot";
    public static final String MULTIOMICS_TYPE  = "Multiomics";

    public static final String PUB_DATE_FIELD   = "publication_date";

    public static final String EGA_UPDATED_FIELD = "updated";

    public static final String UNIPROT =  "UNIPROT";

    public static final String CHEBI = "CHEBI";

    public static final String ALL_DOMAIN                 =   "";

    public static final String SEARCH_DOMAIN    = "search_domains";

    public static final String ENAWSGFILECOLUMN = "study_accession,embl_file,fasta_file,master_file";

    public static final String ENAANALYSISCOLUMN = "study_accession,submitted_ftp,submitted_aspera,submitted_galaxy";

    public static final String ENAREADRUNCOLUMN = "study_accession,fastq_ftp,fastq_aspera,fastq_galaxy";

    public static final String[] DATASET_SUMMARY        = {
            DSField.DESCRIPTION.getName(),
            DSField.NAME.getName(),
            DSField.Additional.SUBMITTER_KEYWORDS.getName(),
            DSField.Additional.CURATOR_KEYWORDS.getName(),
            Constants.PUB_DATE_FIELD,
            DSField.CrossRef.TAXONOMY.getName(),
            DSField.Additional.OMICS.getName(),
            DSField.CrossRef.ENSEMBL_EXPRESSION_ATLAS.getName().toUpperCase(),
            Constants.UNIPROT,
            Constants.CHEBI};
}
