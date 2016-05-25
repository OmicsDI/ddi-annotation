package uk.ac.ebi.ddi.annotation.utils;

import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;

import java.util.*;

/**
 * Created by yperez on 25/05/2016.
 */
public class DatasetUtils {


    public static Dataset addCrossReferenceValue(Dataset dataset, String key, String value) {
        if(dataset.getCrossReferences() == null)
            dataset.setCrossReferences(new HashMap<>());

        Map<String, Set<String>> fields = dataset.getCrossReferences();
        if(key != null && value != null){
            Set<String> values = null;
            if(!fields.containsKey(key))
                values = new HashSet<>();
            else
                values = fields.get(key);
            values.add(value);
            fields.put(key, values);
        }
        dataset.setCrossReferences(fields);
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
        if(key != null && value != null){
            Map<String, Set<String>> additional = dataset.getAdditional();
            if(additional == null)
                additional = new HashMap();
            Set<String> values = new HashSet<>();
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
}
