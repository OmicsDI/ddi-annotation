package uk.ac.ebi.ddi.annotation.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ddi.service.db.service.intersection.ExpOutputDatasetService;
import uk.ac.ebi.ddi.service.db.service.intersection.TermInDBService;

/**
 * Created by mingze on 14/09/15.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationTestContext.xml"})
public class DDIExpDataProcessServiceTest {

    @Autowired
    TermInDBService termInDBService = new TermInDBService();

    @Autowired
    DDIExpDataProcessService ddiExpDataProcessService = new DDIExpDataProcessService();
//    DDIExpDataProcessService ddiExpDataProcessService = new DDIExpDataProcessService("MetabolomicsData");

    @Autowired
    ExpOutputDatasetService expOutputDatasetService = new ExpOutputDatasetService();

    @Autowired
    DDIExpDataImportService ddiExpDataImportService = new DDIExpDataImportService();

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testCalculate() throws Exception {
//        ddiExpDataProcessService.calculateIDFWeight("ProteomicsData");
//        ddiExpDataProcessService.calculateIntersections("ProteomicsData");
        ddiExpDataProcessService.calculateIDFWeight("MetabolomicsData");
        ddiExpDataProcessService.calculateIntersections("MetabolomicsData");
    }

    @Test
    public void testCalculateIntersections() throws Exception {
    }
}