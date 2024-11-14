import java.util.concurrent.ConcurrentHashMap;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;
import static edu.stanford.nlp.semgraph.semgrex.ssurgeon.pred.WordlistTest.TYPE.word;

public class B3DescriptiveStatistics {
    // B3.1 - Main method to launch Descriptive Statistics
    public static void main(String[] args){
        // B3.1.1 Argument validation for input JSON file
        if (args.length != 1) {
            System.out.println("Please provide the JSON file name as an argument.");
            System.exit(1);
        }

        // B3.1.2 Call startCreatingStatistics() with the provided filename
        String filename = args[0];
        B3DescriptiveStatistics stats = new B3DescriptiveStatistics();
        stats.startCreatingStatistics(filename);
    }

    // B3.2 - Method to start the process of creating statistics
    public void startCreatingStatistics(String filename){
        System.out.println("Starting to create statistics from: " + filename);

        // B3.2.1 Instantiate JSONIOHelper to load JSON data
        JSONIOHelper jsonIO = new JSONIOHelper();
        jsonIO.loadJSON(filename);

        // B3.2.2 Try to load lemmas; if empty, load from documents instead
        ConcurrentHashMap<String, String> lemmas = jsonIO.getDocumentsFromJSONStructure();
        countWordsInCorpus(lemmas);
        countWordsInDocuments(lemmas);
        // B3.2.3 Print directory information
        System.out.println("Working directory: " + System.getProperty("user.dir"));
    }

    // B3.3 Count word frequencies across the entire corpus
    private void countWordsInCorpus(ConcurrentHashMap<String, String> lemmas){
        ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();

        // B3.3.1 Calculate total word frequency for corpus
        for(String value : lemmas.values()){
            String[] words = value.split(" ");
            for (String word : words){
                counts.put(word, counts.getOrDefault(word, 0) + 1);
            }
        }

        // B3.3.2 Print word counts
        counts.forEach((word, count) -> System.out.println(word + ": " + count));

        // B3.3.3 Output word counts to CSV file
        try{
            outputCountsAsCSV(counts, "word_count.csv");
        } catch (IOException e) {
            System.out.println("An error occured while saving the CSV file.");
            e.printStackTrace();
        }
    }

    // B3.4 Count words within each document separately
    private void countWordsInDocuments(ConcurrentHashMap<String,String> lemmas){
        ConcurrentHashMap<String, Integer> documentWordCounts = new ConcurrentHashMap<>();

        // B3.4.1 Calculate word count per document
        for(Entry<String, String>entry : lemmas.entrySet()){
            String[]words = entry.getValue().split(" ");
            documentWordCounts.put(entry.getKey(), words.length);
        }
        documentWordCounts.forEach((doc, count) -> System.out.println(doc + ": " + count));

        // B3.4.2 Write document-level word counts to CSV
        try{
            outputCountsAsCSV(documentWordCounts, "documentWordCount.csv");
            System.out.println("Document word counts successfully written to documentWordCount.csv");
        } catch (IOException e){
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }
    }

    // B3.5 Helper method to output counts to a CSV file
    private void outputCountsAsCSV(ConcurrentHashMap<String, Integer> counts, String filename) throws IOException {
        StringBuilder csvOutput = new StringBuilder();

        // B3.5.1 Add CSV header row
        csvOutput.append("Word,Count").append(System.lineSeparator());

        // B3.5.2 Populate CSV rows with word/count pairs
        for(Entry<String, Integer>entry : counts.entrySet()){
            csvOutput.append(entry.getKey())
                    .append(",")
                    .append(entry.getValue())
                    .append(System.lineSeparator());
        }

        // B3.5.3 Write CSV content to file
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(csvOutput.toString());
            System.out.println("Word counts successfully written to " + filename);
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }
    }
}