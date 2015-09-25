package uk.ac.ebi.ddi.annotation.service;

import java.io.IOException;

/**
 * Created by mingze on 22/07/15.
 */
public class DDISynonymServiceTest { //extends TestCase {

    public void testGetSynonyms() throws Exception {

    }

    public static void main(String []args) throws IOException {
        System.out.println("Hello World");
        DDISynonymService synonymService = new DDISynonymService();
        System.out.println(synonymService.getSynonyms("Human"));
    }
}