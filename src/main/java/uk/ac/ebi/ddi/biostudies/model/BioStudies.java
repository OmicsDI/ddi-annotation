package uk.ac.ebi.ddi.biostudies.model;

import java.util.List;

public class BioStudies {

    public void setSubmissions(List<Submissions> submissions) {
        this.submissions = submissions;
    }

    public List<Submissions> getSubmissions() {
        return submissions;
    }

    private List<Submissions> submissions;

}
