package uk.ac.ebi.ddi.retriever;

import java.util.HashSet;
import java.util.Set;

public class DefaultDatasetFileUrlRetriever implements IDatasetFileUrlRetriever {

    @Override
    public Set<String> getDatasetFiles(String accession, String database) {
        return new HashSet<>();
    }
}
