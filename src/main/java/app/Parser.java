package app;

import domain.parser.BellePouleParser;
import domain.parser.EnGardeParser;
import domain.parser.SportCloudParser;
import org.dom4j.DocumentException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Created by fabricejeannet on 07/06/2016.
 */
public class Parser {

    public static void main(String[] args) throws Exception {
        Parser parser = new Parser();
        parser.proceedWithArguments(args);
    }

    private void proceedWithArguments(String[]args) throws CmdLineException, IOException, DocumentException {

        CmdLineParser cmdLineParser = new CmdLineParser(this);

        cmdLineParser.parseArgument(args);

        String json = "";
        SportCloudParser parser;
        Optional<String> encoding = null;

        if(inputFile !=null) {

            logger.info("Parsing " + inputFile.getName());


            String filename = inputFile.getName();
            String extension = filename.substring(filename.lastIndexOf('.'), filename.length());


           if(extension.equals(".xml")) {
                logger.info(inputFile.getName() + " is from 'En Garde'");
                parser = EnGardeParser.create();
            } else {
                logger.info(inputFile.getName() + " is from 'Belle Poule'");
                parser = BellePouleParser.create();
            }

            parser.parse(new FileReader(inputFile));

            json = parser.getJson();
            encoding.ofNullable(parser.getEncoding());
            logger.info("Encoding : " + encoding);

            logger.info(json);

        }

        if(outputFile != null) {
            logger.info("Writing " + outputFile.getName());


/*
            Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), encoding.orElse("UTF-8")));
            fw.write(json);
            fw.close();
*/


            FileWriter fw = new FileWriter(outputFile);

            fw.write(json);
            fw.flush();
            fw.close();
        }

    }

    @Option(name="-i",usage="Input file",metaVar="INPUT")
    private File inputFile = null;

    @Option(name="-o",usage="Output file",metaVar="OUPTUT")
    private File outputFile = null;

    private Logger logger = Logger.getLogger("SportCloud -> ");

}
