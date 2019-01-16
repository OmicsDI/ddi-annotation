package uk.ac.ebi.ddi.annotation.service.synonyms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ddi.annotation.model.DatasetTobeEnriched;
import uk.ac.ebi.ddi.annotation.model.EnrichedDataset;
import uk.ac.ebi.ddi.xml.validator.exception.DDIException;
import uk.ac.ebi.ddi.xml.validator.parser.OmicsXMLFile;
import uk.ac.ebi.ddi.xml.validator.parser.model.Entry;
import uk.ac.ebi.ddi.xml.validator.parser.model.Reference;
import uk.ac.ebi.ddi.xml.validator.utils.Field;

import java.io.File;
import java.util.List;


/**
 * Provide service for dataset XML file processing, do enrichment annotation and similarity calculation
 * This a Testing class and should be deprecated in the future because is not use by any of the services.
 *
 * @author Mingze
 */
public class DDIXmlProcessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DDIXmlProcessService.class);

    @Autowired
    DDIAnnotationService annotService = new DDIAnnotationService();

    @Autowired
    DDIExpDataImportService ddiExpDataImportService = new DDIExpDataImportService();

    private OmicsXMLFile reader;

    public DDIXmlProcessService() {

    }

    /**
     * Import a dataset xml file in to the enrichment DB
     * @param file file to be imported
     * @param dataType dataset type
     * @return success(true) or fail(false)
     */

    public boolean xmlFileImport(File file, String dataType) {
        if (file.isFile() && file.getName().toLowerCase().endsWith("xml")) {
            int index = 1;
            try {
                reader = new OmicsXMLFile(file);
            } catch (DDIException e) {
                e.printStackTrace();
            }
            String database = reader.getName();
            for (int i = 0; i < reader.getEntryIds().size(); i++) {
                LOGGER.debug("dealing the" + index + "entry in " + file.getName() + ";");
                index++;
                Entry entry = null;
                try {
                    entry = reader.getEntryByIndex(i);
                } catch (DDIException e) {
                    e.printStackTrace();
                }
                List<Reference> refs = entry != null ? entry.getCrossReferences().getRef() : null;

                DatasetTobeEnriched datasetTobeEnriched = prepareTheDataset(entry, database, dataType);

                enrichTheDataset(datasetTobeEnriched, refs);
            }
            return true;
        } else {
            LOGGER.error("The file is not an XML File");
            return false;
        }
    }

    /**
     * Construct the whole dataset objcet to be enriched, based on the info extracted from xml file
     * @param entry  dataset entry
     * @param database database name
     * @param dataType
     * @return
     */
    public DatasetTobeEnriched prepareTheDataset(Entry entry, String database, String dataType) {

        String accession = entry.getId();
        DatasetTobeEnriched datasetTobeEnriched = new DatasetTobeEnriched(accession, database, dataType);

        datasetTobeEnriched.addAttribute(Field.NAME.getName(), entry.getName().getValue());
        datasetTobeEnriched.addAttribute(Field.DESCRIPTION.getName(), entry.getDescription());
        datasetTobeEnriched.addAttribute(Field.SAMPLE.getName(), entry.getAdditionalFieldValue("sample_protocol"));
        datasetTobeEnriched.addAttribute(Field.DATA.getName(), entry.getAdditionalFieldValue("data_protocol"));
        return datasetTobeEnriched;
    }

    public EnrichedDataset enrichTheDataset(DatasetTobeEnriched datasetTobeEnriched, List<Reference> refs) {
        String dataType = datasetTobeEnriched.getDataType();
        String accession = datasetTobeEnriched.getAccession();
        String database = datasetTobeEnriched.getDatabase();

        ddiExpDataImportService.importDatasetTerms(dataType, accession, database, refs);

        try {
            return annotService.enrichment(datasetTobeEnriched, false);
        } catch (Exception e) {
            LOGGER.error("Exception occurred when enriching dataset {}, ", datasetTobeEnriched.getAccession(), e);
        }
        return null;
    }


}
