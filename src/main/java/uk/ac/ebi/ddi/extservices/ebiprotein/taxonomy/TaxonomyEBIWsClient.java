package uk.ac.ebi.ddi.extservices.ebiprotein.taxonomy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import uk.ac.ebi.ddi.extservices.ebiprotein.config.TaxEBIPRIDEWsConfigProd;
import uk.ac.ebi.ddi.extservices.ebiprotein.model.EBITaxParent;
import uk.ac.ebi.ddi.extservices.ebiprotein.model.EBITaxonomyEntry;
import uk.ac.ebi.ddi.extservices.ebiprotein.utils.EBITaxonomyUtils;


import java.util.Set;


/**
 * @author Yasset Perez-Riverol ypriverol
 */
public class TaxonomyEBIWsClient extends WsClient {

    private static final Logger logger = LoggerFactory.getLogger(TaxonomyEBIWsClient.class);

    /**
     * Default constructor for Ws clients
     *
     * @param config
     */
    public TaxonomyEBIWsClient(TaxEBIPRIDEWsConfigProd config) {
        super(config);
    }

    public EBITaxParent getTaxonomyParent(String id){

        if(id != null && id.length() > 0){
            String url = String.format("%s://%s/taxonomy/id/%s/parent",
                    config.getProtocol(), config.getHostName(), id);
            //Todo: Needs to be removed in the future, this is for debugging
            logger.debug(url);

            return this.restTemplate.getForObject(url, EBITaxParent.class);
        }
        return null;
    }

    public EBITaxonomyEntry getTaxonomyEntryById(String id){

        try{
            if(id != null && id.length() > 0){
                String url = String.format("%s://%s/taxonomy/id/%s",
                        config.getProtocol(), config.getHostName(), id);
                //Todo: Needs to be removed in the future, this is for debugging
                logger.debug(url);
                System.out.println(url);
                return this.restTemplate.getForObject(url, EBITaxonomyEntry.class);
            }
        }catch (HttpClientErrorException e){
            logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * Get the parent Entry for a current Entry
     * @param entry entry to search
     * @return Parent Entry
     */
    public EBITaxonomyEntry getParentByEntry(EBITaxonomyEntry entry){

        String url = entry.getParentLink();
            //Todo: Needs to be removed in the future, this is for debugging
        logger.debug(url);

        return this.restTemplate.getForObject(url, EBITaxonomyEntry.class);

    }

    /**
     * Check if the Entry is a Non Rank species and return the parent term if is an Specie
     * or a Genues. See the NCBI Taxonomy Documentation https://www.ncbi.nlm.nih.gov/taxonomy
     * @param id of the Taxonomy
     * @return the Taxonomy of the NonRan parent Taxonomy
     */

    public EBITaxonomyEntry getParentForNonRanSpecie(String id){

        EBITaxonomyEntry entry = getTaxonomyEntryById(id);
        if(entry == null )
            return null;
        if(!entry.getRank().equalsIgnoreCase(EBITaxonomyUtils.EbiTaxRank.NO_RANK.getName()))
            return entry;
        return getParentSpecieOrGenuesTaxonomy(entry);
    }


    public EBITaxonomyEntry getParentSpecieOrGenuesTaxonomy(EBITaxonomyEntry entry){

        EBITaxonomyEntry parent = getParentByEntry(entry);
        if(EBITaxonomyUtils.EbiTaxRank.isSpeciesOrGenues(parent.getRank()))
            return parent;
        return getParentByEntry(parent);
    }



}
