package uk.ac.ebi.ddi.similarityCalculator;

import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ddi.annotation.service.synonyms.DDIXmlProcessService;
import uk.ac.ebi.ddi.annotation.utils.Constants;
import uk.ac.ebi.ddi.ebe.ws.dao.client.dataset.DatasetWsClient;
import uk.ac.ebi.ddi.ebe.ws.dao.client.europmc.CitationClient;
import uk.ac.ebi.ddi.ebe.ws.dao.model.common.QueryResult;
import uk.ac.ebi.ddi.ebe.ws.dao.model.europmc.Citation;
import uk.ac.ebi.ddi.ebe.ws.dao.model.europmc.CitationResponse;
import uk.ac.ebi.ddi.service.db.model.dataset.*;
import uk.ac.ebi.ddi.service.db.model.similarity.Citations;
import uk.ac.ebi.ddi.service.db.model.similarity.EBISearchPubmedCount;
import uk.ac.ebi.ddi.service.db.model.similarity.ReanalysisData;
import uk.ac.ebi.ddi.service.db.service.dataset.IDatasetService;
import uk.ac.ebi.ddi.service.db.service.dataset.IDatasetSimilarsService;
import uk.ac.ebi.ddi.service.db.service.similarity.*;
import uk.ac.ebi.ddi.service.db.utils.DatasetSimilarsType;

import javax.xml.crypto.Data;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by gaur on 13/07/17.
 */
public class SimilarityCounts {

    Integer startDataset = 0;

    Integer numberOfDataset = 100;

    @Autowired
    CitationClient citationClient;

    @Autowired
    DatasetWsClient datasetWsClient;

    @Autowired
    ICitationService citationService;

    @Autowired
    IDatasetService datasetService;

    @Autowired
    IDatasetStatInfoService datasetStatInfoService;

    @Autowired
    IReanalysisDataService reanalysisDataService;

    @Autowired
    IEBIPubmedSearchService ebiPubmedSearchService;

    @Autowired
    IDatasetSimilarsService similarsService;

    private static final Logger logger = LoggerFactory.getLogger(SimilarityCounts.class);

    public void getCitationCount(String database,String accession){
        try {
            CitationResponse citationResponse = citationClient.getCitations(accession);
            Dataset dataset = datasetService.read(accession, database);
            Set<String> cit = new HashSet<String>();
            cit = Arrays.stream(citationResponse.citations.get("result")).
                    filter(data -> (dataset.getCrossReferences() != null && dataset.getCrossReferences().get(Constants.PUBMED_FIELD) != null && !dataset.getCrossReferences().get(Constants.PUBMED_FIELD).contains(data.pubmedId)))
                    .map(dt -> dt.pubmedId).collect(Collectors.toSet());
            Citations citations = new Citations();
            citations.setAccession(accession);
            citations.setDatabase(database);
            citations.setPubmedId(cit);
            citations.setPubmedCount(cit.size());
            citationService.saveCitation(citations);

            if(dataset.getScores() != null) {
                dataset.getScores().setCitationCount(cit.size());

            }else{
                Scores scores = new Scores();
                scores.setCitationCount(cit.size());
                dataset.setScores(scores);
                HashSet<String> count = new HashSet<String>();
                count.add(String.valueOf(String.valueOf(cit.size())));
                dataset.getAdditional().put(Constants.CITATION_FIELD ,count);
            }
            datasetService.update(dataset.getId(),dataset);

        }
        catch(Exception ex){
            //System.out.println(ex.getMessage());
            logger.error("inside getcitationcount exception is " + ex.getMessage());
        }
    }

    public void addAllCitations(){
        //try {
            for (int i = startDataset; i < datasetService.getDatasetCount()/numberOfDataset; i = i + 1) {
                //System.out.println("value of i is" + i);
                datasetService.readAll(i, numberOfDataset).getContent()
                        .forEach(dt -> getCitationCount(dt.getDatabase(), dt.getAccession()));
                //Thread.sleep(3000);
            }
        /*}
        catch(Exception ex){
            logger.error("error inside add all citations " + ex.getMessage());
        }*/
    }

