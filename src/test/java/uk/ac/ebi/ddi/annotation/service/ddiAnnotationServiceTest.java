package uk.ac.ebi.ddi.annotation.service;

import junit.framework.TestCase;

import java.io.IOException;

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


public class ddiAnnotationServiceTest{

    public static void main(String []args) throws IOException {
        System.out.println("Hello World");
        ddiAnnotationService annotService = new ddiAnnotationService();
        System.out.println("those words are in ontologies: " + annotService.getWordsInFiled("Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye."));
        System.out.println("the synonms is: " + annotService.getSynonymsForWord("cancer"));
    }
}