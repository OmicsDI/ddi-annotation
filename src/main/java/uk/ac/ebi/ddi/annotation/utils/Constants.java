package uk.ac.ebi.ddi.annotation.utils;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * 20/10/15
 */
public class Constants {
    public static final String OBO_LONG_URL  = "http://data.bioontology.org/annotator?ontologies=MESH,MS&longest_only=true&whole_word_only=false&apikey=807fa818-0a7c-43be-9bac-51576e8795f5&text=";
    public static final String OBO_INPUT_URL = "http://data.bioontology.org/recommender?ontologies=MESH,MS&apikey=807fa818-0a7c-43be-9bac-51576e8795f5&input=";
    public static final String OBO_KEY       = "807fa818-0a7c-43be-9bac-51576e8795f5";
    public static final String[] OBO_ONTOLOGIES = {"MESH","MS",
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
    public static final String TAXONOMY_FIELD           = "TAXONOMY";

    public static final String MAIN_DOMAIN              = "omics";

    public static final String REPOSITORY_TAG           = "Repositories";

    public static final String TISSUE_FIELD             = "tissue" ;

    public static final String OMICS_TYPE_FIELD         = "omics_type";

    public static final String DISEASE_FIELD            = "disease";

    public static final String DESCRIPTION_FIELD        = "description";

    public static final String NAME_FIELD               = "name";

    public static final String SUBMITTER_KEY_FIELD      = "submitter_keywords";

    public static final String CURATOR_KEY_FIELD        = "curator_keywords";

    public static final String PUB_DATE_FIELD           = "publication_date";

    public static final String PUB_DATE_FIELD_OPTIONAL  = "publication";

    public static final String[] PUB_DATES = new String[]{PUB_DATE_FIELD, PUB_DATE_FIELD_OPTIONAL};

    public static final String EGA_UPDATED_FIELD        = "updated";

    public static final String DATA_PROTOCOL_FIELD      = "data_protocol";

    public static final String SAMPLE_PROTOCOL_FIELD    = "sample_protocol";

    public static final String PUBMED_FIELD             = "pubmed";

    public static final String DATASET_LINK_FIELD       = "full_dataset_link";

    public static final String INSTRUMENT_FIELD         = "instrument_platform";

    public static final String EXPERIMENT_TYPE_FIELD    = "technology_type";

    public static final String ORGANIZATION_FIELD       = "submitter_affiliation";

    public static final String DATES_FIELD              = "dates";

    public static String SUBMITTER_FIELD                = "submitter";

    public static String SUBMITTER_MAIL_FIELD           = "submitter_mail";

    public static String LAB_HEAD_FIELD                 =  "labhead";

    public static String LAB_HEAD_MAIL_FIELD            =  "labhead_mail";

    public static String ENSEMBL                        =   "ENSEMBL";

    public static String UNIPROT                        =   "UNIPROT";

    public static String CHEBI                          =   "CHEBI";

    public static String CITATION_FIELD                 =   "citationCount";

    public static String SEARCH_FIELD                   =   "searchCount";

    public static final String[] DATASET_SUMMARY        = {Constants.DESCRIPTION_FIELD,
            Constants.NAME_FIELD,
            Constants.SUBMITTER_KEY_FIELD,Constants.CURATOR_KEY_FIELD,
            Constants.PUB_DATE_FIELD,
            Constants.TAXONOMY_FIELD,
            Constants.OMICS_TYPE_FIELD,
            Constants. ENSEMBL,
            Constants.UNIPROT,
            Constants.CHEBI};

}
