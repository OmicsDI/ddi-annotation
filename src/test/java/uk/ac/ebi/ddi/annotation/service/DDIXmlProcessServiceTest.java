package uk.ac.ebi.ddi.annotation.service;

import org.junit.After;
import org.junit.Assert;
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
import uk.ac.ebi.ddi.xml.validator.exception.DDIException;
import uk.ac.ebi.ddi.xml.validator.parser.OmicsXMLFile;
import uk.ac.ebi.ddi.xml.validator.parser.marshaller.OmicsDataMarshaller;
import uk.ac.ebi.ddi.xml.validator.parser.model.Database;
import uk.ac.ebi.ddi.xml.validator.parser.model.Entries;
import uk.ac.ebi.ddi.xml.validator.parser.model.Entry;
import uk.ac.ebi.ddi.xml.validator.parser.model.Reference;
import uk.ac.ebi.ddi.xml.validator.utils.Field;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;

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

    private OmicsXMLFile reader;

    @Before
    public void setUp() throws Exception {
        URL fileURL = DDIXmlProcessServiceTest.class.getClassLoader().getResource("pride-files/PRIDE_EBEYE_PRD000123.xml");

        assert fileURL != null;


        reader = new OmicsXMLFile(new File(fileURL.toURI()));

//
//        expOutputDatasetService.deleteAll();
//        termInDBService.deleteAll();
//        datasetStatInfoService.deleteAll();


    }

    @Test
    public void testGetEntryIds() throws Exception {

        Assert.assertEquals(reader.getEntryIds().size(),1);

    }




    @Test
    public void testGetEntryById() throws Exception {

        Entry entry = reader.getEntryById("PRD000123");

        Assert.assertEquals(entry.getName().getValue(), "Large scale qualitative and quantitative profiling of tyrosine phosphorylation using a combination of phosphopeptide immuno-affinity purification and stable isotope dimethyl labeling");

        System.out.println(entry.toString());

    }




    @Test
    public void marshall() throws DDIException {

        FileWriter fw;
        File tmpFile;
        try {
            tmpFile = File.createTempFile("tmpMzML", ".xml");
            tmpFile.deleteOnExit();
            fw = new FileWriter(tmpFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not create or write to temporary file for marshalling.");
        }

        OmicsDataMarshaller mm = new OmicsDataMarshaller();

        Entry entry = reader.getEntryById("PRD000123");

        Database database = new Database();
        database.setDescription("new description");
        database.setEntryCount(10);
        database.setRelease("2010");
        Entries entries = new Entries();
        entries.addEntry(entry);
        database.setEntries(entries);
        mm.marshall(database, fw);

        OmicsXMLFile.isSchemaValid(tmpFile);
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

        @Test
    public void testMetabolomicsInsert() throws Exception {

        URL urlMetabolomics = DDIXmlProcessServiceTest.class.getClassLoader().getResource("metabolites-files");

        assert urlMetabolomics != null;
        File folder = new File(urlMetabolomics.toURI());
        String dataType = DataType.METABOLOMICS_DATA.getName();

        File[] listOfFiles = folder.listFiles();

        if (termInDBService == null) {
            System.err.println("termInDBService is null");
            System.exit(1);
        }

        //delete all data in Mongodb
//        expOutputDatasetService.deleteAll();
//        termInDBService.deleteAll();
//        datasetStatInfoService.deleteAll();


//        int iterTime = 199;
        int index = 1;
        int fileindex = 1;
        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                if(file.getName().toLowerCase().endsWith("xml")) {
                    System.out.println("\n\n"+fileindex + "-" + file.getName()+":");
                    fileindex++;
                    reader = new OmicsXMLFile(file);
                    String database = reader.getName();
                    for (int i=0; i < reader.getEntryIds().size(); i++) {
                        System.out.println("deal the" + index + "entry in "+file.getName()+";");
                        index++;
                        Entry entry = reader.getEntryByIndex(i);
                        String entryAccession = entry.getId();
                        List<Reference> refs = entry.getCrossReferences().getRef();
                        ddiExpDataImportService.importDatasetTerms(dataType, entryAccession, entry.getAdditionalFieldValue(Field.REPOSITORY.getName()), refs);
                    }
                }
            }
        }

    }


    @Test
    public void testProteomicsImportMethod() throws Exception {

        URL urlProteomics = DDIXmlProcessServiceTest.class.getClassLoader().getResource("pride-files");

        assert urlProteomics != null;
        File folder = new File(urlProteomics.toURI());
        String dataType = DataType.PROTEOMICS_DATA.getName();

        File[] listOfFiles = folder.listFiles();

        if (termInDBService == null) {
            System.err.println("termInDBService is null");
            System.exit(1);
        }

        //delete all data in Mongodb
//        expOutputDatasetService.deleteAll();
//        termInDBService.deleteAll();
//        datasetStatInfoService.deleteAll();


//        int iterTime = 199;
        int index = 1;
        int fileindex = 1;
        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                if(file.getName().toLowerCase().endsWith("xml")) {
                    System.out.println("\n\n"+fileindex + "-" + file.getName()+":");
                    fileindex++;
                    reader = new OmicsXMLFile(file);
                    String database = reader.getName();
                    for (int i=0; i<reader.getEntryIds().size(); i++) {
                        System.out.println("deal the" + index + "entry in "+file.getName()+";");
                        index++;
                        Entry entry = reader.getEntryByIndex(i);
                        String entryId = entry.getId();
                        List<Reference> refs = entry.getCrossReferences().getRef();
                        ddiExpDataImportService.importDatasetTerms(dataType, entryId,database,refs);
                    }
                }
            }
        }

    }



    @After
    public void tearDown() throws Exception {

    }

}