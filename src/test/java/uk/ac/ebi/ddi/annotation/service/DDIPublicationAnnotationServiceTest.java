package uk.ac.ebi.ddi.annotation.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.ddi.extservices.pubmed.client.PubmedWsClient;
import uk.ac.ebi.ddi.extservices.pubmed.config.PubmedWsConfigProd;
import uk.ac.ebi.ddi.extservices.pubmed.model.PubmedJSON;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DDIPublicationAnnotationServiceTest {

    DDIPublicationAnnotationService service;

    @Before
    public void setUp() throws Exception {
        service = DDIPublicationAnnotationService.getInstance();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetPubMedIDs() throws Exception {

        String example = "Rhee HW, Zou P, Udeshi ND, Martell JD, Mootha VK, Carr SA,Ting AY. Science, 2013," +
                " 339, 1328-1331. doi: 10.1126/science.1230593. Microscopy and mass spectrometry" +
                " (MS) are complementary techniques: the former provides" +
                " spatiotemporal information in living cells, but only for a" +
                " handful of recombinant proteins, while the latter can detect" +
                " thousands of endogenous proteins simultaneously, but only in" +
                " lysed samples. Here we introduce technology that combines these" +
                " strengths by offering spatially- and temporally resolved proteomic" +
                " maps of endogenous proteins within living cells." +
                " The method relies on a genetically-targetable peroxidase enzyme" +
                " that biotinylates nearby proteins, which are subsequently" +
                " purified and identified by MS.  doi: 10.1127/science.1230593. We used this approach to" +
                " identify 495 proteins within the human mitochondrial matrix," +
                " including 31 not previously linked to mitochondria. The labeling" +
                " was exceptionally specific and distinguished between inner membrane proteins facing the matrix" +
                " versus the intermembrane space (IMS). Several proteins previously thought to reside in the IMS or" +
                " outer membrane, including protoporphyrinogen oxidase, were reassigned to the matrix. The specificity " +
                " of live-cell peroxidase-mediated proteomic mapping combined with its ease of use offers biologists" +
                " a powerful tool for understanding the molecular composition of living cells.";

        List<String> listTerms = new ArrayList<>();
        listTerms.add(example);
        List<String> dois = service.getPubMedIDs(listTerms);

        PubmedWsClient clientPMC = new PubmedWsClient(new PubmedWsConfigProd());

        PubmedJSON pubmedResult = clientPMC.getPubmedIds(dois);

        System.out.println(pubmedResult.getRecords().length);


    }
}