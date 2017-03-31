package uk.ac.ebi.ddi.annotation.service.crossreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ddi.service.db.model.similarity.*;
import uk.ac.ebi.ddi.service.db.service.similarity.DatasetStatInfoService;
import uk.ac.ebi.ddi.service.db.service.similarity.ExpOutputDatasetService;
import uk.ac.ebi.ddi.service.db.service.similarity.TermInDBService;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 *  ==Overview==
 *
 *  This class
 *
 * Created by ypriverol (ypriverol@gmail.com) on 11/09/15.
 */
public class DDIDatasetSimilarityService {

    private static final Logger logger = LoggerFactory.getLogger(DDIDatasetSimilarityService.class);

    @Autowired
    TermInDBService termInDBService = new TermInDBService();
    @Autowired
    ExpOutputDatasetService expOutputDatasetService = new ExpOutputDatasetService();
    @Autowired
    DatasetStatInfoService datasetStatInfoService= new DatasetStatInfoService();

    private double[][] cosineScores = null;

    private List<ExpOutputDataset> expOutputDatasets = null;

    private HashMap<String, Integer> indecies = null;

    private List<TermInDB> termsInDB;

    private final HashMap<String, Double> idfWeightMap;

    private String dataType;


    /**
     * Default Constructor
     */
    public DDIDatasetSimilarityService() {
        this.idfWeightMap = new HashMap<>();
    }


    /**
     * Calculate the inverse dataset/document frequency weight of each term = log(N/DatasetFrequency)
     *
     * @param dataType Omics type of the dataset
     */
    public void calculateIDFWeight(String dataType) {

        this.dataType = dataType;
        this.termsInDB = termInDBService.readAllInOneType(dataType);
        long numberOfDatasets = this.termsInDB.parallelStream().collect(Collectors.groupingBy(TermInDB::getAccession)).size();
        Map<String, List<TermInDB>> termsMap = this.termsInDB.parallelStream().collect(Collectors.groupingBy(TermInDB::getTermName));

        Set <String> keys =  termsMap.keySet();
        keys.parallelStream().forEach((key) -> {
            double tempscore = (double) numberOfDatasets / (double) termsMap.get(key).size();
            double idfWeigt = Math.log(tempscore) / Math.log(2);
            this.idfWeightMap.put(key, idfWeigt);
        });

//        for (String key : termsMap.keySet()) {
//            double tempscore = (double) numberOfDatasets / (double) termsMap.get(key).size();
//            double idfWeigt = Math.log(tempscore) / Math.log(2);
//            this.idfWeightMap.put(key, idfWeigt);
//        }
        logger.info("End of calculating IDFWeight for" + dataType);
    }

       /**
     * Calculate the inverse dataset/document frequency weight of each term = log(N/DatasetFrequency)
     *
     * @param dataType Omics type of the dataset
     */
    public void calculateIDFWeight_new(String dataType) {

        this.dataType = dataType;
        this.termsInDB = termInDBService.readAllUncalculatedTermsInOneType(dataType);
        long numberOfDatasets = this.termsInDB.parallelStream().collect(Collectors.groupingBy(TermInDB::getAccession)).size();
        Map<String, List<TermInDB>> termsMap = this.termsInDB.parallelStream().collect(Collectors.groupingBy(TermInDB::getTermName));

        Set <String> keys =  termsMap.keySet();
        keys.parallelStream().forEach((key) -> {
            double tempscore = (double) numberOfDatasets / (double) termsMap.get(key).size();
            double idfWeigt = Math.log(tempscore) / Math.log(2);
            this.idfWeightMap.put(key, idfWeigt);
        });

//        for (String key : termsMap.keySet()) {
//            double tempscore = (double) numberOfDatasets / (double) termsMap.get(key).size();
//            double idfWeigt = Math.log(tempscore) / Math.log(2);
//            this.idfWeightMap.put(key, idfWeigt);
//        }
        logger.info("End of calculating IDFWeight for" + dataType);
    }



