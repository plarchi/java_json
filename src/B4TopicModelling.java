import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.pipe.*;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.Map.Entry;

public class B4TopicModelling {
    // B4.1 Main entry point for starting topic modelling
    public static void main(String[] args){
        // B4.1.1 Validate JSON filename argument
        if(args.length !=1){
            System.out.println("Please provide the JSON file name as an argument.");
            System.exit(1);
        }
        String filename = args[0];

        // B4.1.2 Instantiate and launch topic modeling
        B4TopicModelling topicModelling = new B4TopicModelling();
        topicModelling.startTopicModelling(filename);
    }

    // B4.2 Start the topic modelling process
    private void startTopicModelling(String filename){
        System.out.println("Starting topic modelling with file: " + filename);

        JSONIOHelper jsonIO = new JSONIOHelper();
        jsonIO.loadJSON(filename);

        // B4.2.1 Load stop words using JSONIOHelper
        Set<String> stopWords = jsonIO.loadStopWords("stopwords.txt");

        // B4.2.2 Retrieve and filter lemmatized text from JSON
        ConcurrentHashMap<String, String> lemmas = jsonIO.getLemmasFromJSONStructure();
        ConcurrentHashMap<String, String> filteredLemmas = new ConcurrentHashMap<>();

        for (Map.Entry<String, String> entry : lemmas.entrySet()) {
            String filteredText = filterText(entry.getValue(), stopWords);
            filteredLemmas.put(entry.getKey(), filteredText);
        }

        // B4.2.3 Save filtered lemmas to file for topic modeling
        saveLemmasToFile("topicdata.txt", filteredLemmas);

        // B4.2.4 Prompt for the number of topics and CSV filename
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the number of topics you want to generate:");
        int numberOfTopics = scanner.nextInt(); // Read user input for the number of topics

        System.out.println("Please input the topic file name for the modeling result in CSV format:");
        scanner.nextLine();  // Consume newline
        String customFilename = scanner.nextLine().trim();
        if (!customFilename.endsWith(".csv")) {
            customFilename += ".csv";
        }

        // B4.2.5 Run topic modeling with specified number of topics
        runTopicModelling("topicdata.txt", numberOfTopics, 2, 50, customFilename);
    }

    // B4.3 Save lemmas to a temporary file for topic modeling input
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

    // B4.4 Filter stop words from text using a stop words set
    private String filterText(String text, Set<String> stopWords) {
        StringBuilder filteredText = new StringBuilder();
        for (String word : text.split("\\s+")) {
            if (!stopWords.contains(word.toLowerCase())) {
                filteredText.append(word).append(" ");
            }
        }
        return filteredText.toString().trim();
    }

    // B4.5 Run the topic modeling process with the MALLET library
    private void runTopicModelling(String flatFile, int nTopics, int nThreads, int nIterations, String outputFile){
        File inputFile = new File(flatFile);

        // B4.5.1 Verify that input file exists and is readable
        if (!inputFile.exists() || !inputFile.canRead()) {
            System.out.println("Error: The specified flatFile does not exist or cannot be read.");
            System.exit(1);
        } else {
            System.out.println("Confirmed: flatFile exists and is readable.");
        }

        // B4.5.2 Print initial lines of flat file for verification
        try (FileInputStream fileInputStream = new FileInputStream(inputFile);
             InputStreamReader fileReader = new InputStreamReader(fileInputStream)) {

            System.out.println("Printing first few lines of the flatFile for verification:");
            java.io.BufferedReader reader = new java.io.BufferedReader(fileReader);
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount < 5) {
                System.out.println(line);
                lineCount++;
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the flatFile.");
            e.printStackTrace();
            System.exit(1);
        }

        // B4.5.3 Prepare data pipes for MALLET instance list
        ArrayList<Pipe> pipeList = new ArrayList<>();
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
        pipeList.add(new TokenSequence2FeatureSequence());
        InstanceList instances = new InstanceList(new SerialPipes(pipeList));

        // B4.5.4 Load instances into MALLET from the flat file
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

        // B4.5.5 Initialize and run the topic modeling algorithm
        try {
            ParallelTopicModel model = new ParallelTopicModel(nTopics, 1.0, 0.01);
            model.addInstances(instances);
            model.setNumThreads(nThreads);
            model.setNumIterations(nIterations);
            model.estimate();

            System.out.println("Topic modeling complete!");
            Object[][] topWords = model.getTopWords(10);

            // New - Save topics to specified CSV file
            saveTopicsToCSV(topWords, outputFile);

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

    // B4.6 Helper method to save topics to a CSV file
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