package uk.ac.ebi.ddi.annotation.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ddi.annotation.utils.DataType;
import uk.ac.ebi.ddi.service.db.service.enrichment.EnrichmentInfoService;
import uk.ac.ebi.ddi.service.db.service.similarity.DatasetStatInfoService;
import uk.ac.ebi.ddi.service.db.service.similarity.ExpOutputDatasetService;
import uk.ac.ebi.ddi.service.db.service.similarity.TermInDBService;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Created by mingze on 22/10/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationTestContext.xml"})
public class DDIXmlProcessServiceTest {

    @Autowired
    DDIXmlProcessService ddiXmlProcessService = new DDIXmlProcessService();

    @Autowired
    TermInDBService termInDBService = new TermInDBService();

    @Autowired
    ExpOutputDatasetService expOutputDatasetService = new ExpOutputDatasetService();

    @Autowired
    DDIExpDataImportService ddiExpDataImportService = new DDIExpDataImportService();

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    DatasetStatInfoService datasetStatInfoService = new DatasetStatInfoService();

    @Autowired
    EnrichmentInfoService enrichmentInfoService = new EnrichmentInfoService();

    @Before
    public void setUp() throws Exception {
        URL fileURL = DDIXmlProcessServiceTest.class.getClassLoader().getResource("pride-files/PRIDE_EBEYE_PRD000123.xml");

        assert fileURL != null;


//
//        expOutputDatasetService.deleteAll();
//        termInDBService.deleteAll();
//        datasetStatInfoService.deleteAll();


    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testXmlFileImport() throws Exception {
//        expOutputDatasetService.deleteAll();
//        termInDBService.deleteAll();
//        datasetStatInfoService.deleteAll();
//        enrichmentInfoService.deleteAll();

        String dataType = DataType.PROTEOMICS_DATA.getName();
        URL fileURL = DDIXmlProcessServiceTest.class.getClassLoader().getResource("pride-files/PRIDE_EBEYE_PRD000123.xml");
        ddiXmlProcessService.xmlFileImport(new File(fileURL.toURI()),dataType);


        dataType = DataType.METABOLOMICS_DATA.getName();
        fileURL = DDIXmlProcessServiceTest.class.getClassLoader().getResource("metabolites-files/MetabolomicsWorkbench_EBEYE_ST000001.xml");
        ddiXmlProcessService.xmlFileImport(new File(fileURL.toURI()),dataType);

    }
}