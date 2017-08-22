package uk.ac.ebi.ddi.similaritycalculator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ddi.similarityCalculator.SimilarityCounts;

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
        similarityCounts.getCitationCount("ArrayExpress","E-MEXP-981");
    }

    @Test
    public void addCitations(){
        similarityCounts.addAllCitations();
    }
}


