package uk.ac.ebi.ddi.annotation.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ddi.annotation.model.DatasetTobeEnriched;
import uk.ac.ebi.ddi.annotation.model.EnrichedDataset;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by mingze on 22/07/15.
 */
//public class ddiAnnotationServiceTest extends TestCase {
//
//    public void main() {
//        System.out.println("hello world");
//
//    }
//
//
//}

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationTestContext.xml"})

public class DDIAnnotationServiceTest {
    @Autowired
    DDIAnnotationService annotService = new DDIAnnotationService();

    @Test
    public void annotationTest() throws IOException {

        long startTime = System.currentTimeMillis();
        for (int i=0; i<10; i++) {
    //        System.out.println("those words are in ontologies: " + annotService.getWordsInFiled("Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye."));
            for (int j = 0; j < 20; j++) {
//                System.out.println("the synonms is: " + annotService.getSynonymsForWord("cancer"));
                ArrayList<String> temp = annotService.getSynonymsForWord("cancer");
                for (String temp2 : temp) {
                    System.out.println(temp2);
                }
                System.out.println("--------------------------------");
            }
        }
        long endTime = System.currentTimeMillis();

        System.out.println("That took " + (endTime - startTime) + " milliseconds to annotate 100 field from Bioontology web service");

    }

    @Test
    public void enrichmentTest() throws IOException{

        DatasetTobeEnriched datasetTobeEnriched1 = new DatasetTobeEnriched("pxd0000001", "pride");
        datasetTobeEnriched1.setTitle("Large scale qualitative and quantitative profiling of tyrosine phosphorylation using a combination of phosphopeptide immuno-affinity purification and stable isotope dimethyl labeling");
        datasetTobeEnriched1.setAbstractDescription("Triplex stable isotope dimethyl labeling of phosphotyrosine peptides after EGF stimulation");
        datasetTobeEnriched1.setSampleProtocol("Not available");
        datasetTobeEnriched1.setDataProtocol("Not available");

        EnrichedDataset enrichedDataset1 = annotService.enrichment(datasetTobeEnriched1);
        System.out.println(enrichedDataset1.getEnrichedTitle());
        System.out.println(enrichedDataset1.getEnrichedAbstractDescription());
        System.out.println(enrichedDataset1.getEnrichedSampleProtocol());
        System.out.println(enrichedDataset1.getEnrichedDataProtocol());

    }
}