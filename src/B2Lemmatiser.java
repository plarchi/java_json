import edu.stanford.nlp.simple.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// B2 - Text Cleaning and Lemmatisation block
public class B2Lemmatiser {

    // New - B2.1 Stop words
    private Set<String> stopWords;

    // B2.2 Main Lemmatisation block
    public static void main(String[] args) {
        // B2.2.1 Argument validation for input JSON file
        if(args.length != 1){
            System.out.println("Provide one parameter: the JSON file storing data.");
            System.exit(1);
        }
        String datastore = args[0];
        if(!datastore.endsWith(".json")){
            System.out.println("The argument must have a .json extension");
            System.exit(1);
        }

        // B2.2.2 Instantiate and launch lemmatiser
        B2Lemmatiser lemmatiser = new B2Lemmatiser();
        // B2.2.3 Start Lemmatisation process
        lemmatiser.startLemmatisation(datastore);
    }

    // B2.3 Lemmatisation process:
    private void startLemmatisation(String filename){
        System.out.println("Loading data from "+filename);
        // B2.3.1 using a JSONIOHelper to read the documents
        JSONIOHelper jsonIO = new JSONIOHelper();
        jsonIO.loadJSON(filename);

        // New B2.3.2 - Load stopWords
        stopWords = jsonIO.loadStopWords("stopwords.txt");

        // B2.3.3 Retrieving documents from the datastore
        ConcurrentHashMap<String,String> documents = jsonIO.getDocumentsFromJSONStructure();

        // B2.3.4 Lemmatizing documents and storing results in a separate HashMap
        ConcurrentHashMap<String, String> lemmatised = new ConcurrentHashMap<>();
        for (Map.Entry<String, String> entry : documents.entrySet()) {
            // B2.3.5 Lemmatize each document
            String processedText = lemmatiseAndFilter(entry.getValue());
            lemmatised.put(entry.getKey(), processedText);
            System.out.println("Processed document " + entry.getKey() + ": " + processedText);
        }

        // B2.3.6 Adding lemmatised content back into the JSON datastore
        jsonIO.addLemmasToJSONStructure(lemmatised);
        // B2.3.7 Saving updated JSON datastore
        jsonIO.saveJSON(filename);

    }

    // New - B4 filter with JSONIOHelper's filterStopWords method to remove stop words
    private String lemmatiseAndFilter(String text) {
        // B4.1 Clean the text
        text = text.replaceAll("\\p{Punct}", " ")
                .replaceAll("\\s+", " ")
                .trim().toLowerCase();

        // B4.2 Lemmatize the text
        Sentence sentence = new Sentence(text);
        List<String> lemmas = sentence.lemmas();
        System.out.println("Original text: " + text);
        System.out.println("Lemmatized tokens: " + lemmas);

        // B4.3 Filter out stop words in one pass
        StringBuilder filteredText = new StringBuilder();
        for (String lemma : lemmas) {
            if (!stopWords.contains(lemma)) { // Only include non-stop words
                filteredText.append(lemma).append(" ");
            }
        }

        String result = filteredText.toString().trim();
        System.out.println("Filtered text: " + result);
        return result;
    }
}