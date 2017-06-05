package uk.ac.ebi.ddi.annotation.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ddi.annotation.service.dataset.DDIDatasetAnnotationService;
import uk.ac.ebi.ddi.xml.validator.exception.DDIException;
import uk.ac.ebi.ddi.xml.validator.parser.OmicsXMLFile;
import uk.ac.ebi.ddi.xml.validator.parser.model.Entry;

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
    DDIDatasetAnnotationService annotDatasetService;

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

    @Test
    public void updateDataset(){
        reader.getEntryIds().parallelStream().forEach( id ->{
            try {
                Entry dataset = reader.getEntryById(id);
                dataset.addDate("reload", "2016-06-13");
                annotDatasetService.insertDataset(dataset);
            } catch (DDIException e) {
                e.printStackTrace();
            }
        });

    }

    @Test
    public void updateClaimDataset()
    {
        try
        {
            annotDatasetService.updateDatasetClaim();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
