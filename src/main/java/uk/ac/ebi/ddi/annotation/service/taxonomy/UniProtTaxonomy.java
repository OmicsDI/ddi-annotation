package uk.ac.ebi.ddi.annotation.service.taxonomy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ddi.annotation.utils.DatasetUtils;
import uk.ac.ebi.ddi.extservices.ebiprotein.config.TaxEBIPRIDEWsConfigProd;
import uk.ac.ebi.ddi.extservices.ebiprotein.model.EBITaxonomyEntry;
import uk.ac.ebi.ddi.extservices.ebiprotein.taxonomy.TaxonomyEBIWsClient;
import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;
import uk.ac.ebi.ddi.xml.validator.utils.Field;

import java.util.HashSet;
import java.util.Set;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * == General Description ==
 * <p>
 * This class Provides a general information or functionalities for
 * <p>
 * ==Overview==
 * <p>
 * How to used
 * <p>
 * Created by yperez (ypriverol@gmail.com) on 20/10/2016.
 */
public class UniProtTaxonomy {

    private static UniProtTaxonomy instance;

    private static final Logger LOGGER = LoggerFactory.getLogger(UniProtTaxonomy.class);

    TaxonomyEBIWsClient taxonomyClient = new TaxonomyEBIWsClient(new TaxEBIPRIDEWsConfigProd());

    private static Set<String> taxonomySpecies = new HashSet<>();

    /**
     * Private Constructor
     */
    private UniProtTaxonomy() {
    }

    /**
     * Public instance to be retrieved
     * @return Public-Unique instance
     */
    public static UniProtTaxonomy getInstance() {
        if (instance == null) {
            instance = new UniProtTaxonomy();
        }
        return instance;
    }

    public String getParentForNonRanSpecie(String id) {
        EBITaxonomyEntry parent = taxonomyClient.getParentForNonRanSpecie(id);
        if (parent != null) {
            return parent.getTaxonomyId();
        }
        return id;
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
            if (newTaxonomies.size() > 0) {
                LOGGER.debug("{}: {}", dataset.getAccession(), newTaxonomies.size());
            }
            dataset = DatasetUtils.addCrossReferenceValues(dataset, Field.TAXONOMY.getName(), taxonomies);
        }

        return dataset;
    }

}
