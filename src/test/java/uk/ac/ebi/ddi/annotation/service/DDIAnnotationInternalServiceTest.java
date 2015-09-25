package uk.ac.ebi.ddi.annotation.service;

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


public class DDIAnnotationInternalServiceTest {

    public static void main(String []args) throws IOException {
        DDIAnnotationInternalService annotService = new DDIAnnotationInternalService();




        long startTime = System.currentTimeMillis();
        for (int i=0; i<10; i++) {
            System.out.println("those words are in ontologies: " + annotService.getWordsInFiled("Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye."));
            for (int j = 0; j < 20; j++) {
//                System.out.println("the synonms is: " + annotService.getSynonymsForWord("cancer"));
                ArrayList<String> temp = annotService.getSynonymsForWord("cancer");
            }
        }
        long endTime = System.currentTimeMillis();

        System.out.println("That took " + (endTime - startTime) + " milliseconds to annotate 100 field from Bioontology web service");

    }
}