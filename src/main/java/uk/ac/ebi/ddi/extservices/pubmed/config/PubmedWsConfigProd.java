package uk.ac.ebi.ddi.extservices.pubmed.config;

/**
 * This class help to configure the web-service provider that would be used.
 */
public class PubmedWsConfigProd {

    private String hostName;
    private String protocol;

    public PubmedWsConfigProd(String protocol, String hostName) {
        this.hostName = hostName;
        this.protocol = protocol;
    }

    public PubmedWsConfigProd() {
        this("http", "www.ncbi.nlm.nih.gov");
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
