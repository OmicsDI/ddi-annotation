package uk.ac.ebi.ddi.retriever;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.ddi.annotation.utils.Constants;
import uk.ac.ebi.ddi.retriever.providers.*;

import java.io.IOException;
import java.util.Set;


public class ITDatasetFileUrlRetrieverTest {

    @Test
    public void test() throws IOException {
        IDatasetFileUrlRetriever retriever = new DefaultDatasetFileUrlRetriever();
        retriever = new ArrayExpressFileUrlRetriever(retriever);
        retriever = new GEOFileUrlRetriever(retriever);
        retriever = new BioModelsFileUrlRetriever(retriever);
        retriever = new ExpressionAtlasFileUrlRetriever(retriever);
        retriever = new DbGapFileUrlRetriever(retriever);
        retriever = new GNPSFileUrlRetriever(retriever);

        Set<String> files = retriever.getDatasetFiles("E-MEXP-2224", Constants.ARRAYEXPRESS_DATABASE);
        Assert.assertEquals(7, files.size());

        files = retriever.getDatasetFiles("E-GEOD-18213", Constants.ARRAYEXPRESS_DATABASE);
        Assert.assertEquals(4, files.size());

        files = retriever.getDatasetFiles("GSE4745", Constants.GEO_DATABASE);
        Assert.assertEquals(2, files.size());

        files = retriever.getDatasetFiles("GSE2096", Constants.GEO_DATABASE);
        Assert.assertEquals(0, files.size());

        files = retriever.getDatasetFiles("BIOMD0000000652", Constants.BIOMODELS_DATABASE);
        Assert.assertEquals(10, files.size());

        files = retriever.getDatasetFiles("E-GEOD-4745", Constants.EXPRESSION_ATLAS_DATABASE);
        Assert.assertEquals(91, files.size());

        files = retriever.getDatasetFiles("phs000703", Constants.DBGAP_DATABASE);
        Assert.assertEquals(21, files.size());

        files = retriever.getDatasetFiles("MSV000080113", Constants.GNPS_DATABASE);
        Assert.assertEquals(14, files.size());
    }
}