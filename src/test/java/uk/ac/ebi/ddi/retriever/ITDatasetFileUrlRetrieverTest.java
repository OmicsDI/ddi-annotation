package uk.ac.ebi.ddi.retriever;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.ddi.annotation.utils.Constants;
import uk.ac.ebi.ddi.retriever.providers.*;

import java.io.IOException;
import java.util.Set;


public class ITDatasetFileUrlRetrieverTest {

    private IDatasetFileUrlRetriever retriever = new DefaultDatasetFileUrlRetriever();

    public ITDatasetFileUrlRetrieverTest() {
        retriever = new ArrayExpressFileUrlRetriever(retriever);
        retriever = new GEOFileUrlRetriever(retriever);
        retriever = new BioModelsFileUrlRetriever(retriever);
        retriever = new ExpressionAtlasFileUrlRetriever(retriever);
        retriever = new DbGapFileUrlRetriever(retriever);
        retriever = new GNPSFileUrlRetriever(retriever);
        retriever = new JPostFileUrlRetriever(retriever);
        retriever = new MassIVEFileUrlRetriever(retriever);
        retriever = new LincsFileUrlRetriever(retriever);
        retriever = new PeptideAtlasFileUrlRetriever(retriever);
    }

    @Test
    public void testArrayExpress() throws IOException {
        Set<String> files = retriever.getDatasetFiles("E-MEXP-2224", Constants.ARRAYEXPRESS_DATABASE);
        Assert.assertEquals(7, files.size());

        files = retriever.getDatasetFiles("E-GEOD-18213", Constants.ARRAYEXPRESS_DATABASE);
        Assert.assertEquals(4, files.size());
    }

    @Test
    public void testGEODatabase() throws IOException {
        Set<String> files = retriever.getDatasetFiles("GSE4745", Constants.GEO_DATABASE);
        Assert.assertEquals(2, files.size());

        files = retriever.getDatasetFiles("GSE2096", Constants.GEO_DATABASE);
        Assert.assertEquals(0, files.size());
    }

    @Test
    public void testBioModelsDatabase() throws IOException {
        Set<String> files = retriever.getDatasetFiles("BIOMD0000000652", Constants.BIOMODELS_DATABASE);
        Assert.assertEquals(10, files.size());
    }

    @Test
    public void testExpressionAtlas() throws IOException {
        Set<String> files = retriever.getDatasetFiles("E-GEOD-4745", Constants.EXPRESSION_ATLAS_DATABASE);
        Assert.assertEquals(91, files.size());
    }

    @Test
    public void testDBGap() throws IOException {
        Set<String> files = retriever.getDatasetFiles("phs000703", Constants.DBGAP_DATABASE);
        Assert.assertEquals(21, files.size());
    }

    @Test
    public void testGNPSDatabase() throws IOException {
        Set<String> files = retriever.getDatasetFiles("MSV000080113", Constants.GNPS_DATABASE);
        Assert.assertEquals(14, files.size());
    }

    @Test
    public void testJPost() throws IOException {
        Set<String> files = retriever.getDatasetFiles("PXD004621", Constants.JPOST_DATABASE);
        Assert.assertEquals(62, files.size());
    }

    @Test
    public void testMassIVE() throws IOException {
        Set<String> files = retriever.getDatasetFiles("MSV000078822", Constants.MASSIVE_DATABASE);
        Assert.assertEquals(153, files.size());
    }

    @Test
    public void testLincs() throws IOException {
        Set<String> files = retriever.getDatasetFiles("LDS-1372", Constants.LINCS_DATABASE);
        Assert.assertEquals(4, files.size());

        files = retriever.getDatasetFiles("LDS-1226", Constants.LINCS_DATABASE);
        Assert.assertEquals(1, files.size());

        files = retriever.getDatasetFiles("LDS-1237", Constants.LINCS_DATABASE);
        Assert.assertEquals(1, files.size());
    }

    @Test
    public void testPeptideAtlas() throws IOException {
        Set<String> files = retriever.getDatasetFiles("PAe000572", Constants.PEPTIDE_ATLAS_DATABASE);
        Assert.assertEquals(6, files.size());
    }
}