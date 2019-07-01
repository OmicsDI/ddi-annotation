package uk.ac.ebi.ddi.similaritycalculator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ddi.ddidomaindb.dataset.DSField;
import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;
import uk.ac.ebi.ddi.similarityCalculator.SimilarityCounts;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by gaur on 13/07/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationTestContext.xml"})
public class SimilarityCountTest {

    @Autowired
    SimilarityCounts similarityCounts;

    @Test
    public void getCitationCount(){
        similarityCounts.getCitationCount("ArrayExpress","E-MEXP-981", new ArrayList<String>());
    }

   /* @Test
    public void testCitation(){
        List<Dataset> dts = new ArrayList<Dataset>();
                dts.add(similarityCounts.datasetService.read("E-MEXP-981","ArrayExpress"));
        dts.forEach(dt ->  similarityCounts.getCitationCount(
                dt.getDatabase(), dt.getAccession(),
                dt.getAdditional().containsKey(DSField.Additional.SECONDARY_ACCESSION.key()) ?
                        new ArrayList<String>(
                                dt.getAdditional().get(DSField.Additional.SECONDARY_ACCESSION.key())
                        ) :
                        new ArrayList<String>()));
    }*/
    @Test
    public void addCitations(){
        similarityCounts.addAllCitations();
    }

    @Test
    public void addReanalysis(){
        similarityCounts.saveReanalysisCount();
    }

    @Test
    public void addSearchCounts(){
        similarityCounts.saveSearchcounts();
    }

    @Test
    public void testAllRecords() {
        similarityCounts.getPageRecords();
    }

    @Test
    public void getSearchCount(){
        similarityCounts.addSearchCounts("E-MTAB-599","21921910","ExpressionAtlas");
    }

    /*@Test
    public void testDownloadCount(){
        similarityCounts.addDatasetDownloadCount();
    }*/
}