    /**
     * Calculate the intersection/(terms sharing) information between datasets
     *
     * @param dataType omics type of the dataset
     */
    public void calculateSimilarity(String dataType) {

        if (!dataType.equals(this.dataType)) {
            throw new IllegalStateException("The dataType for calculate Intersections is" + dataType + ", not same as prev calculated dataType" + this.dataType);
        }

        logger.info("start to calculate similarity for" + dataType);

        this.expOutputDatasets = this.expOutputDatasetService.readAllInOneType(dataType);

        long numberOfDatasets = expOutputDatasets.size();

        logger.info("The number of Datasets for calculate Intersections:" + numberOfDatasets);

        int i = 0;
        this.indecies = new HashMap<>();
        for (ExpOutputDataset dataset : expOutputDatasets) {
            this.indecies.put(dataset.getAccession(), i++);
        }

        this.cosineScores = calculateCosineScore(expOutputDatasets, numberOfDatasets);

        expOutputDatasets.stream().forEach(dataset -> {
            List<IntersectionInfo> datasetIntersectionInfos = new CopyOnWriteArrayList<>();
            Set<String> terms = dataset.getTerms();
            List<IntersectionInfo> finalDatasetIntersectionInfos = datasetIntersectionInfos;
            terms.parallelStream().forEach(term -> {
                List<ExpOutputDataset> relatedDatasets = getRelatedDatasets(dataset, term, expOutputDatasets);
                List<IntersectionInfo> tempIntersectionInfos = getIntersectionInfos(dataset, relatedDatasets);
                finalDatasetIntersectionInfos.addAll(tempIntersectionInfos);
            });
            datasetIntersectionInfos = mergeIntersectionInfos(finalDatasetIntersectionInfos);
            DatasetStatInfo datasetStatInfo = new DatasetStatInfo(dataset.getAccession(), dataset.getDatabase(), dataType, datasetIntersectionInfos);
            datasetStatInfoService.insert(datasetStatInfo);

        });
        logger.info("End of calculating similarity for" + dataType);
    }

    /**
     * If one dataset share multiple terms with another dataset, merge the multiple Intersection Informations into one and set
     * the right SharedTermNo
     *
     * @param datasetIntersectionInfos The dataset Interseptions
     * @return a List of intersections
     */
    private List<IntersectionInfo> mergeIntersectionInfos(List<IntersectionInfo> datasetIntersectionInfos) {
        List<IntersectionInfo> newIntersectionInfos = new ArrayList<>();
        for (IntersectionInfo tempIntersectionInfo : datasetIntersectionInfos) {
            if(tempIntersectionInfo != null){
//                System.out.println(tempIntersectionInfo.toString());
                int i = contains(newIntersectionInfos, tempIntersectionInfo);
                if (i >= 0) {
                    newIntersectionInfos.get(i).increaseOneSharedTermsNo();
                } else {
                    tempIntersectionInfo.setSharedTermsNo(1);
                    newIntersectionInfos.add(tempIntersectionInfo);
                }
            }
        }
        return newIntersectionInfos;
    }

    /**
     * Locate the index of intersectionInfo in the new merged intersectionInfo list
     *
     * @param datasetIntersectionInfos the dataset list to compute the interception
     * @param tempIntersectionInfo a temporary interception information
     * @return index of the word in a given list
     */
    private int contains(List<IntersectionInfo> datasetIntersectionInfos, IntersectionInfo tempIntersectionInfo) {
        for (int i = 0; i < datasetIntersectionInfos.size(); i++) {
            if(datasetIntersectionInfos.get(i) != null
                    && datasetIntersectionInfos.get(i).getRelatedDatasetAcc() != null
                    && !datasetIntersectionInfos.get(i).getRelatedDatasetAcc().isEmpty()
                    && tempIntersectionInfo != null && tempIntersectionInfo.getRelatedDatasetAcc() != null
                    && datasetIntersectionInfos.get(i).getRelatedDatasetAcc().equals(tempIntersectionInfo.getRelatedDatasetAcc()))
                return i;

        }
        return -1;
    }

