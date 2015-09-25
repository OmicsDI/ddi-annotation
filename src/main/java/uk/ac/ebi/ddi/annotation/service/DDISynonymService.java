package uk.ac.ebi.ddi.annotation.service;

import com.mongodb.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mingze on 22/07/15.
 *
 * find  synonym list for a word, if doesn't have, create one and stored in MongoDB
 */
public class DDISynonymService {

    public List<String> getSynonyms(String word) throws IOException {

        MongoClient mongo = new MongoClient("localhost", 27017);
        DB db = mongo.getDB("SynonymDB");
        DBCollection table = db.getCollection("synonyms");

        ArrayList<String>  synonyms =  getSynonymsInList(word,table);

        if (synonyms.size()==0) {
            DDIAnnotationInternalService annotInternalService = new DDIAnnotationInternalService();
            synonyms =  annotInternalService.getSynonymsForWord(word);
            insertInSynonymsList(synonyms, table);
        }
//        BasicDBObject document = new BasicDBObject();
//        document.put("_id", "test");
//        document.put("next_id", "test2");
//        table.insert(document);
        return synonyms;
    }

    private void insertInSynonymsList(ArrayList<String> synonyms, DBCollection table) {


    }

    private ArrayList<String> getSynonymsInList(String word, DBCollection table) {
        List<String>  synonyms = new ArrayList<>();
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", word);
        DBCursor cursor = table.find(searchQuery);

        while (cursor.hasNext()) {
            System.out.println(cursor.next());
        }

        return null;
    }

}
