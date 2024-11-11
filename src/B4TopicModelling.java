import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.util.*;
import cc.mallet.pipe.*;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.Map.Entry;


public class B4TopicModelling {
    public static void main(String[] args){
        if(args.length !=1){
            System.out.println("Please provide the JSON file name as an argument.");
            System.exit(1);
        }
        String filename = args[0];

        B4TopicModelling topicModelling = new B4TopicModelling();
        topicModelling.startTopicModelling(filename);
    }

    private void startTopicModelling(String filename){
        System.out.println("Starting topic modelling with file: " + filename);

        JSONIOHelper josnIO = new JSONIOHelper();
        josnIO.loadJSON(filename);

        ConcurrentHashMap<String, String> lemmas = josnIO.getLemmasFromJSONStructure();

        if (lemmas.isEmpty()) {
            System.out.println("No lemmatised data found. Ensure the JSON file contains processed lemmas.");
        } else {
            System.out.println("Lemmatised data loaded successfully. Number of documents: " + lemmas.size());
            saveLemmasToFile("topicdata.txt", lemmas);
        }
        runTopicModelling("topicdata.txt", 5, 2, 50);
    }

    private void saveLemmasToFile(String flatFile, ConcurrentHashMap<String, String> lemmas) {
        try (FileWriter writer = new FileWriter(flatFile)) {
            for (Entry<String, String> entry : lemmas.entrySet()) {
                // Write each line in format: document_id\ten\tdocument_text
                writer.write(entry.getKey() + "\ten\t" + entry.getValue().replace("\n", " ") + System.lineSeparator());
            }
            System.out.println("Lemmas successfully saved to " + flatFile);
        } catch (IOException e) {

            System.out.println("An error occurred while saving lemmas to file.");
            e.printStackTrace();
        }
    }

    private void runTopicModelling(String flatFile, int nTopics, int nThreads, int nIterations){
        // Step 1: Verify that the flatFile exists and is readable
        File inputFile = new File(flatFile);
        if (!inputFile.exists() || !inputFile.canRead()) {
            System.out.println("Error: The specified flatFile does not exist or cannot be read.");
            System.exit(1);
        } else {
            System.out.println("Confirmed: flatFile exists and is readable.");
        }

        // Step 2: Optionally read and print the contents of flatFile for debugging
        try (FileInputStream fileInputStream = new FileInputStream(inputFile);
             InputStreamReader fileReader = new InputStreamReader(fileInputStream)) {

            System.out.println("Printing first few lines of the flatFile for verification:");
            java.io.BufferedReader reader = new java.io.BufferedReader(fileReader);
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount < 5) { // Limit to first 5 lines
                System.out.println(line);
                lineCount++;
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the flatFile.");
            e.printStackTrace();
            System.exit(1);
        }

        // Step 3: Proceed with MALLET loading and topic modeling
        ArrayList<Pipe> pipeList = new ArrayList<>();
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
        pipeList.add(new TokenSequence2FeatureSequence());
        InstanceList instances = new InstanceList(new SerialPipes(pipeList));

        // Load data into MALLET instances
        try (FileInputStream fileInputStream = new FileInputStream(inputFile);
             InputStreamReader fileReader = new InputStreamReader(fileInputStream)) {

            instances.addThruPipe(new CsvIterator(fileReader,
                    Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                    3, 2, 1)); // Group 3 is data, 2 is label, 1 is name

            System.out.println("Data successfully loaded into MALLET for topic modeling.");
            System.out.println("InstanceList size: " + instances.size());

        } catch (Exception e) {
            System.out.println("An error occurred while loading the flat file for topic modeling.");
            e.printStackTrace();
            System.exit(1);
        }

        // Run topic modeling as before
        try {
            ParallelTopicModel model = new ParallelTopicModel(nTopics, 1.0, 0.01);
            model.addInstances(instances);
            model.setNumThreads(nThreads);
            model.setNumIterations(nIterations);
            model.estimate();

            System.out.println("Topic modeling complete!");
            Object[][] topWords = model.getTopWords(10);

            // New - Save topics to CSV file
            saveTopicsToCSV(topWords, "topic_modeling_results.csv");

            for (int i = 0; i < topWords.length; i++) {
                System.out.print("Top words in topic " + i + ": ");
                for (Object word : topWords[i]) {
                    System.out.print(word + " ");
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("An error occurred during the topic modeling process.");
            e.printStackTrace();
        }
    }

    private void saveTopicsToCSV(Object[][] topWords, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("Topic,TopWords\n");
            for (int i = 0; i < topWords.length; i++) {
                StringBuilder line = new StringBuilder("Topic " + i + ",");
                for (Object word : topWords[i]) {
                    line.append(word).append(" ");
                }
                writer.write(line.toString().trim() + "\n");
            }
            System.out.println("Topics successfully saved to " + filename);
        } catch (IOException e) {
            System.out.println("An error occurred while saving topics to CSV.");
            e.printStackTrace();
        }
    }
}