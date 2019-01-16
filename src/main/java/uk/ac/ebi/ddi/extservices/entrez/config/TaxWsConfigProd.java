package uk.ac.ebi.ddi.extservices.entrez.config;

/**
 * This class help to configure the web-service provider that would be used.
 */
public class TaxWsConfigProd {

    private String hostName;
    private String protocol;

    public TaxWsConfigProd(String protocol, String hostName) {
        this.hostName = hostName;
        this.protocol = protocol;
    }

    public TaxWsConfigProd() {
        this("http", "eutils.ncbi.nlm.nih.gov");
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