    public void addSearchCounts(String accession,String pubmedId,String database){
        try {
            int size = 20;
            String query = pubmedId;
            query = (query == null || query.isEmpty() || query.length() == 0) ? "*:*" : query;

            QueryResult queryResult = datasetWsClient.getDatasets(Constants.MAIN_DOMAIN, query,
                    Constants.DATASET_SUMMARY, Constants.PUB_DATE_FIELD, "descending", 0, size, 10);

            queryResult.getCount();

            Set<String> matchDataset = Arrays.stream(queryResult.getEntries()).filter(dt -> !dt.getId().toString().equals(accession)).map(dts -> dts.getId().toString()).collect(Collectors.toSet());

            EBISearchPubmedCount ebiSearchPubmedCount = new EBISearchPubmedCount();
            ebiSearchPubmedCount.setAccession(accession);
            ebiSearchPubmedCount.setPubmedCount(matchDataset.size());
            Map<String, Set<String>> pubmedDatasets = new HashMap<String, Set<String>>();
            pubmedDatasets.put(pubmedId, matchDataset);
            ebiSearchPubmedCount.setPubmedDatasetList(pubmedDatasets);
            ebiPubmedSearchService.saveEbiSearchPubmed(ebiSearchPubmedCount);
            Dataset dataset = datasetService.read(accession,database);
            if (dataset != null) {

                if(dataset.getScores() != null) {
                    dataset.getScores().setSearchCount(matchDataset.size());
                }else{
                    Scores scores = new Scores();
                    scores.setSearchCount(matchDataset.size());
                    dataset.setScores(scores);
                    HashSet<String> count = new HashSet<String>();
                    count.add(String.valueOf(matchDataset.size()));
                    dataset.getAdditional().put(Constants.SEARCH_FIELD,count);
                }
                datasetService.update(dataset.getId(),dataset);
            }


        }
        catch(Exception ex){
            logger.error("inside add Search Counts exception is " + ex.getMessage() + " query is " + pubmedId + " dataset is  " + accession);
        }
    }

    public void saveReanalysisCount(){
        List<ReanalysisData> reanalysisData = datasetStatInfoService.reanalysisCount();

        reanalysisData.parallelStream().forEach(dt -> reanalysisDataService.saveReanalysis(dt));
    }

    public void saveSearchcounts(){
        try {
            for (int i = startDataset; i < datasetService.getDatasetCount()/numberOfDataset; i = i + 1) {
                //System.out.println("value of i is" + i);
                datasetService.readAll(i, numberOfDataset).getContent().stream().filter(data ->
                        data.getCrossReferences() != null && data.getCrossReferences().get(Constants.PUBMED_FIELD) != null)
                        .forEach(dt ->  dt.getCrossReferences().get(Constants.PUBMED_FIELD).
                                forEach(dta -> addSearchCounts(dt.getAccession(),dta,dt.getDatabase())));
                //Thread.sleep(3000);
            }
        }
        catch(Exception ex){
            logger.error("error inside savesearch count exception message is " + ex.getMessage());
        }
    }

    public void getPageRecords(){
        for (int i = startDataset; i < datasetService.getDatasetCount()/numberOfDataset; i = i + 1) {
            //System.out.println("value of i is" + i);
            datasetService.readAll(i, numberOfDataset).getContent().stream().
                    forEach(dt -> System.out.print(dt.getAccession()));
        }
    }

    public void renalyseBioModels(){
        List<Dataset> datasets = datasetService.findByDatabaseBioModels(Constants.BIOMODELS_DATABASE);
        datasets.parallelStream().forEach(data -> addSimilarDataset(data.getAccession(),data.getDatabase(),data.getCrossReferences().get("biomodels__db")));
    }

    public void addSimilarDataset(String accession,String database,Set<String> similarAccession){
        DatasetSimilars datasetSimilars = new DatasetSimilars();
        datasetSimilars.setAccession(accession);
        datasetSimilars.setDatabase(database);
        Set<SimilarDataset> similarDatasets = new HashSet<SimilarDataset>();
        for(String similarAcc : similarAccession) {
            List<Dataset> dataset = datasetService.findByAccession(similarAccession.iterator().next());
            if (dataset.size() > 0) {
                for (Dataset data : dataset) {
                    SimilarDataset similar = new SimilarDataset(data, DatasetSimilarsType.REANALYSIS_OF.getType());
                    similarDatasets.add(similar);
                }
            }

        }
        datasetSimilars.setSimilars(similarDatasets);
        similarsService.save(datasetSimilars);
    }

    public void renalysedByBioModels(){
        List<Dataset> datasets = datasetService.findByDatabaseBioModels(Constants.BIOMODELS_DATABASE);
        datasets.parallelStream().forEach(data -> addSimilarDataset(data.getAccession(),data.getDatabase(),data.getCrossReferences().get("biomodels__db")));
    }

}
