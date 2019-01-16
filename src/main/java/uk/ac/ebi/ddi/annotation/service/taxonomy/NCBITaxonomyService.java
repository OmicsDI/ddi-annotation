package uk.ac.ebi.ddi.annotation.service.taxonomy;

import uk.ac.ebi.ddi.annotation.utils.DatasetUtils;
import uk.ac.ebi.ddi.extservices.entrez.client.taxonomy.TaxonomyWsClient;
import uk.ac.ebi.ddi.extservices.entrez.config.TaxWsConfigProd;
import uk.ac.ebi.ddi.extservices.entrez.ncbiresult.NCBITaxResult;
import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;
import uk.ac.ebi.ddi.xml.validator.utils.Field;

import java.util.*;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 03/05/2016
 */
public class NCBITaxonomyService {

    private static NCBITaxonomyService instance;

    private TaxonomyWsClient taxonomyClient = new TaxonomyWsClient(new TaxWsConfigProd());

    private static Set<String> taxonomySpecies = new HashSet<>();

    /**
     * Private Constructor
     */
    private NCBITaxonomyService() {
    }

    /**
     * Public instance to be retrieved
     * @return Public-Unique instance
     */
    public static NCBITaxonomyService getInstance() {
        if (instance == null) {
            instance = new NCBITaxonomyService();
        }
        return instance;
    }

    public List<String> getNCBITaxonomy(String term) {
        if (term != null && !term.isEmpty()) {
            NCBITaxResult ncbiTax = taxonomyClient.getNCBITax(term);
            if (ncbiTax != null && ncbiTax.getNCBITaxonomy() != null && ncbiTax.getNCBITaxonomy().length > 0) {
                return getTaxonomyArr(ncbiTax.getNCBITaxonomy());
            }
        }
        return Collections.emptyList();
    }

    public List<String> getNCBITaxonomy(List<String> term) {
        if (term != null && !term.isEmpty()) {
            Set<String> terms = new HashSet<>(term);
            NCBITaxResult ncbiTax = taxonomyClient.getNCBITax(terms);
            if (ncbiTax != null && ncbiTax.getNCBITaxonomy() != null && ncbiTax.getNCBITaxonomy().length > 0) {
                return getTaxonomyArr(ncbiTax.getNCBITaxonomy());
            }
        }
        return Collections.emptyList();
    }

    private List<String> getTaxonomyArr(String[] taxonomy) {
        List<String> taxonomies = new ArrayList<>();
        if (taxonomy != null && taxonomy.length > 0) {
            for (String tax: taxonomy) {
                if (tax != null && !tax.isEmpty()) {
                    taxonomies.add(tax);
                }
            }
        }
        return  taxonomies;
    }

    public Dataset annotateSpecies(Dataset dataset) {
        if (dataset.getCrossReferences() != null && !dataset.getCrossReferences().containsKey(Field.TAXONOMY.getName())
                && dataset.getAdditional() != null
                && getAdditionalField(dataset, Field.SPECIE_FIELD.getName()) != null) {
            Set<String> taxs = getAdditionalField(dataset, Field.SPECIE_FIELD.getName());
            List<String> taxonomies = NCBITaxonomyService.getInstance().getNCBITaxonomy(new ArrayList<>(taxs));
            for (String tax : taxonomies) {
                DatasetUtils.addCrossReferenceValue(dataset, Field.TAXONOMY.getName(), tax);
            }
        }
        return dataset;
    }

    private Set<String> getAdditionalField(Dataset dataset, String key) {
        if (dataset.getAdditional() != null && dataset.getAdditional().containsKey(key)) {
            return dataset.getAdditional().get(key);
        }
        return Collections.emptySet();
    }

    public String getParentForNonRanSpecie(String id) {
        return taxonomyClient.getParentForNonRanSpecie(id).getTaxSet()[0].getTaxId();
    }

    public Dataset annotateParentForNonRanSpecies(Dataset dataset) {
        if (dataset.getCrossReferences() != null
                && dataset.getCrossReferences().containsKey(Field.TAXONOMY.getName())) {
            Set<String> taxonomies = dataset.getCrossReferences().get(Field.TAXONOMY.getName());
            Set<String> newTaxonomies = new HashSet<>();

            for (String taxId : taxonomies) {
                if (!taxonomySpecies.contains(taxId)) {
                    String parentID = getParentForNonRanSpecie(taxId);
                    if (!taxId.equalsIgnoreCase(parentID)) {
                        newTaxonomies.add(parentID);
                    } else {
                        taxonomySpecies.add(taxId);
                    }
                }
            }
            taxonomies.addAll(newTaxonomies);
            DatasetUtils.addCrossReferenceValues(dataset, Field.TAXONOMY.getName(), taxonomies);
        }

        return dataset;
    }
}
