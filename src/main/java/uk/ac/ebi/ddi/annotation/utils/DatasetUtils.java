package uk.ac.ebi.ddi.annotation.utils;

import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;
import uk.ac.ebi.ddi.service.db.utils.DatasetCategory;
import uk.ac.ebi.ddi.xml.validator.parser.model.*;
import uk.ac.ebi.ddi.xml.validator.utils.Field;

import java.util.*;
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
 * Created by ypriverol (ypriverol@gmail.com) on 25/05/2016.
 */
public class DatasetUtils {


    public static Dataset addCrossReferenceValue(Dataset dataset, String key, String value) {
        Map<String, Set<String>> fields = dataset.getCrossReferences();
        if(fields == null)
            fields = new HashMap<>();
        if(key != null && value != null){
            Set<String> values = new HashSet<>();
            if(fields.containsKey(key))
                values = fields.get(key);
            values.add(value);
            fields.put(key, values);
            dataset.setCrossReferences(fields);
        }
        return dataset;
    }

    public static Set<String> getCrossReferenceFieldValue(Dataset dataset, String nameKey) {
        if(dataset.getCrossReferences() != null && !dataset.getCrossReferences().isEmpty()){
            if(dataset.getCrossReferences().containsKey(nameKey))
                return dataset.getCrossReferences().get(nameKey);
        }
        return Collections.EMPTY_SET;
    }

    public static Dataset addAdditionalField(Dataset dataset, String key, String value) {
        Map<String, Set<String>> additional = dataset.getAdditional();
        if(additional == null)
            additional = new HashMap<>();
        if(key != null && value != null){
            Set<String> values = new HashSet<>();
            if(additional.containsKey(key))
                values = additional.get(key);
            values.add(value);
            additional.put(key, values);
            dataset.setAdditional(additional);
        }
        return dataset;
    }


    public static String getFirstAdditionalFieldValue(Dataset dataset, String key) {
        if(dataset.getAdditional() != null && !dataset.getAdditional().isEmpty())
            if(dataset.getAdditional().containsKey(key))
                return new ArrayList<>(dataset.getAdditional().get(key)).get(0);
        return null;

    }

    @Deprecated
    public static Dataset transformEntryDataset(Entry dataset){

        Map<String, Set<String>> dates= new HashMap<>();;
        Map<String, Set<String>> crossReferences= new HashMap<>();;
        Map<String, Set<String>> additionals = new HashMap<>();
        try {
            dates = dataset.getDates().getDate().parallelStream().collect(Collectors.groupingBy(uk.ac.ebi.ddi.xml.validator.parser.model.Date::getType, Collectors.mapping(uk.ac.ebi.ddi.xml.validator.parser.model.Date::getValue, Collectors.toSet())));

            if (dataset.getCrossReferences() != null && dataset.getCrossReferences().getRef() != null) {
                crossReferences = dataset.getCrossReferences().getRef()
                        .stream().parallel()
                        .collect(Collectors.groupingBy(x -> x.getDbname().trim(), Collectors.mapping(x -> x.getDbkey().trim(), Collectors.toSet())));
            }
             additionals = dataset.getAdditionalFields().getField()
                    .stream().parallel()
                    .collect(Collectors.groupingBy(x -> x.getName().trim(), Collectors.mapping(x -> x.getValue().trim(), Collectors.toSet())));
        }
        catch(Exception ex)
        {
            System.out.println("exception occured in entry with id " + dataset.getId());
        }
        return new Dataset(dataset.getId(), dataset.getDatabase(), dataset.getName().getValue(), dataset.getDescription(),dates, additionals, crossReferences, DatasetCategory.INSERTED);

    }

    /**
     * This function with use a database as a fixed name. That means that the user will use
     * the name provided in the function and not the one provided in the File.
     * @param dataset Dataset Entry from the XML
     * @param databaseName The database Name
     * @return Dataset from the dtabase.
     */
    public static Dataset transformEntryDataset(Entry dataset, String databaseName){

        Map<String, Set<String>> dates= new HashMap<>();;
        Map<String, Set<String>> crossReferences= new HashMap<>();;
        Map<String, Set<String>> additionals = new HashMap<>();
        try {
            dates = dataset.getDates().getDate().parallelStream().collect(Collectors.groupingBy(uk.ac.ebi.ddi.xml.validator.parser.model.Date::getType, Collectors.mapping(uk.ac.ebi.ddi.xml.validator.parser.model.Date::getValue, Collectors.toSet())));

            crossReferences = new HashMap<>();
            if (dataset.getCrossReferences() != null && dataset.getCrossReferences().getRef() != null) {
                crossReferences = dataset.getCrossReferences().getRef()
                        .stream().parallel()
                        .collect(Collectors.groupingBy(x -> x.getDbname().trim(), Collectors.mapping(x -> x.getDbkey().trim(), Collectors.toSet())));
            }
            additionals = dataset.getAdditionalFields().getField()
                    .stream().parallel()
                    .collect(Collectors.groupingBy(x -> x.getName().trim(), Collectors.mapping(x -> x.getValue().trim(), Collectors.toSet())));

            //** Rewrite the respoitory with the name we would like to handle ***/
            Set<String> databases = new HashSet<>();
            databases.add(databaseName);
            additionals.put(Field.REPOSITORY.getName(), databases);
        }
        catch(Exception ex){
            System.out.println("exception occured in entry with id " + dataset.getId());
        }
        return new Dataset(dataset.getId(), databaseName, dataset.getName().getValue(), dataset.getDescription(),dates, additionals, crossReferences, DatasetCategory.INSERTED);

    }

    public static Entry tansformDatasetToEntry(Dataset dataset){

        Entry entry = new Entry();
        entry.setId(dataset.getAccession());
        entry.setAcc(dataset.getAccession());
        entry.setDescription(dataset.getDescription());
        entry.setName(dataset.getName());
        if(dataset.getDates() != null)
            dataset.getDates().entrySet().stream().forEach( date -> date.getValue().stream().forEach(value -> entry.addDate(date.getKey(), value)));
        if(dataset.getCrossReferences() != null)
            dataset.getCrossReferences().entrySet().stream().forEach( cross -> cross.getValue().stream().forEach(value -> entry.addCrossReferenceValue(cross.getKey(), value)));
        if(dataset.getAdditional() != null)
            dataset.getAdditional().entrySet().stream().forEach( additional -> additional.getValue().stream().forEach(value -> entry.addAdditionalField(additional.getKey(), value)));

        return entry;
    }

    public static Dataset removeCrossReferences(Dataset dataset, String key) {
        if(dataset.getCrossReferences().containsKey(key))
            dataset.getCrossReferences().remove(key);
        return dataset;
    }

    public static Dataset addCrossReferenceValues(Dataset dataset, String dbName, Set<String> newKeys) {
        if(dataset.getCrossReferences() == null)
            dataset.setCrossReferences(new HashMap<>());
        dataset.getCrossReferences().put(dbName, newKeys);
        return dataset;

    }
}