    /**
     * Get intersectionInfos from related datasets, which linked by term
     *
     * @param dataset the dataset
     * @param relatedDatasets related datasets
     * @return
     */
    private List<IntersectionInfo> getIntersectionInfos(ExpOutputDataset dataset, List<ExpOutputDataset> relatedDatasets) {
        List<IntersectionInfo> intersectionInfos = new ArrayList<>();
        int indexOfThisDataset = this.indecies.get(dataset.getAccession());
        relatedDatasets.stream().forEach(relateddataset -> {
            int indexOfThatDataset = this.indecies.get(relateddataset.getAccession());
            IntersectionInfo intersectionInfo = new IntersectionInfo();
            intersectionInfo.setRelatedDatasetAcc(relateddataset.getAccession());
            intersectionInfo.setRelatedDatasetDatabase(relateddataset.getDatabase());
            intersectionInfo.setCosineScore(this.cosineScores[indexOfThisDataset][indexOfThatDataset]);
            intersectionInfos.add(intersectionInfo);
        });
        return intersectionInfos;
    }

    /**
     * calculate cosine scores between experiment output datasets
     *
     * @param expOutputDatasets
     * @param numberOfDatasets  total number of datasets(in one omics type)
     * @return score array
     */
    private double[][] calculateCosineScore(List<ExpOutputDataset> expOutputDatasets, long numberOfDatasets) {

        double[][] cosineScores = new double[(int) numberOfDatasets][(int) numberOfDatasets];
        HashMap<String, Double> normMap = CalculateNormArray();

        expOutputDatasets.parallelStream().forEach( dataset -> {
            int indexOfThisDataset = this.indecies.get(dataset.getAccession());
            Set<String> termsOfThisDataset = dataset.getTerms();
            expOutputDatasets.parallelStream().forEach(dataset2 -> {
                List<String> intersectionTerms;
                int indexOfThatDataset = this.indecies.get(dataset2.getAccession());
                Set<String> termsOfThatDataset = dataset2.getTerms();
                if (termsOfThisDataset == termsOfThatDataset) { //same dataset
                    cosineScores[indexOfThisDataset][indexOfThatDataset] = -1;
                } else if (cosineScores[indexOfThisDataset][indexOfThatDataset] != 0) {//already calculated
                } else {
                    intersectionTerms = getIntersectionSet(termsOfThisDataset, termsOfThatDataset);
                    if (intersectionTerms.size() == 0) {
                        cosineScores[indexOfThisDataset][indexOfThatDataset] = -1;
                        cosineScores[indexOfThatDataset][indexOfThisDataset] = -1;
                    }else {
                        double score = 0;
                        for (String termInList : intersectionTerms) {
                            score += Math.pow(idfWeightMap.get(termInList), 2); //each term has same score in both vector(dataset)
                        }
                        double scoreFinal = score / (normMap.get(dataset.getAccession()) * normMap.get(dataset2.getAccession()));
                        cosineScores[indexOfThisDataset][indexOfThatDataset] = scoreFinal;
                        cosineScores[indexOfThatDataset][indexOfThisDataset] = scoreFinal;
                    }
                }
            });
        });

        return cosineScores;
    }

    private HashMap<String, Double> CalculateNormArray() {
        HashMap<String, Double> normMap = new HashMap<>();

        for (ExpOutputDataset dataset : expOutputDatasets) {
            double norm = 0;
            for (String term : dataset.getTerms()) {
                double weight = idfWeightMap.get(term);
                norm += Math.pow(weight, 2);
            }
            double normFinal = Math.sqrt(norm);
            normMap.put(dataset.getAccession(), normFinal);
        }
        return normMap;
    }

    private List<ExpOutputDataset> getRelatedDatasets(ExpOutputDataset originDataset, String term, List<ExpOutputDataset> expOutputDatasets) {
        List<ExpOutputDataset> relatedDatasets = new ArrayList<>();
        for (ExpOutputDataset possibleDataset : expOutputDatasets) {
            if (possibleDataset.getAccession().equalsIgnoreCase(originDataset.getAccession())) {
                continue;
            }
            Set<String> termsInList = possibleDataset.getTerms();
            for (String tempTerm : termsInList) {
                if (tempTerm.equals(term)) {
                    relatedDatasets.add(possibleDataset);
                    break;
                }
            }
        }
        return relatedDatasets;
    }

    private static List<String> getIntersectionSet(Set<String> set1, Set<String> set2) {
        return set1.stream()
                .filter(set2::contains)
                .collect(Collectors.toList());
    }


    public List<DatasetStatInfo> getBiologicalSimilars() {
        return datasetStatInfoService.readAll();
    }
}
