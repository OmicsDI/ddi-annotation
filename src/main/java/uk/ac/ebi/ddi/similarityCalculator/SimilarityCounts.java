package uk.ac.ebi.ddi.similarityCalculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ddi.annotation.utils.Constants;
import uk.ac.ebi.ddi.ebe.ws.dao.client.dataset.DatasetWsClient;
import uk.ac.ebi.ddi.ebe.ws.dao.client.domain.DomainWsClient;
import uk.ac.ebi.ddi.ebe.ws.dao.client.europmc.CitationClient;
import uk.ac.ebi.ddi.ebe.ws.dao.model.common.Entry;
import uk.ac.ebi.ddi.ebe.ws.dao.model.common.QueryResult;
import uk.ac.ebi.ddi.ebe.ws.dao.model.domain.Domain;
import uk.ac.ebi.ddi.ebe.ws.dao.model.domain.DomainList;
import uk.ac.ebi.ddi.ebe.ws.dao.model.europmc.CitationResponse;
import uk.ac.ebi.ddi.service.db.model.dataset.*;
import uk.ac.ebi.ddi.service.db.model.similarity.Citations;
import uk.ac.ebi.ddi.service.db.model.similarity.EBISearchPubmedCount;
import uk.ac.ebi.ddi.service.db.model.similarity.ReanalysisData;
import uk.ac.ebi.ddi.service.db.service.dataset.DatasetCountService;
import uk.ac.ebi.ddi.service.db.service.dataset.IDatasetService;
import uk.ac.ebi.ddi.service.db.service.dataset.IDatasetSimilarsService;
import uk.ac.ebi.ddi.service.db.service.similarity.*;
import uk.ac.ebi.ddi.service.db.utils.DatasetSimilarsType;
import uk.ac.ebi.ddi.similarityCalculator.utils.SimilarityConstants;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by gaur on 13/07/17.
 */
@Service
public class SimilarityCounts {

    Integer startDataset = 0;

    Integer numberOfDataset = 2000;

    Integer numberOfCitations = 500;

    @Autowired
    CitationClient citationClient;

    @Autowired
    DatasetWsClient datasetWsClient;

    @Autowired
    ICitationService citationService;

    @Autowired
    IDatasetService datasetService;

    @Autowired
    DatasetCountService datasetCountService;

    @Autowired
    IDatasetStatInfoService datasetStatInfoService;

    @Autowired
    IReanalysisDataService reanalysisDataService;

    @Autowired
    IEBIPubmedSearchService ebiPubmedSearchService;

    @Autowired
    IDatasetSimilarsService similarsService;

    @Autowired
    DomainWsClient domainWsClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(SimilarityCounts.class);

    public void getCitationCount(String database, String accession, List<String> secondaryAccession) {
        try {
           /* List<CitationResponse> citations = new ArrayList<>();
            CitationResponse citationResponse = citationClient.getCitations(accession,numberOfCitations);
            citations.add(citationResponse);
            if(citationResponse.count > numberOfCitations){
                while(citationResponse.count % numberOfCitations > 0){
                    CitationResponse allCitations = citationClient.getCitations(accession,numberOfCitations);
                    citations.add(allCitations);
                }
            }
            //CitationResponse secondaryCitations = new CitationResponse();
            if(!secondaryAccession.isEmpty()) {

                secondaryAccession.stream().forEach(acc ->{
                    CitationResponse secondaryCitations = citationClient.getCitations(acc,);
                    Dataset dataset = datasetService.read(accession, database);
                    dataset = addCitationData(dataset, citationResponse, secondaryCitations);
                    datasetService.update(dataset.getId(),dataset);}
                );
                return;
            }*/
            final Dataset dataset = datasetService.read(accession, database);
            Set<String> primaryCitationIds = getCitationsSet(accession, dataset);
            if (!secondaryAccession.isEmpty()) {
                secondaryAccession.forEach(acc -> {
/*                    CitationResponse secondaryCitations = citationClient.getCitations(acc,);
                    Dataset dataset = datasetService.read(accession, database);
                    dataset = addCitationData(dataset, citationResponse, secondaryCitations);
                    datasetService.update(dataset.getId(),dataset);}*/
                    Set<String> secondaryCitationIds = getCitationsSet(acc, dataset);
                    primaryCitationIds.addAll(secondaryCitationIds);
                });
                //return;
            }
            addCitationData(dataset, primaryCitationIds);
            //datasetService.update(dataset.getId(),dataset);
        } catch (Exception ex) {
            LOGGER.error("Exception occurred when getting dataset {},  ", accession, ex);
        }
    }

