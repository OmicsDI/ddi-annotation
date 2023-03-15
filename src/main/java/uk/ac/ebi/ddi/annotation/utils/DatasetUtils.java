package uk.ac.ebi.ddi.annotation.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ddi.ddidomaindb.dataset.DSField;
import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;
import uk.ac.ebi.ddi.service.db.utils.DatasetCategory;
import uk.ac.ebi.ddi.xml.validator.parser.model.Date;
import uk.ac.ebi.ddi.xml.validator.parser.model.Entry;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetUtils.class);

    public static Dataset addCrossReferenceValue(Dataset dataset, String key, String value) {
        Map<String, Set<String>> fields = dataset.getCrossReferences();
        if (fields == null) {
            fields = new HashMap<>();
        }
        if (key != null && value != null) {
            Set<String> values = new HashSet<>();
            if (fields.containsKey(key)) {
                values = fields.get(key);
            }
            values.add(value);
            fields.put(key, values);
            dataset.setCrossReferences(fields);
        }
        return dataset;
    }

    public static Set<String> getCrossReference(Dataset dataset, String nameKey) {
        if (dataset.getCrossReferences() != null && !dataset.getCrossReferences().isEmpty()) {
            if (dataset.getCrossReferences().containsKey(nameKey)) {
                return dataset.getCrossReferences().get(nameKey);
            }
        }
        return Collections.emptySet();
    }

    public static Dataset addAdditionalField(Dataset dataset, String key, String value) {
        Map<String, Set<String>> additional = dataset.getAdditional();
        if (additional == null) {
            additional = new HashMap<>();
        }
        if (key != null && value != null) {
            Set<String> values = new HashSet<>();
            if (additional.containsKey(key)) {
                values = additional.get(key);
            }
            values.add(value);
            additional.put(key, values);
            dataset.setAdditional(additional);
        }
        return dataset;
    }

    public static Dataset addAdditionalFieldSingleValue(Dataset dataset, String key, String value) {
        Map<String, Set<String>> additional = dataset.getAdditional();
        if (additional == null) {
            additional = new HashMap<>();
        }
        if (key != null && value != null && !value.isEmpty()) {
            Set<String> values = new HashSet<>();
            values.add(value);
            additional.put(key, values);
            dataset.setAdditional(additional);
        }
        return dataset;
    }

    public static String getFirstAdditional(Dataset dataset, String key) {
        if (dataset.getAdditional() != null && !dataset.getAdditional().isEmpty()) {
            if (dataset.getAdditional().containsKey(key) && !dataset.getAdditional().get(key).isEmpty()) {
                return new ArrayList<>(dataset.getAdditional().get(key)).get(0);
            }
        }
        return null;
    }

    @Deprecated
    public static Dataset transformEntryDataset(Entry dataset) {

        Map<String, Set<String>> dates = new HashMap<>();
        Map<String, Set<String>> crossReferences = new HashMap<>();
        Map<String, Set<String>> additionals = new HashMap<>();

        if (dataset.getName() == null) {
            LOGGER.error("Exception occurred, entry with id name value is not there, {}", dataset.getAcc());
        }
        try {
            if (dataset.getDates() != null) {
                dates = dataset.getDates().getDate().parallelStream()
                        .collect(Collectors.groupingBy(
                                Date::getType,
                                Collectors.mapping(Date::getValue, Collectors.toSet())));
            }
            if (dataset.getCrossReferences() != null && dataset.getCrossReferences().getRef() != null) {
                crossReferences = dataset.getCrossReferences().getRef()
                        .stream().parallel().filter(x -> x.getDbname() != null)
                        .collect(Collectors.groupingBy(
                                x -> x.getDbname().trim(),
                                Collectors.mapping(x -> x.getDbkey().trim(), Collectors.toSet())));
            }
            if (dataset.getAdditionalFields() != null) {
                additionals = dataset.getAdditionalFields().getField()
                        .stream().parallel()
                        .collect(Collectors.groupingBy(
                                x -> x.getName().trim(),
                                Collectors.mapping(x -> x.getValue().trim(), Collectors.toSet())));
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occured, acc: {}", dataset.getAcc());
        }
        return new Dataset(dataset.getId(), dataset.getRepository(),
                dataset.getName() != null ? dataset.getName().getValue() : "",
                dataset.getDescription(), dates, additionals, crossReferences, DatasetCategory.INSERTED);

    }

    /**
     * This function with use a database as a fixed name. That means that the user will use
     * the name provided in the function and not the one provided in the File.
     * @param dataset Dataset Entry from the XML
     * @param databaseName The database Name
     * @return Dataset from the dtabase.
     */
    public static Dataset transformEntryDataset(Entry dataset, String databaseName) {

        Map<String, Set<String>> dates = new HashMap<>();
        Map<String, Set<String>> crossReferences = new HashMap<>();
        Map<String, Set<String>> additionals = new HashMap<>();
        try {
            if (dataset.getName() == null) {
                LOGGER.error("Exception occurred, entry with id name value is not there, acc: {}", dataset.getAcc());
            }
            if (dataset.getDates() != null && dataset.getDates().getDate() != null) {
                dates = dataset.getDates().getDate().parallelStream().filter(date -> date.getType() != null && date.getValue() != null)
                        .collect(Collectors.groupingBy(
                                Date::getType,
                                Collectors.mapping(Date::getValue, Collectors.toSet())));
            }
            crossReferences = new HashMap<>();
            if (dataset.getCrossReferences() != null && dataset.getCrossReferences().getRef() != null) {
                crossReferences = dataset.getCrossReferences().getRef()
                        .stream().parallel().filter(x -> x.getDbname() != null && x.getDbkey() != null)
                        .collect(Collectors.groupingBy(
                                x -> x.getDbname().trim(),
                                Collectors.mapping(x -> x.getDbkey().trim(), Collectors.toSet())));
            }
            if (dataset.getAdditionalFields() != null &&  dataset.getAdditionalFields().getField() != null && !dataset.getAdditionalFields().getField().isEmpty()) {
                additionals = dataset.getAdditionalFields().getField()
                        .stream().filter(field -> field != null).parallel().filter(x -> x.getName() != null && x.getValue() != null)
                        .collect(Collectors.groupingBy(
                                x -> x.getName().trim(),
                                Collectors.mapping(x -> x.getValue().trim(), Collectors.toSet())));
            }
            Set<String> repositories = additionals.get(DSField.Additional.REPOSITORY.getName());
            if (null == repositories || repositories.size() < 1) {
                //AZ:this code overrides additonal.repository. why? wrapped by if
                //** Rewrite the respoitory with the name we would like to handle ***/
                Set<String> databases = new HashSet<>();
                databases.add(databaseName);
                additionals.put(DSField.Additional.REPOSITORY.getName(), databases);
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occured in transformEntryDataset entry with id " + dataset.getId());
        }
        return new Dataset(dataset.getId(), databaseName,
                dataset.getName() != null ? dataset.getName().getValue() : "",
                dataset.getDescription(), dates, additionals, crossReferences, DatasetCategory.INSERTED);

    }

    public static Entry tansformDatasetToEntry(Dataset dataset) {
        Entry entry = new Entry();
        try {
            entry.setId(dataset.getAccession());
            entry.setAcc(dataset.getAccession());
            entry.setDescription(dataset.getDescription());
            entry.setName(dataset.getName());
            if (dataset.getDates() != null) {
                dataset.getDates().forEach((key, value1) -> value1.forEach(value -> entry.addDate(key, value)));
            }
            if (dataset.getCrossReferences() != null) {
                dataset.getCrossReferences()
                        .forEach((key, value1) -> value1.forEach(value -> entry.addCrossReferenceValue(key, value)));
            }
            if (dataset.getAdditional() != null) {
                dataset.getAdditional()
                        .forEach((key, value1) -> value1.forEach(value -> entry.addAdditionalField(key, value)));
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occurred in transformEntryDataset entry with id " + dataset.getId());
        }
        return entry;
    }

    public static Dataset removeCrossReferences(Dataset dataset, String key) {
        dataset.getCrossReferences().remove(key);
        return dataset;
    }

    public static Dataset addCrossReferenceValues(Dataset dataset, String dbName, Set<String> newKeys) {
        if (dataset.getCrossReferences() == null) {
            dataset.setCrossReferences(new HashMap<>());
        }
        dataset.getCrossReferences().put(dbName, newKeys);
        return dataset;
    }
}
