package uk.ac.ebi.ddi.similarityCalculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import uk.ac.ebi.ddi.annotation.service.DDIDatasetSimilarityService;
import org.apache.commons.cli.*;
import uk.ac.ebi.ddi.annotation.service.DDIExpDataImportService;
import uk.ac.ebi.ddi.service.db.service.similarity.ExpOutputDatasetService;
import uk.ac.ebi.ddi.service.db.service.similarity.TermInDBService;

/**
 * Created by mingze on 30/09/15.
 */

@Component
public class SimilarityCalculator {

    public static void main(String[] args) {

        //For test, avoid inputing argument
        String[] argsExample = {"--dataType=ProteomicsData"};
        args = argsExample;


        TermInDBService termInDBService;
        DDIDatasetSimilarityService ddiDatasetSimilarityService = new DDIDatasetSimilarityService();
        ExpOutputDatasetService expOutputDatasetService = new ExpOutputDatasetService();
        DDIExpDataImportService ddiExpDataImportService = new DDIExpDataImportService();
        MongoTemplate mongoTemplate;

        //JavaBeans
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        termInDBService = (TermInDBService) ctx.getBean("termInDBService");
        ddiDatasetSimilarityService = (DDIDatasetSimilarityService) ctx.getBean("ddiDatasetSimilarityService");
        expOutputDatasetService = (ExpOutputDatasetService) ctx.getBean("expOutputDatasetService");
        ddiExpDataImportService = (DDIExpDataImportService) ctx.getBean("ddiExpDataImportService");
        mongoTemplate = (MongoTemplate) ctx.getBean("mongoTemplate");


        // Definite command line
        CommandLineParser parser = new PosixParser();
        Options options = new Options();

        //Help page
        String helpOpt = "help";
        options.addOption("h", helpOpt, false, "print help message");

        String dataTypeOpt = "dataType";
        Option dataTypeOption = OptionBuilder
                .withLongOpt(dataTypeOpt)
                .withArgName("Omics Data Type")
                .hasArgs()
                .withDescription("ProteomicsData/MetabolomicsData")
                .create();
        options.addOption(dataTypeOption);


        String dataTypeInput = null;

        // create the parser
        try {
            // parse the command line arguments

            CommandLine line = parser.parse(options, args);
            if (line.hasOption(helpOpt) || line.getOptions().length == 0) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("SimilarityCalculator", options);
            } else {
                if (line.hasOption("dataType")) {
                    System.out.println("the option is:" + line.getOptionValue(dataTypeOpt));
                    dataTypeInput = line.getOptionValue(dataTypeOpt);
                }
            }

        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }


        if (dataTypeInput != null) {
            ddiDatasetSimilarityService.calculateIDFWeight(dataTypeInput);
            ddiDatasetSimilarityService.calculateSimilarity(dataTypeInput);
        }
    }

}
