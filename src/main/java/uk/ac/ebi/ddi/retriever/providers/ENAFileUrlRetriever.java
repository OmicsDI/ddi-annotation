package uk.ac.ebi.ddi.retriever.providers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ddi.annotation.model.ENAReadRunDataset;
import uk.ac.ebi.ddi.ddidomaindb.database.DB;
import uk.ac.ebi.ddi.retriever.DatasetFileUrlRetriever;
import uk.ac.ebi.ddi.retriever.IDatasetFileUrlRetriever;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class ENAFileUrlRetriever extends DatasetFileUrlRetriever {

    private static final String ENA_ENDPOINT = "www.ebi.ac.uk/ena/portal/api";

    public ENAFileUrlRetriever(IDatasetFileUrlRetriever datasetDownloadingRetriever) {
        super(datasetDownloadingRetriever);
    }

    @Override
    public Set<String> getAllDatasetFiles(String accession, String database) throws IOException {

        Set<String> result = new HashSet<>();
        //getTemplateFiles();
        //getReadRunFiles();
        return result;
    }

    /*public <T> void getFiles(URI uri, Class<T[]> data) throws IOException{
        Set<String> result = new HashSet<>();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        ObjectMapper objectMapper = new ObjectMapper();
        HttpGet httpGet = new HttpGet(uri);
        CloseableHttpResponse response1 = httpclient.execute(httpGet);

        try{
            if(response1.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity1 = response1.getEntity();

                T[] myObject = objectMapper.readValue(entity1.getContent(), data);
                System.out.println(myObject.length);
            }
        } finally {
            response1.close();
        }
    }*/
    public Set<String> getReadRunFiles(String accession) throws IOException{
        Set<String> result = new HashSet<>();
        try{
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost(ENA_ENDPOINT)
                    .setPath("/search")
                    .setParameter("query", "(study_accession=" + accession + ")")
                    .setParameter("fields", "study_accession,fastq_ftp,fastq_aspera,fastq_galaxy")
                    .setParameter("result", "read_run")
                    .setParameter("limit", "0")
                    .setParameter("format", "json")
                    .build();
            ResponseEntity<JsonNode> files = execute(x -> restTemplate.getForEntity(uri, JsonNode.class));

            for (JsonNode node : files.getBody()) {
                result.addAll(Arrays.asList(node.get("fastq_ftp").asText().split(";")));
            /*String fastqAspera = node.get("fastq_aspera").asText();
            String fastqGalaxy = node.get("fastq_galaxy").asText();*/
            }
            //getFiles(uri, ENAReadRunDataset[].class);
        }
        catch(URISyntaxException ex){

        }
        return result;
    }

    public Set<String> getAnalysisFiles(String accession) throws IOException{
        Set<String> result = new HashSet<>();
        try{
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost(ENA_ENDPOINT)
                    .setPath("/search")
                    .setParameter("query", "(study_accession=" + accession + ")")
                    .setParameter("fields", "study_accession,fastq_ftp,fastq_aspera,fastq_galaxy")
                    .setParameter("result", "analysis")
                    .setParameter("limit", "0")
                    .setParameter("format", "json")
                    .build();
            ResponseEntity<JsonNode> files = execute(x -> restTemplate.getForEntity(uri, JsonNode.class));

            for (JsonNode node : files.getBody()) {
                result.addAll(Arrays.asList(node.get("fastq_ftp").asText().split(";")));
            /*String fastqAspera = node.get("fastq_aspera").asText();
            String fastqGalaxy = node.get("fastq_galaxy").asText();*/
            }
            //getFiles(uri);
        }
        catch(URISyntaxException ex){

        }
        return result;
    }

    public Set<String> getAssemblyFiles(String accession) throws IOException{
        Set<String> result = new HashSet<>();
        try{
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost(ENA_ENDPOINT)
                    .setPath("/search")
                    .setParameter("query", "(study_accession=" + accession + ")")
                    .setParameter("fields", "study_accession,fastq_ftp,fastq_aspera,fastq_galaxy")
                    .setParameter("result", "assembly")
                    .setParameter("limit", "0")
                    .setParameter("format", "json")
                    .build();
            ResponseEntity<JsonNode> files = execute(x -> restTemplate.getForEntity(uri, JsonNode.class));

            for (JsonNode node : files.getBody()) {
                result.addAll(Arrays.asList(node.get("fastq_ftp").asText().split(";")));
            /*String fastqAspera = node.get("fastq_aspera").asText();
            String fastqGalaxy = node.get("fastq_galaxy").asText();*/
            }
           // getFiles(uri);
        }
        catch(URISyntaxException ex){

        }
        return result;
    }

    public Set<String> getWgsFiles(String accession) throws IOException{
        Set<String> result = new HashSet<>();
        try{
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost(ENA_ENDPOINT)
                    .setPath("/search")
                    .setParameter("query", "(study_accession=" + accession + ")")
                    .setParameter("fields", "study_accession,fastq_ftp,fastq_aspera,fastq_galaxy")
                    .setParameter("result", "wgs_set")
                    .setParameter("limit", "0")
                    .setParameter("format", "json")
                    .build();
            ResponseEntity<JsonNode> files = execute(x -> restTemplate.getForEntity(uri, JsonNode.class));

            for (JsonNode node : files.getBody()) {
                result.addAll(Arrays.asList(node.get("fastq_ftp").asText().split(";")));
            /*String fastqAspera = node.get("fastq_aspera").asText();
            String fastqGalaxy = node.get("fastq_galaxy").asText();*/
            }
            //getFiles(uri);
        }
        catch(URISyntaxException ex){

        }
        return result;
    }
    @Override
    protected boolean isSupported(String database) {
        return DB.ENA.getDBName().equals(database);
    }

    /*public void getTemplateFiles()
    {
        Set<String> result = new HashSet<>();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://www.ebi.ac.uk/ena/portal/api")
                .path("/search")
                .queryParam("query", "(study_accession=PRJNA215355)")
                .queryParam("fields", "study_accession,fastq_ftp,fastq_aspera,fastq_galaxy")
                .queryParam("result", "read_run")
                .queryParam("limit", "0")
                .queryParam("format", "json");

        URI uriForFiles = builder.build().toUri();

        ResponseEntity<JsonNode> files = execute(x -> restTemplate.getForEntity(uriForFiles, JsonNode.class));

        for (JsonNode node : files.getBody()) {
            result.addAll(Arrays.asList(node.get("fastq_ftp").asText().split(";")));
            *//*String fastqAspera = node.get("fastq_aspera").asText();
            String fastqGalaxy = node.get("fastq_galaxy").asText();*//*
        }

    }*/
}
