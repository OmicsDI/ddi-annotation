package uk.ac.ebi.ddi.similarityCalculator;

import com.google.common.collect.Multiset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ddi.downloas.logs.ElasticSearchWsClient;
import uk.ac.ebi.ddi.downloas.logs.ElasticSearchWsConfigProd;
import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;
import uk.ac.ebi.ddi.service.db.service.dataset.IDatasetService;
import uk.ac.ebi.ddi.service.db.utils.Constants;

import java.util.*;

/*
* @author Gaurhari
*
* Service to get download counts and update datasets with respective counts
*
* */
@Service
public class StatisticsService {

    @Autowired
    IDatasetService datasetService;

    ElasticSearchWsClient elasticSearchClient = new ElasticSearchWsClient(
            new ElasticSearchWsConfigProd(9200, "10.3.10.28", "readall", "readall"));

    //list of databases for which downloads will be retrieved
    List<String> databases = Arrays.asList(Constants.Database.ARRAY_EXPRESS.getDatabaseName(),
            Constants.Database.PRIDE.getDatabaseName(), Constants.Database.EXPRESSION_ATLAS.getDatabaseName(),
            Constants.Database.EVA.getDatabaseName(), Constants.Database.METABOLIGHTS.getDatabaseName(),
            Constants.Database.ENA.getDatabaseName());

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsService.class);


    /*
    * function to save download counts for each dataset
    * */
    public void saveDatasetDownloadCount() {

        int pageSize = 100;
        int pageCounts =  datasetService.readAll(0, pageSize).getTotalPages();

        elasticSearchClient.setParallel(false);

        //retrieveing each dataset from mongodb
        for (int i = 0; i < pageCounts; i++) {

            datasetService.readAll(i, pageSize).map(dt -> {
                if (databases.contains(dt.getDatabase())) {
                    /*
                    * map of accession , files and counts of datasets and months
                    * */
                    Map<String, Map<String, Multiset<String>>> prideDownloads = elasticSearchClient
                            .getDataDownloads(ElasticSearchWsConfigProd.DB.valueOf(dt.getDatabase()),
                                    dt.getAccession());

                    Dataset dataset = datasetService.read(dt.getAccession(), dt.getDatabase());
                    int downloadCurrValue = dataset.getAdditional().containsKey(Constants.DOWNLOAD_COUNT)
                            ? Integer.valueOf(dataset.getAdditional().get(Constants.DOWNLOAD_COUNT).iterator().next())
                            : 0;

                    Set<String> downloadCount = new HashSet<String>();

                    if (prideDownloads != null) {
                        int count = prideDownloads.entrySet().stream().mapToInt(dst ->
                                dst.getValue().entrySet().stream().mapToInt(
                                        dtr -> dtr.getValue().elementSet().stream().mapToInt(
                                                dtrc -> dtr.getValue().count(dtrc)
                                        ).sum()
                                ).sum()
                        ).sum();

                        downloadCount.add(String.valueOf(count + downloadCurrValue));
                        dataset.getAdditional().put(Constants.DOWNLOAD_COUNT, downloadCount);
                        datasetService.save(dataset);
                    }
                }
                return true;
            });
        }
    }
}
