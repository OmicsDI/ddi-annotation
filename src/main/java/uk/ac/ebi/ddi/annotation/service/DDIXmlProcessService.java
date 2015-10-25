package uk.ac.ebi.ddi.annotation.service;

/**
 * Created by mingze on 22/10/15.
 */

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ddi.annotation.model.DatasetTobeEnriched;
import uk.ac.ebi.ddi.annotation.model.EnrichedDataset;
import uk.ac.ebi.ddi.annotation.service.DDIExpDataImportService;
import uk.ac.ebi.ddi.annotation.utils.DataType;
import uk.ac.ebi.ddi.xml.validator.exception.DDIException;
import uk.ac.ebi.ddi.xml.validator.parser.OmicsXMLFile;
import uk.ac.ebi.ddi.xml.validator.parser.model.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;


/**
 * Provide service for dataset XML file processing, do enrichment annotation and similarity calculation
 *
 * @author Mingze
 */
public class DDIXmlProcessService {

    private static final Logger logger = LoggerFactory.getLogger(DDIXmlProcessService.class);

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
                logger.debug("dealing the" + index + "entry in " + file.getName() + ";");
                index++;
                Entry entry = null;
                try {
                    entry = reader.getEntryByIndex(i);
                } catch (DDIException e) {
                    e.printStackTrace();
                }
                List<Reference> refs = entry.getCrossReferences().getRef();

                DatasetTobeEnriched datasetTobeEnriched = prepareTheDataset(entry, database, dataType);

                enrichTheDataset(datasetTobeEnriched, refs);
            }
            return true;
        } else {
            logger.error("The file is not an XML File");
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

        String title = entry.getName().getValue();
        String abstractDesc = entry.getDescription();
        String sampleProtocol = entry.getAdditionalFieldValue("sample_protocol");
        String dataProtocol = entry.getAdditionalFieldValue("data_protocol");

        datasetTobeEnriched.setTitle(title);
        datasetTobeEnriched.setAbstractDescription(abstractDesc);
        datasetTobeEnriched.setSampleProtocol(sampleProtocol);
        datasetTobeEnriched.setDataProtocol(dataProtocol.substring(0, dataProtocol.length()));

        return datasetTobeEnriched;
    }

    public void enrichTheDataset(DatasetTobeEnriched datasetTobeEnriched, List<Reference> refs) {
        String dataType = datasetTobeEnriched.getDataType();
        String accession = datasetTobeEnriched.getAccession();
        String database = datasetTobeEnriched.getDatabase();

        ddiExpDataImportService.importDatasetTerms(dataType, accession, database, refs);

        try {
            EnrichedDataset enrichedDataset = annotService.enrichment(datasetTobeEnriched);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (DDIException e) {
            e.printStackTrace();
        }

    }


}
