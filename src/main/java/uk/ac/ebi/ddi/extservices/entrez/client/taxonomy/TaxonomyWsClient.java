package uk.ac.ebi.ddi.extservices.entrez.client.taxonomy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ddi.extservices.ebiprotein.utils.EBITaxonomyUtils;
import uk.ac.ebi.ddi.extservices.entrez.config.TaxWsConfigProd;
import uk.ac.ebi.ddi.extservices.entrez.ncbiresult.NCBITaxResult;
import uk.ac.ebi.ddi.extservices.entrez.ncbiresult.NCBITaxonomyEntry;
import uk.ac.ebi.ddi.extservices.entrez.ncbiresult.NCBITaxonomyEntrySet;

import java.util.Set;


/**
 * @author Yasset Perez-Riverol ypriverol
 */
public class TaxonomyWsClient extends WsClient {

    private static final Logger logger = LoggerFactory.getLogger(TaxonomyWsClient.class);

    /**
     * Default constructor for Ws clients
     *
     * @param config
     */
    public TaxonomyWsClient(TaxWsConfigProd config) {
        super(config);
    }

    public NCBITaxResult getNCBITax(String term){

        if(term != null && term.length() > 0){
            String url = String.format("%s://%s/entrez/eutils/esearch.fcgi?db=taxonomy&term=%s&retmode=JSON",
                    config.getProtocol(), config.getHostName(), term);
            //Todo: Needs to be removed in the future, this is for debugging
            logger.debug(url);

            return this.restTemplate.getForObject(url, NCBITaxResult.class);
        }
        return null;

    }

    public NCBITaxResult getNCBITax(Set<String> terms){

        String query = "";
        if(terms != null && terms.size() > 0){
            for (String term : terms) {
                query = query + "+OR+" + term;
            }
            query = query.replaceFirst("\\+OR\\+","");
            String url = String.format("%s://%s/entrez/eutils/esearch.fcgi?db=taxonomy&term=%s&retmode=JSON",
                    config.getProtocol(), config.getHostName(), query);
            //Todo: Needs to be removed in the future, this is for debugging
            logger.debug(url);

            return this.restTemplate.getForObject(url, NCBITaxResult.class);
        }
        return null;
    }

    public NCBITaxonomyEntrySet getTaxonomyEntryById(String id){

        if(id != null && id.length() > 0){
            String url = String.format("%s://%s/entrez/eutils/efetch.fcgi?db=taxonomy&id=%s",
                    config.getProtocol(), config.getHostName(), id);
            //Todo: Needs to be removed in the future, this is for debugging
            logger.debug(url);
            System.out.println(url);
            return this.restTemplate.getForObject(url, NCBITaxonomyEntrySet.class);
        }
        return null;
    }

    /**
     * Get the parent Entry for a current Entry
     * @param entry entry to search
     * @return Parent Entry
     */
    public NCBITaxonomyEntrySet getParentByEntry(NCBITaxonomyEntry entry){

        String url = entry.getParentTaxId();
        //Todo: Needs to be removed in the future, this is for debugging
        logger.debug(url);

        return this.restTemplate.getForObject(url, NCBITaxonomyEntrySet.class);

    }

    /**
     * Check if the Entry is a Non Rank species and return the parent term if is an Specie
     * or a Genues. See the NCBI Taxonomy Documentation https://www.ncbi.nlm.nih.gov/taxonomy
     * @param id of the Taxonomy
     * @return the Taxonomy of the NonRan parent Taxonomy
     */

    public NCBITaxonomyEntrySet getParentForNonRanSpecie(String id){

        NCBITaxonomyEntrySet entry = getTaxonomyEntryById(id);
        if((entry != null) && (entry.getTaxSet() != null) && (entry.getTaxSet().length == 1) &&
                entry.getTaxSet()[0].getRank().equalsIgnoreCase(EBITaxonomyUtils.EbiTaxRank.NO_RANK.getName()))
            return entry;
        return getParentSpecieOrGenuesTaxonomy((entry != null ? entry.getTaxSet() : new NCBITaxonomyEntry[0])[0].getParentTaxId());
    }


    public NCBITaxonomyEntrySet getParentSpecieOrGenuesTaxonomy(String id){

        NCBITaxonomyEntrySet parent = getTaxonomyEntryById(id);
        if((parent != null) && (parent.getTaxSet() != null) && (parent.getTaxSet().length == 1) &&
                (EBITaxonomyUtils.EbiTaxRank.isSpeciesOrGenues(parent.getTaxSet()[0].getRank())))
            return parent;
        return getTaxonomyEntryById((parent != null ? parent.getTaxSet() : new NCBITaxonomyEntry[0])[0].getParentTaxId());
    }


}
