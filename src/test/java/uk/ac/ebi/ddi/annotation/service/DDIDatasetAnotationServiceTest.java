package uk.ac.ebi.ddi.annotation.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ddi.annotation.service.dataset.DDIDatasetAnnotationService;
import uk.ac.ebi.ddi.annotation.service.synonyms.DDIAnnotationService;

/**
 * Created by gaur on 25/02/18.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationTestContext.xml"})
public class DDIDatasetAnotationServiceTest {

    @Autowired
    DDIDatasetAnnotationService ddiDatasetAnnotationService;

    /*@Test
    public void getMergeDatasetCount(){
        ddiDatasetAnnotationService.getMergedDatasetCount("ArrayExpress","E-GEOD-24660");
    }*/

    @Test
    public void updatePrivateDataset(){
        ddiDatasetAnnotationService.updatePrivateDataset("BioModels");
    }

    @Test
    public void getPrivateDatasets(){
        ddiDatasetAnnotationService.getPrivateDatasets("BioModels");
    }
}
