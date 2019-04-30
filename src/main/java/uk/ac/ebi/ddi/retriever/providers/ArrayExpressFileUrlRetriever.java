package uk.ac.ebi.ddi.retriever.providers;

import org.apache.commons.net.ftp.FTPClient;
import uk.ac.ebi.ddi.ddidomaindb.database.DB;
import uk.ac.ebi.ddi.extservices.net.FtpUtils;
import uk.ac.ebi.ddi.extservices.net.UriUtils;
import uk.ac.ebi.ddi.retriever.DatasetFileUrlRetriever;
import uk.ac.ebi.ddi.retriever.IDatasetFileUrlRetriever;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;


public class ArrayExpressFileUrlRetriever extends DatasetFileUrlRetriever {

    private static final String FTP_ARRAYEXPRESS = "ftp://ftp.ebi.ac.uk/pub/databases/arrayexpress/data/experiment";

    public ArrayExpressFileUrlRetriever(IDatasetFileUrlRetriever datasetDownloadingRetriever) {
        super(datasetDownloadingRetriever);
    }

    @Override
    public Set<String> getAllDatasetFiles(String accession, String database) throws IOException {
        Set<String> result = new HashSet<>();
        String url = String.format("%s/%s/%s", FTP_ARRAYEXPRESS, getPrefix(accession), accession);
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
        return DB.ARRAY_EXPRESS.getDBName().equals(database);
    }

    private String getPrefix(String accession) {
        return accession.split("-")[1];
    }
}
