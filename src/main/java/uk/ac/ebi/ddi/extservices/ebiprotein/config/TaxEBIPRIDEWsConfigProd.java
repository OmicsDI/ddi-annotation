package uk.ac.ebi.ddi.extservices.ebiprotein.config;

/**
 * This class help to configure the web-service provider that would be used. This web service help to get the
 * ancestors of one taxonomy and know if they are strains or not.
 * Todo: In the future we can evaluate to move this to NCBI Teaxonomy web service
 */
public class TaxEBIPRIDEWsConfigProd {

    private String hostName;
    private String protocol;

    public TaxEBIPRIDEWsConfigProd(String protocol, String hostName) {
        this.hostName = hostName;
        this.protocol = protocol;
    }

    public TaxEBIPRIDEWsConfigProd() {
        this("http", "www.ebi.ac.uk/proteins/api");
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
