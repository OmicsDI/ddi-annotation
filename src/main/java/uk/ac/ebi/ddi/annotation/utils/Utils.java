package uk.ac.ebi.ddi.annotation.utils;

import java.util.HashSet;
import java.util.Set;

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
        return ch > 128 || ch < 0 || " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }

    public static String removeRedundantSynonyms(String synonyms){
        if(synonyms != null){
            Set<String> resultStringSet = new HashSet<>();
            String resultSynonym = "";
            String[] synonymsArr = synonyms.split(";");
            for(String synonym: synonymsArr){
                if(synonym != null && !synonym.isEmpty()){
                    String[] redudantSynonyms = synonym.split(",");
                    for(String redundantSynom: redudantSynonyms)
                        resultStringSet.add(redundantSynom.trim());
                }
            }
            for(String synonym: resultStringSet)
                resultSynonym += synonym + ", ";
            if(!resultSynonym.isEmpty() && resultSynonym.length() > 2)
                resultSynonym = resultSynonym.substring(0, resultSynonym.length()-2);
            return resultSynonym;

        }
        return null;
    }
}