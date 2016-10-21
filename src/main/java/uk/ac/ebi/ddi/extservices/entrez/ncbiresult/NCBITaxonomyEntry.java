package uk.ac.ebi.ddi.extservices.entrez.ncbiresult;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
@XmlRootElement(name = "Taxon")

public class NCBITaxonomyEntry {
    @XmlElement(name = "TaxId", required = true)
    String taxId;

    @XmlElement(name = "Rank", required = true)
    String rank;

    @XmlElement(name = "ParentTaxId", required = true)
    String parentTaxId;

    public String getTaxId() {
        return taxId;
    }


    public String getRank() {
        return rank;
    }

    public String getParentTaxId() {
        return parentTaxId;
    }

}
