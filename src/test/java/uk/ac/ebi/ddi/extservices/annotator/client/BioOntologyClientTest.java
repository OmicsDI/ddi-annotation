package uk.ac.ebi.ddi.extservices.annotator.client;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.ddi.annotation.utils.Constants;
import uk.ac.ebi.ddi.extservices.annotator.config.BioOntologyWsConfigProd;
import uk.ac.ebi.ddi.extservices.annotator.model.RecomendedOntologyQuery;

/**
 * Created by yperez on 29/05/2016.
 */
public class BioOntologyClientTest {

    BioOntologyWsConfigProd configProd;
    BioOntologyClient client;

    @Before
    public void setUp() throws Exception {
        configProd = new BioOntologyWsConfigProd();
        client = new BioOntologyClient(configProd);
    }

    @Test
    public void getOntologyTerms() throws Exception {
        RecomendedOntologyQuery[] terms = client.postRecommendedTerms("Overexpression+of+Snail+is+associated+with+lymph+node+metastasis+and+poor+prognosis+in+patients+with+gastric+cancer", Constants.OBO_ONTOLOGIES);
        terms.toString();
    }

}