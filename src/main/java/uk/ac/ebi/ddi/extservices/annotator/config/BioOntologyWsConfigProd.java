package uk.ac.ebi.ddi.extservices.annotator.config;

/**
 * This class help to configure the web-service provider that would be used.
 */
public class BioOntologyWsConfigProd {

    private String hostName;
    private String protocol;

    public BioOntologyWsConfigProd(String protocol, String hostName) {
        this.hostName = hostName;
        this.protocol = protocol;

    }

    public BioOntologyWsConfigProd() {
        this("http", "data.bioontology.org");
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
