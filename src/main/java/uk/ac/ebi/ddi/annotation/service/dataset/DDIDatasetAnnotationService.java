package uk.ac.ebi.ddi.annotation.service.dataset;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ddi.annotation.utils.DatasetUtils;
import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;
import uk.ac.ebi.ddi.service.db.model.dataset.DatasetStatus;
import uk.ac.ebi.ddi.service.db.model.publication.PublicationDataset;
import uk.ac.ebi.ddi.service.db.service.dataset.IDatasetService;
import uk.ac.ebi.ddi.service.db.service.dataset.IDatasetStatusService;
import uk.ac.ebi.ddi.service.db.service.publication.IPublicationDatasetService;
import uk.ac.ebi.ddi.service.db.utils.DatasetCategory;
import uk.ac.ebi.ddi.xml.validator.parser.model.Date;
import uk.ac.ebi.ddi.xml.validator.parser.model.Entry;
import uk.ac.ebi.ddi.xml.validator.parser.model.Reference;
import uk.ac.ebi.ddi.xml.validator.utils.Field;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 05/05/2016
 */
public class DDIDatasetAnnotationService {

    @Autowired
    IDatasetService datasetService;

    @Autowired
    IDatasetStatusService statusService;

    @Autowired
    IPublicationDatasetService publicationService;

    /**
     * This function looks for individual datasets and check if they are in the database and if they needs to
     * be updated.
     *
     * @param dataset
     */
    public void insertDataset(Entry dataset){
        Dataset dbDataset = DatasetUtils.transformEntryDataset(dataset);
        Dataset currentDataset = datasetService.read(dbDataset.getAccession(), dbDataset.getDatabase());
        if(currentDataset == null){
            insertDataset(dbDataset);
        }else if(currentDataset.getInitHashCode() != dbDataset.getInitHashCode()){
            updateDataset(currentDataset, dbDataset);
        }
    }

    private void updateDataset(Dataset currentDataset, Dataset dbDataset) {
        dbDataset = datasetService.update(currentDataset.getId(), dbDataset);
        if(dbDataset.getId() != null){
            statusService.save(new DatasetStatus(dbDataset.getAccession(), dbDataset.getDatabase(), dbDataset.getInitHashCode(), getDate(), DatasetCategory.INSERTED.getType()));
        }
    }

    public void annotateDataset(Dataset exitingDataset) {
        if(!exitingDataset.getCurrentStatus().equalsIgnoreCase(DatasetCategory.DELETED.getType()))
            exitingDataset.setCurrentStatus(DatasetCategory.UPDATED.getType());
        datasetService.update(exitingDataset.getId(), exitingDataset);
        if(exitingDataset.getCrossReferences() != null && !DatasetUtils.getCrossReferenceFieldValue(exitingDataset, Field.PUBMED.getName()).isEmpty()){
            for(String pubmedId: DatasetUtils.getCrossReferenceFieldValue(exitingDataset, Field.PUBMED.getName())){
                //Todo: In the future we need to check for providers that have multiple omics already.
                publicationService.save(new PublicationDataset(pubmedId, exitingDataset.getAccession(), exitingDataset.getDatabase(), DatasetUtils.getFirstAdditionalFieldValue(exitingDataset, Field.OMICS.getName())));
            }
        }
    }

    public List<PublicationDataset> getPublicationDatasets(){
        return publicationService.readAll();
    }

    public void enrichedDataset(Dataset existingDataset) {
        if(!existingDataset.getCurrentStatus().equalsIgnoreCase(DatasetCategory.DELETED.getType()))
            existingDataset.setCurrentStatus(DatasetCategory.ENRICHED.getType());
        datasetService.update(existingDataset.getId(), existingDataset);
    }

    public void updateDeleteStatus(Dataset dataset) {
        Dataset existingDataset = datasetService.read(dataset.getId());
        updateStatus(existingDataset, DatasetCategory.DELETED.getType());
    }

    private void updateStatus(Dataset dbDataset, String status){
        dbDataset.setCurrentStatus(status);
        dbDataset = datasetService.update(dbDataset.getId(), dbDataset);
        if(dbDataset.getId() != null){
            statusService.save(new DatasetStatus(dbDataset.getAccession(), dbDataset.getDatabase(), dbDataset.getInitHashCode(), getDate(), status));
        }
    }

    public List<Dataset> getAllDatasetsByDatabase(String databaseName){
        return datasetService.readDatasetHashCode(databaseName);
    }



    public Dataset getDataset(String accession, String database) {
        return datasetService.read(accession, database);
    }

    /**
     * This function transform an Entry in the XML file into a dataset in the database
     * and add then to the database.
     * @param dbDataset
     */
    private void insertDataset(Dataset dbDataset){
        dbDataset = datasetService.save(dbDataset);
        if(dbDataset.getId() != null){
            statusService.save(new DatasetStatus(dbDataset.getAccession(), dbDataset.getDatabase(), dbDataset.getInitHashCode(), getDate(), DatasetCategory.INSERTED.getType()));
        }
    }

    public Integer findDataset(Entry dataset){

        Dataset dbDataset = datasetService.read(dataset.getAcc(), dataset.getDatabase());

        if(dbDataset != null)
            return dbDataset.getInitHashCode();

        return null;

    }

    private String getDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(new java.util.Date());
    }

    public void updateDataset(Dataset dataset) {
        datasetService.update(dataset.getId(), dataset);
    }
}
