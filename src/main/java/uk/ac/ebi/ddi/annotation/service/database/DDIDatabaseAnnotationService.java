package uk.ac.ebi.ddi.annotation.service.database;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ddi.service.db.model.dataset.Database;
import uk.ac.ebi.ddi.service.db.service.dataset.IDatabaseService;

import java.util.List;

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
 * Created by ypriverol (ypriverol@gmail.com) on 26/05/2016.
 */
public class DDIDatabaseAnnotationService {

    @Autowired
    IDatabaseService databaseService;

    /**
     * This function looks for individual datasets and check if they are in the database and if they needs to
     * be updated.
     *
     * @param name
     * @param releaseTag
     * @param omicsType
     */
    public void updateDatabase(String name, String description, String releaseDate, String releaseTag, List<String> omicsType, String url){
        Database database = new Database(name, description, releaseDate, releaseTag, omicsType, url);
        Database existingDatabase = databaseService.read(name);
        if(existingDatabase != null)
            databaseService.update(existingDatabase.get_id(), database);
        else
            databaseService.save(database);
    }

    public Database getDatabaseInfo( String name){
        return databaseService.read(name);
    }

    public List<Database> getDatabases(){
        return databaseService.readAll();
    }

}
