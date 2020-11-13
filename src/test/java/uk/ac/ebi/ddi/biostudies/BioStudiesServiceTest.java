package uk.ac.ebi.ddi.biostudies;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationTestContext.xml"})
public class BioStudiesServiceTest {

    @Autowired
    BioStudiesService bioStudiesService;

/*    @Autowired
    IBioStudiesParserService bioStudiesParserService;*/

    @Test
    public void testBioStudies() throws IOException {
        bioStudiesService.saveStudies("/media/gaur/Elements/biostudies/publicOnlyStudies.json");
    }
}
