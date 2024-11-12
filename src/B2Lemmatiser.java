import edu.stanford.nlp.simple.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// B2 - Text Cleaning and Lemmatisation block
public class B2Lemmatiser {

    // New - Stop words
    private Set<String> stopWords;

    // B2.1 Main Lemmatisation block
    public static void main(String[] args) {
        // B2.2 Argument validation for input JSON file
        if(args.length != 1){
            System.out.println("Provide one parameter: the JSON file storing data.");
            System.exit(1);
        }
        String datastore = args[0];
        if(!datastore.endsWith(".json")){
            System.out.println("The argument must have a .json extension");
            System.exit(1);
        }

        // B2.3 Instantiate and launch lemmatiser
        B2Lemmatiser lemmatiser = new B2Lemmatiser();
        // B2.4 Start Lemmatisation process
        lemmatiser.startLemmatisation(datastore);
    }

    // B2.4 Lemmatisation process:
    private void startLemmatisation(String filename){
        System.out.println("Loading data from "+filename);
        // B2.5 using a JSONIOHelper to read the documents
        JSONIOHelper jsonIO = new JSONIOHelper();
        jsonIO.loadJSON(filename);

        // New - Load stopWords
        stopWords = jsonIO.loadStopWords("stopwords.txt");

        // B2.6 Retrieving documents from the datastore
        ConcurrentHashMap<String,String> documents = jsonIO.getDocumentsFromJSONStructure();

        // B2.7 Lemmatizing documents and storing results in a separate HashMap
        ConcurrentHashMap<String,String> lemmatised = new ConcurrentHashMap<>();
        for(Map.Entry<String,String> entry: documents.entrySet()){
            // B2.7.1 Lemmatize each document
            lemmatised.put(entry.getKey(), lemmatiseAndFilter(entry.getValue()));
        }
        // B2.8 Adding lemmatised content back into the JSON datastore
        jsonIO.addLemmasToJSONStructure(lemmatised);
        // B2.9 Saving updated JSON datastore
        jsonIO.saveJSON(filename);

    }

    // New - filter with JSONIOHelper's filterStopWords method to remove stop words
    private String lemmatiseAndFilter(String text) {
        text = text.replaceAll("\\p{Punct}", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
        Sentence sentence = new Sentence(text);
        List<String> lemmas = sentence.lemmas();
        String lemmatizedText = String.join(" ", lemmas);

        return new JSONIOHelper().filterStopWords(lemmatizedText);
    }
}