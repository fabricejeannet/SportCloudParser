package app;

import com.fasterxml.jackson.core.JsonProcessingException;
import domain.parser.BellePouleParser;
import domain.parser.EnGardeParser;
import domain.parser.SportCloudParser;
import org.dom4j.DocumentException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;
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

        handleInputFiles();

        if(mustWriteJson) writeOutputFile();

    }

    private void writeOutputFile() throws IOException {
        for (String filename : mapFilenameJson.keySet()) {
            File outputFile = new File(getFilenameWithJsonExtension(filename));
            logger.info("Writing " + outputFile.getName());
            Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), encoding.orElse("UTF-8")));
            fw.write(json);
            fw.close();
        }
    }

    private String getFilenameWithJsonExtension(String filename){
        int lastDotIndex = filename.lastIndexOf('.');
        return filename.substring(0,lastDotIndex).concat(".json");
    }

    private void handleInputFiles() throws JsonProcessingException, FileNotFoundException {

        if(inputFile == null) {
            logger.info("Input file is missing.");
            System.exit(1);
        }

        else if(inputFile.isDirectory()) {
            logger.info("Browsing folder");
            File[] files =  inputFile.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    String extension = getFileExtension(pathname.getName());
                    return extension.equalsIgnoreCase(".xml");
                }
            });
            logger.info(files.length + " xml files found");


            for (File file : files) {
                handleThisFile(file);
            }
        }

        else {
            handleThisFile(inputFile);
        }

    }


    private void handleThisFile(File file){
        try {
            String json = parse(file);
            mapFilenameJson.put(file.getName(), json);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "File " + file.getName() + " aborted.");
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    private String parse(File file) throws FileNotFoundException, JsonProcessingException, DocumentException {

        logger.info("Parsing " + file.getName());

        SportCloudParser parser;

        String filename = file.getName();

        String extension = getFileExtension(filename);


        if(extension.equals(".xml")) {
            logger.info(file.getName() + " is from 'En Garde'");
            parser = EnGardeParser.create();
        } else {
            logger.info(file.getName() + " is from 'Belle Poule'");
            parser = BellePouleParser.create();
        }

        parser.parse(new FileReader(file));

        json = parser.getJson();

        encoding = Optional.ofNullable(parser.getEncoding());
        logger.info("Encoding : " + encoding);

        logger.info(json);

        return json;
    }

    private String getFileExtension(String filename){
        return filename.substring(filename.lastIndexOf('.'), filename.length());
    }

    private HashMap<String, String> mapFilenameJson = new HashMap<String, String>();

    Optional<String> encoding;

    private String json = new String();

    @Option(name="-i",usage="Input file",metaVar="INPUT")
    private File inputFile = null;

    @Option(name="-w",usage="Writes parsed json into a file .json")
    private Boolean mustWriteJson = false;

    @Option(name="-s",usage="Sends the json to the given API via HTTP PUT")
    private String apiUrl;



    private Logger logger = Logger.getLogger("SportCloud -> ");

}
