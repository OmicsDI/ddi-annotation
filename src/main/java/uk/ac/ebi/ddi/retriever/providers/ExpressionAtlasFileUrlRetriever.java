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

public class ExpressionAtlasFileUrlRetriever extends DatasetFileUrlRetriever {

    private static final String FTP_ATLAS = "ftp://ftp.ebi.ac.uk/pub/databases/microarray/data/atlas/experiments";

    public ExpressionAtlasFileUrlRetriever(IDatasetFileUrlRetriever datasetDownloadingRetriever) {
        super(datasetDownloadingRetriever);
    }

    @Override
    public Set<String> getAllDatasetFiles(String accession, String database) throws IOException {
        Set<String> result = new HashSet<>();
        if (database.equals(Constants.EXPRESSION_ATLAS_DATABASE)) {
            String url = String.format("%s/%s", FTP_ATLAS, accession);
            URI uri = UriUtils.toUri(url);
            FTPClient ftpClient = new FTPClient();
            try {
                ftpClient.connect(uri.getHost());
                ftpClient.login("anonymous", "anonymous");
                FtpUtils.getListFiles(ftpClient, uri.getPath(), "archive").stream()
                        .map(x -> String.format("ftp://%s/%s", uri.getHost(), x))
                        .forEach(result::add);
            } finally {
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                }
            }
        }
        return result;
    }
}
