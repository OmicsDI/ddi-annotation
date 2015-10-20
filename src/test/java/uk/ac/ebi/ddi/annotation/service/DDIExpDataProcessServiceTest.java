package uk.ac.ebi.ddi.annotation.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ddi.annotation.utils.DataType;
import uk.ac.ebi.ddi.service.db.service.similarity.ExpOutputDatasetService;
import uk.ac.ebi.ddi.service.db.service.similarity.TermInDBService;

/**
 * Created by mingze on 14/09/15.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationTestContext.xml"})
public class DDIExpDataProcessServiceTest {

    @Autowired
    TermInDBService termInDBService = new TermInDBService();

    @Autowired
    DDIDatasetSimilarityService ddiExpDataProcessService = new DDIDatasetSimilarityService();

    @Autowired
    ExpOutputDatasetService expOutputDatasetService = new ExpOutputDatasetService();

    @Autowired
    DDIExpDataImportService ddiExpDataImportService = new DDIExpDataImportService();

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testCalculate() throws Exception {
        ddiExpDataProcessService.calculateIDFWeight(DataType.METABOLOMICS_DATA.getName());
        ddiExpDataProcessService.calculateSimilarity(DataType.METABOLOMICS_DATA.getName());
    }

    public void testCalculateProteomicsData() throws Exception {
        ddiExpDataProcessService.calculateIDFWeight(DataType.PROTEOMICS_DATA.getName());
        ddiExpDataProcessService.calculateSimilarity(DataType.PROTEOMICS_DATA.getName());
    }

    @Test
    public void testCalculateMetabolomicsData() throws Exception {
        ddiExpDataProcessService.calculateIDFWeight(DataType.METABOLOMICS_DATA.getName());
        ddiExpDataProcessService.calculateSimilarity(DataType.METABOLOMICS_DATA.getName());
    }

}
