import edu.stanford.nlp.simple.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// B2 - Text Cleaning and Lemmatisation block
public class B2Lemmatiser {

    /**
     * B2 Main method to launch the Lemmatising block
     * @param args List of arguments, there should be one: the JSON file storing data
     */

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

    /**
     * B2.4 Lemmatisation process:
     * Method launching the lemmatisation process
     * @param filename The datastore from which the get the document data
     */
    private void startLemmatisation(String filename){
        System.out.println("Loading data from "+filename);
        // B2.5 using a JSONIOHelper to read the documents
        JSONIOHelper jsonIO = new JSONIOHelper();
        jsonIO.loadJSON(filename);

        // B2.6 Retrieving documents from the datastore
        ConcurrentHashMap<String,String> documents = jsonIO.getDocumentsFromJSONStructure();

        // B2.7 Lemmatizing documents and storing results in a separate HashMap
        ConcurrentHashMap<String,String> lemmatised = new ConcurrentHashMap<>();
        for(Map.Entry<String,String> entry: documents.entrySet()){
            // B2.7.1 Lemmatize each document
            lemmatised.put(entry.getKey(), lemmatiseSingleDocument(entry.getValue()));
        }
        // B2.8 Adding lemmatised content back into the JSON datastore
        jsonIO.addLemmasToJSONStructure(lemmatised);
        // B2.9 Saving updated JSON datastore
        jsonIO.saveJSON(filename);
    }

    /**
     * B2.7.1 Method lemmatising the content of one document
     * @param text The original document text
     * @return The lemmatised document text
     */
    private String lemmatiseSingleDocument(String text){
        // B2.7.2 Cleaning the text by removing punctuation and normalizing spaces
        text = text.replaceAll("\\p{Punct}", " ");
        text = text.replaceAll("\\s+", " ");
        text = text.trim();
        // B2.7.3 Converting text to lowercase for consistency
        text = text.toLowerCase();

        // B2.7.4 Using Stanford CoreNLP to lemmatize the text
        Sentence sentence = new Sentence(text);
        List<String> lemmas = sentence.lemmas();

        // B2.7.5 Joining lemmatised words back into a single string
        text = String.join(" ", lemmas);
        return text;
    }
}