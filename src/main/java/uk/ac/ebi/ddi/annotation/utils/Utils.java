package uk.ac.ebi.ddi.annotation.utils;

import uk.ac.ebi.ddi.ddidomaindb.dataset.DSField;
import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 20/10/15
 */
public class Utils {

    public static String encode(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(char ch) {
        return ch > 128 || " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }

    public static String removeRedundantSynonyms(String synonyms) {
        if (synonyms != null) {
            Set<String> resultStringSet = new HashSet<>();
            StringBuilder resultSynonym = new StringBuilder();
            String[] synonymsArr = synonyms.split(";");
            for (String synonym: synonymsArr) {
                if (synonym != null && !synonym.isEmpty()) {
                    String[] redudantSynonyms = synonym.split(",");
                    for (String redundantSynom: redudantSynonyms) {
                        resultStringSet.add(redundantSynom.trim());
                    }
                }
            }
            for (String synonym: resultStringSet) {
                resultSynonym.append(synonym).append(", ");
            }
            if ((resultSynonym.length() > 0) && resultSynonym.length() > 2) {
                resultSynonym = new StringBuilder(resultSynonym.substring(0, resultSynonym.length() - 2));
            }
            return resultSynonym.toString();

        }
        return null;
    }

    public static Dataset replaceTextCase(Dataset existingDataset) {
        if (existingDataset.getAdditional().get(DSField.Additional.DISEASE_FIELD.getName()) != null) {
            Set<String> diseases = existingDataset.getAdditional().get(DSField.Additional.DISEASE_FIELD.getName());
            Set<String> updatedDisease =  diseases.parallelStream()
                    .map(x -> Utils.toTitleCase(x.toLowerCase())).collect(Collectors.toSet());
            existingDataset.addAdditional(DSField.Additional.DISEASE_FIELD.getName(), updatedDisease);
        }
        if (existingDataset.getAdditional().get(DSField.Additional.SPECIE_FIELD.getName()) != null) {
            Set<String> diseases = existingDataset.getAdditional().get(DSField.Additional.SPECIE_FIELD.getName());
            Set<String> updatedSpecies =  diseases.parallelStream()
                    .map(x -> Utils.toTitleCase(x.toLowerCase())).collect(Collectors.toSet());
            existingDataset.addAdditional(DSField.Additional.SPECIE_FIELD.getName(), updatedSpecies);
        }
        if (existingDataset.getAdditional().get(DSField.Additional.TISSUE_FIELD.getName()) != null) {
            Set<String> diseases = existingDataset.getAdditional().get(DSField.Additional.TISSUE_FIELD.getName());
            Set<String> updatedTissue = diseases.parallelStream()
                    .map(x -> Utils.toTitleCase(x.toLowerCase())).collect(Collectors.toSet());
            existingDataset.addAdditional(DSField.Additional.TISSUE_FIELD.getName(), updatedTissue);
        }
        return existingDataset;

    }

    public static String toTitleCase(String input) {
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;
        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }
            titleCase.append(c);
        }
        return titleCase.toString();
    }
}
