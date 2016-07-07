package app;

import com.fasterxml.jackson.core.JsonProcessingException;
import domain.parser.BellePouleParser;
import domain.parser.EnGardeParser;
import domain.parser.SportCloudParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.DocumentException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by fabricejeannet on 07/06/2016.
 */
public class App {

    public static void main(String[] args) throws Exception {
        App app = new App();
        app.proceedWithArguments(args);
    }

    private void proceedWithArguments(String[]args) throws CmdLineException, IOException, DocumentException {

        CmdLineParser cmdLineParser = new CmdLineParser(this);

        cmdLineParser.parseArgument(args);

        handleInputFiles();

    }

    private void writeOutputFile(JsonToProcess jsonToProcess) throws IOException {

        if(outputFolder.isDirectory()) {
            File outputFile = new File(outputFolder.getPath().concat(File.separator).concat(getFilenameWithJsonExtension(jsonToProcess.inputFile)));
            logger.info("Writing " + outputFile.getPath());

            Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),jsonToProcess.encoding));

            String json = jsonToProcess.json;
            fw.write(json);
            fw.close();
        } else {
            logger.log(Level.SEVERE, "Output folder is not a directory. Aborting");
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

            JsonToProcess jsonToProcess = parse(file);

            if(outputFolder != null ) writeOutputFile(jsonToProcess);

            if(apiUrl != null)  postJson(jsonToProcess);



        } catch (Exception e) {
            logger.log(Level.SEVERE, "File " + file.getName() + " aborted.");
            e.printStackTrace();
        }
    }


    private void postJson(JsonToProcess jsonToProcess) throws IOException {

        logger.info("Posting " + jsonToProcess.inputFile + " to " + apiUrl);

        CloseableHttpClient httpClient = HttpClients.createDefault();


        HttpPost post = new HttpPost(apiUrl);
        StringEntity postingString = new StringEntity(jsonToProcess.json);//gson.tojson() converts your pojo to json



        CloseableHttpResponse response = null;
        Scanner in = null;
        try
        {
            post.setEntity(postingString);
            post.setEntity(postingString);
            post.setHeader("Content-type", "application/json");

            response = httpClient.execute(post);

            HttpEntity entity = response.getEntity();
            in = new Scanner(entity.getContent());
            while (in.hasNext())
            {
                System.out.println(in.next());

            }
            EntityUtils.consume(entity);
        } finally
        {
            in.close();
            response.close();
        }
    }

    private JsonToProcess parse(File file) throws FileNotFoundException, JsonProcessingException, DocumentException {

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

        parser.parse(file);

        String json = parser.getJson();
        Optional<String> encoding = Optional.ofNullable(parser.getEncoding());


        JsonToProcess jsonToProcess = new JsonToProcess();
        jsonToProcess.inputFile = file.getName();
        jsonToProcess.json = json;
        jsonToProcess.encoding = encoding.orElse(defaultEncoding);

        return jsonToProcess;
    }

    private String getFileExtension(String filename){
        return filename.substring(filename.lastIndexOf('.'), filename.length());
    }



    @Option(name="-i",usage="Input file",metaVar="INPUT")
    private File inputFile = null;

    @Option(name="-w",usage="Output folder",metaVar="OUTPUT")
    private File outputFolder = null;

    @Option(name="-p",usage="Sends the json to the given API via HTTP PUT")
    private String apiUrl;

    @Option(name="-e",usage="Sets the default encoding used if the parser can't get the file encoding. Default encoding is UTF-8.")
    private String defaultEncoding="UTF-8";


    private Logger logger = Logger.getLogger("SportCloud -> ");

}
