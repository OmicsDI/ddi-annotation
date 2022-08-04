package uk.ac.ebi.ddi.biostudies;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ddi.biostudies.model.Attributes;
import uk.ac.ebi.ddi.biostudies.model.Links;
import uk.ac.ebi.ddi.biostudies.model.Submissions;
import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;
import uk.ac.ebi.ddi.service.db.service.dataset.DatasetService;
import uk.ac.ebi.ddi.service.db.utils.DatasetCategory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class BioStudiesService {

    public static final String SUBMISSIONS = "submissions";
    public static final String TITLE = "Title";
    public static final String ABSTRACT = "Abstract";
    public static final String EXPERIMENTTYPE = "Experiment type";
    public static final String ORGANISM = "Organism";
    public static final String PUBLICATION = "ReleaseDate";
    public static final String DESCRIPTION = "Description";
    //public static final String

    @Autowired
    DatasetService datasetService;

    Set<String> omicsType = new HashSet<String>();

    public static final Logger LOGGER = LoggerFactory.getLogger(BioStudiesService.class);

    public void parseJson(InputStream is) throws IOException {

       /* List<Submissions> submissionsList = new LinkedList<Submissions>();
        // Create and configure an ObjectMapper instance
        ObjectMapper mapper = new ObjectMapper();
        //mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // Create a JsonParser instance
        try (JsonParser jsonParser = mapper.getFactory().createParser(is)) {
            JsonToken jsonToken = jsonParser.nextToken();

            while (jsonToken != JsonToken.END_OBJECT) {
                System.out.println(jsonParser.getCurrentName());
                if (jsonToken == JsonToken.FIELD_NAME && SUBMISSIONS.equals(jsonParser.getCurrentName())) {

                    System.out.println("\nYour are in submissions ");

                    jsonToken = jsonParser.nextToken();

                    if (jsonToken == JsonToken.START_ARRAY) {
                        jsonToken = jsonParser.nextToken();
                    }
                    // Iterate over the tokens until the end of the array
                    while (jsonToken != JsonToken.END_ARRAY) {
                        // Read a contact instance using ObjectMapper and do something with it
                        Submissions submissions = mapper.readValue(jsonParser, Submissions.class);

                        if (submissions.getSection().getType().equals("Study")) {
                            Dataset dataset = transformSubmissionDataset(submissions);
                            if (!dataset.getAdditional().containsKey("additional_accession")) {
                                updateDataset(dataset);
                            } else {
                                String accession = dataset.getAdditional().get("additional_accession")
                                        .iterator().next();
                                System.out.println("updating secondary accession of " + accession);
                                List<Dataset> datasetList = datasetService.findByAccession(accession);
                                if (datasetList.size() > 0) {
                                    Dataset dataset1 =  datasetList.get(0);
                                    if (dataset1.getAdditional().containsKey("additional_accession")) {
                                        dataset1.getAdditional().get("additional_accession")
                                                .add(dataset.getAccession());
                                        updateDataset(dataset1);
                                    }
                                } else {
                                    updateDataset(dataset);
                                }
                                System.out.println("secondary accession is " + accession);
                            }
                            submissions.toString();
                            System.out.println("accession number is " + submissions.getAccno());
                        }
                        submissionsList.add(submissions);
                        System.out.println("list count is " + submissionsList.size());
                        jsonToken = jsonParser.nextToken();
                    }
                }
                jsonToken = jsonParser.nextToken();
            }
        }*/
    }

    public void parseJsonBioStudies(InputStream is) throws IOException {

        List<Submissions> submissionsList = new LinkedList<Submissions>();
        // Create and configure an ObjectMapper instance
        ObjectMapper mapper = new ObjectMapper();
        //mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // Create a JsonParser instance
        try (JsonParser jsonParser = mapper.getFactory().createParser(is)) {
            JsonToken jsonToken = jsonParser.nextToken();

            while (jsonToken != JsonToken.END_ARRAY && jsonToken != null) {
                System.out.println(jsonParser.getCurrentName());
                if (jsonToken == JsonToken.FIELD_NAME) {

                    System.out.println("\nYour are in submissions ");


                        // Read a contact instance using ObjectMapper and do something with it
                        Submissions submissions = mapper.readValue(jsonParser, Submissions.class);

                        if (submissions.getSection().getType().equals("Study")) {
                            Dataset dataset = transformSubmissionDataset(submissions);
                            if (!dataset.getAdditional().containsKey("additional_accession")) {
                                updateDataset(dataset);
                            } else {
                                String accession = dataset.getAdditional().get("additional_accession")
                                        .iterator().next();
                                System.out.println("updating secondary accession of " + accession);
                                List<Dataset> datasetList = datasetService.findByAccession(accession);
                                if (datasetList.size() > 0) {
                                    Dataset dataset1 =  datasetList.get(0);
                                    if (dataset1.getAdditional().containsKey("additional_accession")) {
                                        dataset1.getAdditional().get("additional_accession")
                                                .add(dataset.getAccession());
                                        updateDataset(dataset1);
                                    }
                                } else {
                                    updateDataset(dataset);
                                }
                                System.out.println("secondary accession is " + accession);
                            }
                            submissions.toString();
                            System.out.println("accession number is " + submissions.getAccNo());
                        }
                        submissionsList.add(submissions);
                        System.out.println("list count is " + submissionsList.size());
                        jsonToken = jsonParser.nextToken();
                    }
                jsonToken = jsonParser.nextToken();
                }
                jsonToken = jsonParser.nextToken();
            }
        }

    public void parseSubmissions(JsonParser jsonParser) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        Submissions submissions = mapper.readValue(jsonParser, Submissions.class);

        if (submissions.getSection().getType().equals("Study")) {
            Dataset dataset = transformSubmissionDataset(submissions);
            if (!dataset.getAdditional().containsKey("additional_accession")) {
                updateDataset(dataset);
            } else {
                String accession = dataset.getAdditional().get("additional_accession")
                        .iterator().next();
                System.out.println("updating secondary accession of " + accession);
                List<Dataset> datasetList = datasetService.findByAccession(accession);
                if (datasetList.size() > 0) {
                    Dataset dataset1 =  datasetList.get(0);
                    if (dataset1.getAdditional().containsKey("additional_accession")) {
                        dataset1.getAdditional().get("additional_accession")
                                .add(dataset.getAccession());
                        updateDataset(dataset1);
                    }
                } else {
                    updateDataset(dataset);
                }
                System.out.println("secondary accession is " + accession);
            }
            submissions.toString();
            System.out.println("accession number is " + submissions.getAccNo());
        }
    }
   /* public static void main(String[] args) throws IOException {
        File initialFile = new File("/partition/sample.json");
        InputStream targetStream = new FileInputStream(initialFile);

        BioStudiesService studiesParser = new BioStudiesService();
        studiesParser.saveStudies("/partition/sample.json");
    }*/

    public void saveStudies(String filePath) throws IOException {
        omicsType.add("Unknown");
        File initialFile = new File(filePath);
        //File initialFile = new File("/media/gaur/Elements/biostudies/publicOnlyStudies.json");
        //File initialFile = new File("/media/gaur/Elements/teststudies.json");
        InputStream targetStream = new FileInputStream(initialFile);
        parseJsonBioStudies(targetStream);
        //StudiesParserService studiesParser = new StudiesParserService();
        //studiesParser.parseJson(targetStream);
    }
    public Dataset transformSubmissionDataset(Submissions submissions) {
        Dataset dataset = new Dataset();
        try {
            HashSet<String> datasetLink = new HashSet<String>();
            Map<String, String> subsections = null;
            datasetLink.add("https://www.ebi.ac.uk/biostudies/studies/" + submissions.getAccNo());
            HashSet<String> authors = new HashSet<String>();
            dataset.setAccession(submissions.getAccNo());
            dataset.setDatabase("BioStudies");
            dataset.setCurrentStatus(DatasetCategory.INSERTED.getType());
            dataset.addAdditional("omics_type", omicsType);
            dataset.addAdditional("full_dataset_link", datasetLink);
            Map<String, String> sectionMap = submissions.getSection().getAttributes() != null ?
                    submissions.getSection().getAttributes().stream()
                    .collect(Collectors.toMap(Attributes::getName, Attributes::getValue, (a1, a2) -> a1)) : null;
            Map<String, String> attributesMap = submissions.getAttributes().stream()
                    .collect(Collectors.toMap(Attributes::getName, Attributes::getValue, (a1, a2) -> a1));
            Map<String, String> linksMap = null;
            List<Links> linksList = submissions.getSection().getLinks();
            if (submissions.getSection() != null && submissions.getSection().getSubsections() != null) {
                subsections = submissions.getSection().getSubsections()
                        .stream().filter(r -> r.getType().equals("Author")).map(r -> r.getAttributes())
                        .flatMap(x -> x.stream()).collect(Collectors.toMap(Attributes::getName,
                                Attributes::getValue, (a1, a2) -> a1));
                authors.add(subsections.get("Name"));
                dataset.addAdditional("submitter", authors);
            }

            if (linksList != null && linksList.size() > 0 && linksList.get(0).getAttributes() != null) {
                linksMap = linksList.stream().map(r -> r.getAttributes())
                        .flatMap(x -> x.stream()).collect(Collectors.toMap(Attributes::getName,
                                Attributes::getValue, (a1, a2) -> a1));

            }
            //.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,(countInFirstMap, countInSecondMap)
            // -> countInFirstMap + countInSecondMap)));

            if (linksMap != null && linksMap.containsKey("Type")) {
                String accession = linksList.get(0).getUrl();
                HashSet<String> setSecAcc = new HashSet<String>();
                setSecAcc.add(accession);
                dataset.addAdditional("additional_accession", setSecAcc);
            }
            if (sectionMap != null && sectionMap.containsKey(ABSTRACT.toString())) {
                dataset.setDescription(sectionMap.get(ABSTRACT.toString()));
            } else if (sectionMap != null && sectionMap.containsKey(DESCRIPTION.toString())) {
                dataset.setDescription(sectionMap.get(DESCRIPTION.toString()));
            }
            if (attributesMap.containsKey(TITLE.toString())) {
                dataset.setName(attributesMap.get(TITLE.toString()));
            } else if (sectionMap != null && sectionMap.containsKey(TITLE.toString())) {
                dataset.setName(sectionMap.get(TITLE.toString()));
            }
            Map<String, Set<String>> dates = new HashMap<String, Set<String>>();
            HashSet<String> setData = new HashSet<String>();
            HashSet<String> setOrganisms = new HashSet<String>();
            setData.add(attributesMap.get(PUBLICATION));
            //dates.put()
            dates.put("publication", setData);

            HashSet<String> repository = new HashSet<String>();
            repository.add("biostudies");
            dataset.addAdditional("repository", repository);


            dataset.setDates(dates);
            //setData.clear();
            if (sectionMap != null && sectionMap.containsKey(ORGANISM.toString())) {
                setOrganisms.add(sectionMap.get(ORGANISM.toString()));
                dataset.getAdditional().put("species", setOrganisms);
            }
        } catch (Exception ex) {
            LOGGER.error("exception while parsing submission into dataset in transformsubmission", ex.getMessage());
        }
        //if(submissions.getSection().getLinks().)
        return dataset;
    }

    public void updateDataset(Dataset dataset) {

        Dataset inDataset = datasetService.
                read(dataset.getAccession(), dataset.getDatabase());
        if (inDataset != null) {
            datasetService.update(inDataset.getId(), dataset);
        } else {
            datasetService.save(dataset);
        }
    }
}
