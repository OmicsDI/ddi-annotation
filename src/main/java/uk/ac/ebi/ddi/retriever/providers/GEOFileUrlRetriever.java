package uk.ac.ebi.ddi.retriever.providers;

import org.apache.commons.net.ftp.FTPClient;
import uk.ac.ebi.ddi.annotation.utils.Constants;
import uk.ac.ebi.ddi.extservices.net.FtpUtils;
import uk.ac.ebi.ddi.extservices.net.UriUtils;
import uk.ac.ebi.ddi.retriever.DatasetFileUrlRetriever;
import uk.ac.ebi.ddi.retriever.IDatasetFileUrlRetriever;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class GEOFileUrlRetriever extends DatasetFileUrlRetriever {

    private static final String FTP_GEO = "ftp://ftp.ncbi.nlm.nih.gov/geo";

    public GEOFileUrlRetriever(IDatasetFileUrlRetriever datasetDownloadingRetriever) {
        super(datasetDownloadingRetriever);
    }

    @Override
    public Set<String> getAllDatasetFiles(String accession, String database) throws IOException {
        Set<String> result = new HashSet<>();
        String url = FTP_GEO;
        if (isSerial(accession)) {
            url += "/series";
        } else if (isDataset(accession)) {
            url += "/datasets";
        } else {
            throw new RuntimeException("GEO accession not found: " + accession);
        }
        url += "/" + accession.substring(0, accession.length() - 3) + "nnn";
        url += "/" + accession;
        url += "/suppl";
        URI uri = UriUtils.toUri(url);
        FTPClient ftpClient = createFtpClient();
        try {
            ftpClient.connect(uri.getHost());
            ftpClient.login("anonymous", "anonymous");
            FtpUtils.getListFiles(ftpClient, uri.getPath()).stream()
                    .map(x -> String.format("ftp://%s%s", uri.getHost(), x))
                    .forEach(result::add);
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
            }
        }
        return result;
    }

    @Override
    protected boolean isSupported(String database) {
        return database.equals(Constants.GEO_DATABASE);
    }

    private boolean isSerial(String accession) {
        return accession.contains("GSE");
    }

    private boolean isDataset(String accession) {
        return accession.contains("GDS");
    }
}
