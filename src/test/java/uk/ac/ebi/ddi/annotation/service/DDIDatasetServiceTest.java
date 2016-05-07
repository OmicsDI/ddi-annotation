package uk.ac.ebi.ddi.annotation.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ddi.xml.validator.exception.DDIException;
import uk.ac.ebi.ddi.xml.validator.parser.OmicsXMLFile;

import java.io.File;
import java.net.URL;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 05/05/2016
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationTestContext.xml"})
public class DDIDatasetServiceTest {

    @Autowired
    DDIDatasetAnnotationService annotDatasetService = new DDIDatasetAnnotationService();

    private OmicsXMLFile reader;

    @Before
    public void setUp() throws Exception {
        URL fileURL = DDIXmlProcessServiceTest.class.getClassLoader().getResource("pride-files/PRIDE_EBEYE_PRD000123.xml");

        assert fileURL != null;
        reader = new OmicsXMLFile(new File(fileURL.toURI()));
    }

    @Test
    public void importDatasets(){

        reader.getEntryIds().parallelStream().forEach( id ->{
            try {
                annotDatasetService.insertDataset(reader.getEntryById(id));
            } catch (DDIException e) {
                e.printStackTrace();
            }
        });
    }
}
