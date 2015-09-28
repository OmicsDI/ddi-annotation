package uk.ac.ebi.ddi.annotation;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ddi.annotation.service.DDIExpDataImportService;
import uk.ac.ebi.ddi.service.db.model.intersection.ExpOutputDataset;
import uk.ac.ebi.ddi.service.db.model.intersection.TermInDB;
import uk.ac.ebi.ddi.xml.validator.exception.DDIException;
import uk.ac.ebi.ddi.xml.validator.parser.OmicsXMLFile;
import uk.ac.ebi.ddi.xml.validator.parser.marshaller.OmicsDataMarshaller;
import uk.ac.ebi.ddi.xml.validator.parser.model.*;
import uk.ac.ebi.ddi.service.db.service.intersection.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationTestContext.xml"})


public class  IntersectionTest{

    @Autowired
    TermInDBService termInDBService = new TermInDBService();

    @Autowired
    ExpOutputDatasetService expOutputDatasetService = new ExpOutputDatasetService();

    @Autowired
    DDIExpDataImportService ddiExpDataImportService = new DDIExpDataImportService();

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    DatasetStatInfoService datasetStatInfoService= new DatasetStatInfoService();


    public static int getIntersection(HashSet<String> set1, HashSet<String> set2) {
        boolean set1IsLarger = set1.size() > set2.size();
        Set<String> cloneSet = new HashSet<String>(set1IsLarger ? set2 : set1);
        cloneSet.retainAll(set1IsLarger ? set1 : set2);
        return cloneSet.size();
    }


    File file;

    OmicsXMLFile reader;

    @Before
    public void setUp() throws Exception {


    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetEntryById() throws Exception {

        Entry entry = reader.getEntryById("PRD000123");

        Assert.assertEquals(entry.getName().getValue(), "Large scale qualitative and quantitative profiling of tyrosine phosphorylation using a combination of phosphopeptide immuno-affinity purification and stable isotope dimethyl labeling");

        System.out.println(entry.toString());

    }

    @Test
    public void testImportMethod() throws Exception {

        PrintWriter writer = new PrintWriter("/home/mingze/work/ddi-annotation/src/test/resources/testPrideFiles/data.txt", "UTF-8");


        File folder = new File("/home/mingze/work/ddi-annotation/src/test/resources/testPrideFiles");
        String dataType = "ProteomicsData";
//        File folder = new File("/home/mingze/work/ddi-annotation/src/test/resources/testMetabolitesFiles");
//        String dataType = "MetabolomicsData";

        File[] listOfFiles = folder.listFiles();
        HashMap entriesMap = new HashMap<String, ArrayList<String>>();
        HashMap termsMap = new HashMap<String, HashSet<String>>();

        if (termInDBService == null) {
            System.err.println("termInDBService is null");
            System.exit(1);
        }

        //delete all data in Mongodb
        expOutputDatasetService.deleteAll();
        termInDBService.deleteAll();
        datasetStatInfoService.deleteAll();


//        int iterTime = 199;
        int index = 1;
        int fileindex = 1;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                if(file.getName().toLowerCase().endsWith("xml")) {
                    System.out.println("\n\n"+fileindex + "-" + file.getName()+":");
                    fileindex++;
                    reader = new OmicsXMLFile(file);
                    for (int i=0; i<reader.getEntryIds().size(); i++) {
                        System.out.println("deal the" + index + "entry in "+file.getName()+";");
                        index++;
                        Entry entry = reader.getEntryByIndex(i);
                        String entryId = entry.getId().toString();
                        List<Reference> refs = entry.getCrossReferences().getRef();
                        ddiExpDataImportService.importDataset(dataType, entryId, refs);
                    }
                }
            }
//            if(iterTime-- ==0)break;
        }

    }

    @Test
    public void testGetEntryIds() throws Exception {

        Assert.assertEquals(reader.getEntryIds().size(),1);

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
    public void testGetEntryByIndex() throws Exception {

        int index = 0;

        Entry entry = reader.getEntryByIndex(index);

        Assert.assertEquals(entry.getName().getValue(), "Large scale qualitative and quantitative profiling of tyrosine phosphorylation using a combination of phosphopeptide immuno-affinity purification and stable isotope dimethyl labeling");

        System.out.println(entry.toString());

    }
}