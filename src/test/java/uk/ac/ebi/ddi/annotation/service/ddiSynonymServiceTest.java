package uk.ac.ebi.ddi.annotation.service;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Created by mingze on 22/07/15.
 */
public class ddiSynonymServiceTest { //extends TestCase {

    public void testGetSynonyms() throws Exception {

    }

    public static void main(String []args) throws IOException {
        System.out.println("Hello World");
        ddiSynonymService synonymService = new ddiSynonymService();
        System.out.println(synonymService.getSynonyms("Human"));
    }
}