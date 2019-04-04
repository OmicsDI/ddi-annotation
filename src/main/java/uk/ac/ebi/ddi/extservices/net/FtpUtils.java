package uk.ac.ebi.ddi.extservices.net;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FtpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FtpUtils.class);

    public static List<String> getListFiles(FTPClient client, String path, String... ignoreDir) throws IOException {
        List<String> result = new ArrayList<>();
        for (String dir : ignoreDir) {
            if (path.equals(dir)) {
                return result;
            }
        }
        if (!client.changeWorkingDirectory(path)) {
            return Collections.emptyList();
        }
        FTPFile[] ftpFiles = client.listFiles();
        for (FTPFile file : ftpFiles) {
            if (!file.getName().equals(".") && !file.getName().equals("..")) {
                if (file.isDirectory()) {
                    result.addAll(getListFiles(client, file.getName(), ignoreDir));
                } else {
                    String link = String.format("%s/%s", client.printWorkingDirectory(), file.getName());
                    result.add(link);
                }
            }
        }
        client.changeToParentDirectory();
        return result;
    }
}
