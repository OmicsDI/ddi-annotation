package uk.ac.ebi.ddi.annotation.service;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;
import uk.ac.ebi.ddi.service.db.model.dataset.DatasetStatus;
import uk.ac.ebi.ddi.service.db.service.dataset.DatasetStatusService;
import uk.ac.ebi.ddi.service.db.service.dataset.IDatasetService;
import uk.ac.ebi.ddi.service.db.utils.CategoryType;
import uk.ac.ebi.ddi.service.db.utils.DatasetCategory;
import uk.ac.ebi.ddi.xml.validator.parser.model.Date;
import uk.ac.ebi.ddi.xml.validator.parser.model.Entry;
import uk.ac.ebi.ddi.xml.validator.parser.model.Field;
import uk.ac.ebi.ddi.xml.validator.parser.model.Reference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 05/05/2016
 */
public class DDIDatasetAnnotationService {

    @Autowired
    IDatasetService datasetService;

    @Autowired
    DatasetStatusService statusService;


    /**
     * This function transform an Entry in the XML file into a dataset in the database
     * and add then to the database.
     * @param dataset
     */
    public void insertDataset(Entry dataset){

        Dataset dbDataset = transformEntryDataset(dataset);

        dbDataset = datasetService.save(dbDataset);

        if(dbDataset.getId() != null){
            statusService.save(new DatasetStatus(dbDataset.getAccession(), dbDataset.getDatabase(),getDate(), DatasetCategory.INSERTED.getType()));
        }
    }

    public Integer findDataset(Entry dataset){

        Dataset dbDataset = datasetService.read(dataset.getAcc(), dataset.getDatabase());

        if(dbDataset != null)
            return dbDataset.getHashCode();

        return null;

    }

    public void updateDataset(Entry dataset, DatasetCategory category){

        Dataset dbDataset = transformEntryDataset( dataset);

        Dataset currentDataset = datasetService.read(dbDataset.getAccession(), dbDataset.getDatabase());

        if(currentDataset != null){
            currentDataset.setAdditional(dbDataset.getAdditional());
            currentDataset.setCategory(category.getType());
            currentDataset.setCrossReferences(dbDataset.getCrossReferences());
            currentDataset.setDates(dbDataset.getDates());
            currentDataset.setDescription(dbDataset.getDescription());
            currentDataset.setName(dbDataset.getName());
            currentDataset.setFilePath(dbDataset.getFilePath());
        }

        currentDataset = datasetService.update(currentDataset);

        if(currentDataset != null)
            statusService.save(new DatasetStatus(currentDataset.getAccession(), currentDataset.getDatabase(),getDate(), category.getType()));

    }

    private Dataset transformEntryDataset(Entry dataset){

        Map<String, Set<String>> dates = dataset.getDates().getDate().parallelStream().collect(Collectors.groupingBy(Date::getType, Collectors.mapping(Date::getValue, Collectors.toSet())));

        Map<String, Set<String>> crossReferences = dataset.getCrossReferences().getRef()
                .stream().parallel()
                .collect(Collectors.groupingBy(Reference::getDbname, Collectors.mapping(Reference::getDbkey, Collectors.toSet())));

        Map<String, Set<String>> additionals = dataset.getAdditionalFields().getField()
                .stream().parallel()
                .collect(Collectors.groupingBy(Field::getName, Collectors.mapping(Field::getValue, Collectors.toSet())));

        return new Dataset(dataset.getAcc(), dataset.getDatabase(), dataset.getName().getValue(), dataset.getDescription(),dates, additionals, crossReferences, DatasetCategory.INSERTED);

    }

    private String getDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(new java.util.Date());
    }
}
