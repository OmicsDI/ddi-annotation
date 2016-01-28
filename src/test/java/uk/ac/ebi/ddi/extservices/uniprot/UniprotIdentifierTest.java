package uk.ac.ebi.ddi.extservices.uniprot;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 04/12/2015
 */
public class UniprotIdentifierTest {



    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testRetrieve() throws Exception {
        List<String> identifiers = new ArrayList<String>();
        identifiers.add("1433B_HUMAN");
        identifiers.add("1433F_HUMAN");

        List<String> finalIds = UniprotIdentifier.retrieve(identifiers, "ID", "ACC");

        System.out.println(finalIds);

    }
}