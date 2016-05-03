package uk.ac.ebi.ddi.annotation.service;

import uk.ac.ebi.ddi.extservices.entrez.client.taxonomy.TaxonomyWsClient;
import uk.ac.ebi.ddi.extservices.entrez.config.TaxWsConfigProd;
import uk.ac.ebi.ddi.extservices.entrez.ncbiresult.NCBITaxResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 03/05/2016
 */
public class NCBITaxonomyService {

    private static NCBITaxonomyService instance;

    TaxonomyWsClient taxonomyClient = new TaxonomyWsClient(new TaxWsConfigProd());

    /**
     * Private Constructor
     */
    private NCBITaxonomyService(){}

    /**
     * Public instance to be retrieved
     * @return Public-Unique instance
     */
    public static NCBITaxonomyService getInstance(){
        if(instance == null){
            instance = new NCBITaxonomyService();
        }
        return instance;
    }

    public List<String> getNCBITaxonomy(String term){
        if(term != null && !term.isEmpty()){
            NCBITaxResult ncbiTax = taxonomyClient.getNCBITax(term);
            if(ncbiTax != null && ncbiTax.getNCBITaxonomy() != null && ncbiTax.getNCBITaxonomy().length > 0){
                return getTaxonomyArr(ncbiTax.getNCBITaxonomy());
            }
        }
        return null;
    }

    public List<String> getNCBITaxonomy(List<String> term){
        if(term != null && !term.isEmpty()){
            Set<String> terms = new HashSet<String>(term);
            NCBITaxResult ncbiTax = taxonomyClient.getNCBITax(terms);
            if(ncbiTax != null && ncbiTax.getNCBITaxonomy() != null && ncbiTax.getNCBITaxonomy().length > 0){
                return getTaxonomyArr(ncbiTax.getNCBITaxonomy());
            }
        }
        return null;
    }

    private List<String> getTaxonomyArr(String[] taxonomy){
        List<String> taxonomies = new ArrayList<String>();
        if(taxonomy != null && taxonomy.length > 0){
            for(String tax: taxonomy)
                if(tax != null && !tax.isEmpty())
                    taxonomies.add(tax);
        }
        return  taxonomies;
    }
}
