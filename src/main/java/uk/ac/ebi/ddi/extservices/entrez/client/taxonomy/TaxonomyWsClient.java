package uk.ac.ebi.ddi.extservices.entrez.client.taxonomy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ddi.extservices.ebiprotein.utils.EBITaxonomyUtils;
import uk.ac.ebi.ddi.extservices.entrez.config.TaxWsConfigProd;
import uk.ac.ebi.ddi.extservices.entrez.ncbiresult.NCBITaxResult;
import uk.ac.ebi.ddi.extservices.entrez.ncbiresult.NCBITaxonomyEntry;
import uk.ac.ebi.ddi.extservices.entrez.ncbiresult.NCBITaxonomyEntrySet;

import java.net.URI;
import java.util.Set;


/**
 * @author Yasset Perez-Riverol ypriverol
 */
public class TaxonomyWsClient extends WsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaxonomyWsClient.class);

    /**
     * Default constructor for Ws clients
     *
     * @param config
     */
    public TaxonomyWsClient(TaxWsConfigProd config) {
        super(config);
    }

    public NCBITaxResult getNCBITax(String term) {
        if (term != null && term.length() > 0) {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                    .scheme(config.getProtocol())
                    .host(config.getHostName())
                    .path("/entrez/eutils/esearch.fcgi")
                    .queryParam("db", "taxonomy")
                    .queryParam("term", term)
                    .queryParam("retmode", "JSON");
            URI uri = uriComponentsBuilder.build().encode().toUri();
            return getRetryTemplate().execute(context -> restTemplate.getForObject(uri, NCBITaxResult.class));
        }
        return null;

    }

    public NCBITaxResult getNCBITax(Set<String> terms) {

        StringBuilder query = new StringBuilder();
        if (terms != null && terms.size() > 0) {
            for (String term : terms) {
                query.append("+OR+").append(term);
            }
            query = new StringBuilder(query.toString().replaceFirst("\\+OR\\+", ""));
            return getNCBITax(query.toString());
        }
        return null;
    }

    public NCBITaxonomyEntrySet getTaxonomyEntryById(String id) {
        if (id != null && id.length() > 0) {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                    .scheme(config.getProtocol())
                    .host(config.getHostName())
                    .path("/entrez/eutils/efetch.fcgi")
                    .queryParam("db", "taxonomy")
                    .queryParam("id", id);
            URI uri = uriComponentsBuilder.build().encode().toUri();
            return getRetryTemplate().execute(context -> restTemplate.getForObject(uri, NCBITaxonomyEntrySet.class));
        }
        return null;
    }

    /**
     * Get the parent Entry for a current Entry
     * @param entry entry to search
     * @return Parent Entry
     */
    public NCBITaxonomyEntrySet getParentByEntry(NCBITaxonomyEntry entry) {

        String url = entry.getParentTaxId();
        //Todo: Needs to be removed in the future, this is for debugging
        LOGGER.debug(url);

        return this.restTemplate.getForObject(url, NCBITaxonomyEntrySet.class);

    }

    /**
     * Check if the Entry is a Non Rank species and return the parent term if is an Specie
     * or a Genues. See the NCBI Taxonomy Documentation https://www.ncbi.nlm.nih.gov/taxonomy
     * @param id of the Taxonomy
     * @return the Taxonomy of the NonRan parent Taxonomy
     */

    public NCBITaxonomyEntrySet getParentForNonRanSpecie(String id) {

        NCBITaxonomyEntrySet entry = getTaxonomyEntryById(id);
        if ((entry != null) && (entry.getTaxSet() != null) && (entry.getTaxSet().length == 1) &&
                entry.getTaxSet()[0].getRank().equalsIgnoreCase(EBITaxonomyUtils.EbiTaxRank.NO_RANK.getName())) {
            return entry;
        }
        if (entry != null && entry.getTaxSet() != null && entry.getTaxSet().length > 0) {
            return getParentSpecieOrGenuesTaxonomy(entry.getTaxSet()[0].getTaxId());
        }
        NCBITaxonomyEntry ncbiTaxonomyEntry = new NCBITaxonomyEntry();
        return getParentSpecieOrGenuesTaxonomy(ncbiTaxonomyEntry.getParentTaxId());
    }


    public NCBITaxonomyEntrySet getParentSpecieOrGenuesTaxonomy(String id) {

        NCBITaxonomyEntrySet parent = getTaxonomyEntryById(id);
        if ((parent != null) && (parent.getTaxSet() != null) && (parent.getTaxSet().length == 1) &&
                (EBITaxonomyUtils.EbiTaxRank.isSpeciesOrGenues(parent.getTaxSet()[0].getRank()))) {
            return parent;
        }
        if (parent != null && parent.getTaxSet() != null && parent.getTaxSet().length > 0) {
            return getTaxonomyEntryById(parent.getTaxSet()[0].getTaxId());
        }
        NCBITaxonomyEntry ncbiTaxonomyEntry = new NCBITaxonomyEntry();
        return getParentSpecieOrGenuesTaxonomy(ncbiTaxonomyEntry.getParentTaxId());
    }


}
