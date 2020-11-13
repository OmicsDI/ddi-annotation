package uk.ac.ebi.ddi.annotation.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class ENAReadRunDataset {

    @JsonAlias({"study_accession"})
    public String studyAccession;

    @JsonAlias({"fastq_ftp"})
    public String fastqFtp;

    @JsonAlias({"fastq_aspera"})
    public String fastqAspera;

    @JsonAlias({"fastq_galaxy"})
    public String fastqGalaxy;

    @JsonAlias({"run_accession"})
    public String runAccession;



}
