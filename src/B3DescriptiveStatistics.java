import java.util.concurrent.ConcurrentHashMap;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;

import static edu.stanford.nlp.semgraph.semgrex.ssurgeon.pred.WordlistTest.TYPE.word;

public class B3DescriptiveStatistics {
    public static void main(String[] args){
        // Ensure filename is provided as an argument
        if (args.length != 1) {
            System.out.println("Please provide the JSON file name as an argument.");
            System.exit(1);
        }
        // Call startCreatingStatistics() with the provided filename
        String filename = args[0];
        B3DescriptiveStatistics stats = new B3DescriptiveStatistics();
        stats.startCreatingStatistics(filename);
    }

    // Load Text placeholder
    public void startCreatingStatistics(String filename){
        System.out.println("Starting to create statistics from: " + filename);

        // Instantiate JSONIOHelper to load JSON data
        JSONIOHelper jsonIO = new JSONIOHelper();
        jsonIO.loadJSON(filename);

        // Try to load lemmas; if empty, load from documents instead
        ConcurrentHashMap<String, String> lemmas = jsonIO.getDocumentsFromJSONStructure();

        // Counting method
        countWordsInCorpus(lemmas);

        // Print directory
        System.out.println("Working directory: " + System.getProperty("user.dir"));
    }

    private void countWordsInCorpus(ConcurrentHashMap<String, String> lemmas){
        ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();

        for(String value : lemmas.values()){
            String[] words = value.split(" ");

            for (String word : words){
                counts.put(word, counts.getOrDefault(word, 0) + 1);
            }
        }

        // print the word counts
        counts.forEach((word, count) -> System.out.println(word + ": " + count));

        // Output counts as CSV file
        try{
            outputCountsAsCSV(counts, "D:\\Waitpet_Onedrive\\OneDrive\\Data_Engineering\\MsC_Edinburgh\\Data_Management_Processing\\Coursework2\\DataPipeline_CW2\\word_count.csv");
        } catch (IOException e) {
            System.out.println("An error occured while saving the CSV file.");
            e.printStackTrace();
        }
    }

    private void outputCountsAsCSV(ConcurrentHashMap<String, Integer> counts, String filename) throws IOException {
        StringBuilder csvOutput = new StringBuilder();

        // Iterate over each CSV content
        for(Entry<String, Integer>entry : counts.entrySet()){
            csvOutput.append(entry.getKey())
                    .append(",")
                    .append(entry.getValue())
                    .append(System.lineSeparator());
        }

        // Write CSV content to file
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(csvOutput.toString());
            System.out.println("Word counts successfully written to " + filename);
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }
    }
}