    public Dataset addCitationData(Dataset dataset, Set<String> allCitationIds) {

        Dataset updateDataset = datasetService.read(dataset.getAccession(), dataset.getDatabase());
        Citations citations = new Citations();
        citations.setAccession(dataset.getAccession());
        citations.setDatabase(dataset.getDatabase());
        citations.setPubmedId(allCitationIds);
        citations.setPubmedCount(allCitationIds.size());
        citationService.saveCitation(citations);

        if (dataset.getScores() != null) {
            updateDataset.getScores().setCitationCount(allCitationIds.size());
        } else {
            Scores scores = new Scores();
            scores.setCitationCount(allCitationIds.size());
            updateDataset.setScores(scores);

        }
        HashSet<String> count = new HashSet<>();
        count.add(String.valueOf(allCitationIds.size()));
        updateDataset.getAdditional().put(Constants.CITATION_FIELD, count);
        datasetService.update(updateDataset.getId(), updateDataset);
        return dataset;
    }

    public void addAllCitations() {
        try {
            for (int i = startDataset; i < datasetService.getDatasetCount() / numberOfDataset; i = i + 1) {
                LOGGER.info("value of i is" + i);
                datasetService.readAll(i, numberOfDataset).getContent()
                        .forEach(dt -> getCitationCount(
                                dt.getDatabase(), dt.getAccession(),
                                dt.getAdditional().containsKey(Constants.SECONDARY_ACCESSION) ?
                                        new ArrayList<String>(dt.getAdditional().get(Constants.SECONDARY_ACCESSION)) :
                                        new ArrayList<String>()));
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occurred, ", ex);
        }
    }

    public void addSearchCounts(String accession, String pubmedId, String database) {
        LOGGER.info("inside add search counts ");
        int size = 20;
        int searchCount;
        try {
            HashMap<String, Integer> domainMap = new HashMap<>();
            Dataset dataset = datasetService.read(accession, database);
            List<String> filteredDomains = new ArrayList<>();
            Set<String> secondaryAccession = dataset.getAdditional().get(Constants.SECONDARY_ACCESSION);
            String query = pubmedId;
            query = (query == null || query.isEmpty()) ? "*:*" : query;

            QueryResult queryResult = null;

            DomainList domainList = domainWsClient.getDomainByName(SimilarityConstants.OMICS_DOMAIN);

            List<String> domains = Arrays.stream(domainList.list).map(Domain::getId).collect(Collectors.toList());

            domains.add(SimilarityConstants.ATLAS_GENES);
            domains.add(SimilarityConstants.ATLAS_GENES_DIFFERENTIAL);
            domains.add("metabolights");

            if (!pubmedId.equals("") && !pubmedId.equals("none") && !pubmedId.equals("0")) {
                query = "PUBMED:" + query + " OR MEDLINE:" + query + " OR PMID:" + query;
                queryResult = datasetWsClient.getDatasets(Constants.ALL_DOMAIN, query,
                        Constants.DATASET_SUMMARY, Constants.PUB_DATE_FIELD, "descending", 0, size, 10);
            }

//            int leftCount =  queryResult!=null ? queryResult.getDomains().stream().flatMap(
//                    dtl -> Arrays.stream(dtl.getSubdomains())).
//                    map(dtl -> Arrays.stream(dtl.getSubdomains())).
//                    flatMap(sbdt ->sbdt.filter(dt -> !domains.contains(dt.getId()))).mapToInt(dtf ->{
//                //filteredDomains.add(dtf.getId() + "~" +dtf.getHitCount());
//                return dtf.getHitCount();}).sum():0;

            QueryResult queryAccessionResult = datasetWsClient.getDatasets(Constants.ALL_DOMAIN, accession,
                    Constants.DATASET_SUMMARY, Constants.PUB_DATE_FIELD, "descending", 0, size, 10);


            searchCount = queryResult != null && queryResult.getCount() > 0 ? queryResult.getDomains()
                    .stream()
                    .flatMap(dtl -> Arrays.stream(dtl.getSubdomains()))
                    .map(dtl -> Arrays.stream(dtl.getSubdomains()))
                    .flatMap(sbdt -> sbdt.filter(dt -> !domains.contains(dt.getId())))
                    .mapToInt(dtf -> {
                        updateKeyValue(dtf.getId().toLowerCase(), dtf.getHitCount(), domainMap);
                        //filteredDomains.add(dtf.getId() + "~" +dtf.getHitCount());
                        return dtf.getHitCount(); })
                    .sum() : 0;

            if (queryAccessionResult != null && queryAccessionResult.getCount() > 0) {
                searchCount = searchCount + queryAccessionResult.getDomains()
                        .parallelStream()
                        .flatMap(dtl -> Arrays.stream(dtl.getSubdomains()))
                        .map(dtl -> Arrays.stream(dtl.getSubdomains()))
                        .flatMap(sbdt -> sbdt.filter(dt -> !domains.contains(dt.getId())))
                        .mapToInt(dtf -> {
                            updateKeyValue(dtf.getId().toLowerCase(), dtf.getHitCount(), domainMap);
                            //filteredDomains.add(dtf.getId() + "~" +dtf.getHitCount());
                            return dtf.getHitCount(); })
                        .sum();
            }

            int allCounts = secondaryAccession != null ? secondaryAccession.parallelStream().mapToInt(dt -> {
                QueryResult querySecondaryResult = datasetWsClient.getDatasets(Constants.ALL_DOMAIN, dt,
                        Constants.DATASET_SUMMARY, Constants.PUB_DATE_FIELD, "descending", 0, size, 10);
                return querySecondaryResult.getDomains()
                        .stream()
                        .flatMap(dtl -> Arrays.stream(dtl.getSubdomains()))
                        .map(dtl -> Arrays.stream(dtl.getSubdomains()))
                        .flatMap(sbdt -> sbdt.filter(dtls -> !domains.contains(dtls.getId())))
                        .mapToInt(dtf -> {
                            updateKeyValue(dtf.getId().toLowerCase(), dtf.getHitCount(), domainMap);
                            //filteredDomains.add(dtf.getId() + "~" +dtf.getHitCount());
                            return dtf.getHitCount(); })
                        .sum();
                //return querySecondaryResult.getCount();
            }).sum() : 0;

            searchCount = searchCount + allCounts;
            //queryResult.getCount();

            Set<String> matchDataset = new HashSet<>();
            if (queryResult != null && queryResult.getEntries() != null) {
                matchDataset = Arrays.stream(queryResult.getEntries())
                        .filter(dt -> !dt.getId().equals(accession))
                        .map(Entry::getId).collect(Collectors.toSet());
            }

            if (dataset.getCrossReferences() != null) {
                Collection<Set<String>> crossReferences = dataset.getCrossReferences().values();
                searchCount = searchCount + crossReferences.stream().mapToInt(Set::size).sum();
                dataset.getCrossReferences().keySet().forEach(dt -> {
                    updateKeyValue(dt.toLowerCase(), dataset.getCrossReferences().get(dt).size(), domainMap);
                });

            }

            Set<String> domainSet =  domainMap.entrySet().parallelStream()
                    .map(dt -> dt.getKey() + "~" + dt.getValue()).collect(Collectors.toSet());
            EBISearchPubmedCount ebiSearchPubmedCount = new EBISearchPubmedCount();
            ebiSearchPubmedCount.setAccession(accession);
            ebiSearchPubmedCount.setPubmedCount(searchCount);
            Map<String, Set<String>> pubmedDatasets = new HashMap<String, Set<String>>();
            pubmedDatasets.put(pubmedId, matchDataset);
            ebiSearchPubmedCount.setPubmedDatasetList(pubmedDatasets);
            ebiPubmedSearchService.saveEbiSearchPubmed(ebiSearchPubmedCount);
            //Dataset dataset = datasetService.read(accession,database);
            if (dataset.getScores() != null) {
                dataset.getScores().setSearchCount(searchCount);
            } else {
                Scores scores = new Scores();
                scores.setSearchCount(searchCount);
                dataset.setScores(scores);
            }
            HashSet<String> count = new HashSet<>();
            count.add(String.valueOf(searchCount));
            dataset.getAdditional().put(Constants.SEARCH_FIELD, count);
            dataset.getAdditional().put(Constants.SEARCH_DOMAIN, domainSet);
            datasetService.update(dataset.getId(), dataset);


        } catch (Exception ex) {
            LOGGER.error("Exception occurred, query is " + pubmedId + " dataset is  " + accession + ", ", ex);
        }
    }

    public Map<String, Integer> updateKeyValue(String key, Integer value, Map<String, Integer> domainMap) {
        if (domainMap.containsKey(key)) {
            Integer updatedValue = domainMap.get(key);
            updatedValue = updatedValue + value;
            domainMap.put(key, updatedValue);
        } else {
            domainMap.put(key, value);
        }
        return domainMap;
    }

    public void saveReanalysisCount() {
        List<ReanalysisData> reanalysisData = datasetStatInfoService.reanalysisCount();
        reanalysisData.parallelStream().forEach(dt -> reanalysisDataService.saveReanalysis(dt));
    }

    public void saveSearchcounts() {
        try {
            for (int i = startDataset; i < datasetService.getDatasetCount() / numberOfDataset; i = i + 1) {
                datasetService.readAll(i, numberOfDataset).getContent()
                        .parallelStream()
                        .map(data -> {
                            if (data.getCrossReferences() != null
                                    && data.getCrossReferences().get(Constants.PUBMED_FIELD) != null) {
                                data.getCrossReferences().get(Constants.PUBMED_FIELD)
                                        .forEach(dta -> addSearchCounts(data.getAccession(), dta, data.getDatabase()));
                            } else {
                                 addSearchCounts(data.getAccession(), "", data.getDatabase());
                            }
                            return "";
                        }).count();

//                datasetService.readAll(i, numberOfDataset).getContent().parallelStream()
//                        .filter(data -> data.getCrossReferences() != null
//                                && data.getCrossReferences().get(Constants.PUBMED_FIELD) != null)
//                        .forEach(dt ->  dt.getCrossReferences().get(Constants.PUBMED_FIELD)
//                                .forEach(dta -> addSearchCounts(dt.getAccession(), dta, dt.getDatabase())));
                //Thread.sleep(3000);
            }
        } catch (Exception ex) {
            LOGGER.error("error inside savesearch count exception message is " + ex.getMessage());
        }
    }

    public void saveLeftSearchcounts() {
        try {
            long count = 0;
            int[] iarr = {0};
            int pages = datasetService.getWithoutSearchDomains(0, numberOfDataset).getTotalPages();
            for (int i = startDataset; i < pages; i = i + 1) {
                    System.out.println("page number is " + i);
                 datasetService.getWithoutSearchDomains(i, numberOfDataset).getContent().parallelStream()
                        .map(data -> {
                    if (data.getCrossReferences() != null
                            && data.getCrossReferences().get(Constants.PUBMED_FIELD) != null) {
                        data.getCrossReferences().get(Constants.PUBMED_FIELD).
                                forEach(dta -> addSearchCounts(data.getAccession(), dta, data.getDatabase()));

                    } else {
                        addSearchCounts(data.getAccession(), "", data.getDatabase());
                    }
                    return "";
                }).count();
                //Thread.sleep(3000);
            }
            System.out.println(count);
        } catch (Exception ex) {
            LOGGER.error("error inside savesearch count exception message is " + ex.getMessage());
        }
    }
    public void getPageRecords() {
        for (int i = startDataset; i < datasetService.getDatasetCount() / numberOfDataset; i = i + 1) {
            //System.out.println("value of i is" + i);
            datasetService.readAll(i, numberOfDataset).getContent().stream().
                    forEach(dt -> System.out.print(dt.getAccession()));
        }
    }

    public void renalyseBioModels() {
        List<Dataset> datasets = datasetService.findByDatabaseBioModels(Constants.BIOMODELS_DATABASE);
        datasets.parallelStream().forEach(data -> addSimilarDataset(
                data.getAccession(),
                data.getDatabase(),
                data.getCrossReferences().get("biomodels__db")));
    }

    public void addSimilarDataset(String accession, String database, Set<String> similarAccession) {
        DatasetSimilars datasetSimilars = new DatasetSimilars();
        datasetSimilars.setAccession(accession);
        datasetSimilars.setDatabase(database);
        Set<SimilarDataset> similarDatasets = new HashSet<SimilarDataset>();
        for (String similarAcc : similarAccession) {
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

    public void renalysedByBioModels() {
        List<Dataset> datasets = datasetService.findByDatabaseBioModels(Constants.BIOMODELS_DATABASE);
        datasets.parallelStream().forEach(data -> addSimilarDataset(
                data.getAccession(), data.getDatabase(), data.getCrossReferences().get("biomodels__db")));
    }

    public Set<String> getCitationsSet(String accession, Dataset dataset) {
        List<CitationResponse> citations = new ArrayList<>();
        Set<String> primaryCit = new HashSet<String>();
        int numberOfPages = 0;
        CitationResponse primaryCitation = citationClient.getCitations(accession, numberOfCitations, "*");
        primaryCit.addAll(Arrays.stream(primaryCitation.citations.get("result"))
                .filter(data -> (dataset.getCrossReferences() != null
                        && dataset.getCrossReferences().get(Constants.PUBMED_FIELD) != null
                        && !dataset.getCrossReferences().get(Constants.PUBMED_FIELD).contains(data.pubmedId)))
                .map(dt -> dt.pubmedId).collect(Collectors.toSet()));

        if (primaryCitation.count > numberOfCitations) {
            while (primaryCitation.count / numberOfCitations - numberOfPages > 0) {
                primaryCitation = citationClient.getCitations(accession, numberOfCitations, primaryCitation.cursorMark);
                primaryCit.addAll(Arrays.stream(primaryCitation.citations.get("result"))
                        .filter(data -> (dataset.getCrossReferences() != null
                                && dataset.getCrossReferences().get(Constants.PUBMED_FIELD) != null
                                && !dataset.getCrossReferences().get(Constants.PUBMED_FIELD).contains(data.pubmedId)))
                        .map(dt -> dt.pubmedId).collect(Collectors.toSet()));
                numberOfPages++;
            }
        }
        return primaryCit;

    }

    public void addReanalysisKeyword() {
        reanalysisDataService.updateReanalysisKeywords();
    }
}
