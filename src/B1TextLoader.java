import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ConcurrentHashMap;

// B1 - Loading and Saving from .txt to .json
public class B1TextLoader {

    // B1.1 Map storing documents - key: document ID, value: document text */
    private ConcurrentHashMap<String,String> documents = new ConcurrentHashMap<>();

    // B1.2 Loading and Saving block,
    public static void main(String[] args) {
        // B1.2.1 Argument validation for input/output file names
        if(args.length != 2){
            System.out.println("Provide two parameters: the text file to read and the JSON file to store data.");
            System.exit(1);
        }
        String inFile = args[0];

        // B1.2.2 JSON file name
        String outFile = args[1];
        if(!inFile.endsWith(".txt")){
            System.out.println("The first argument must have a .txt extension");
            System.exit(1);
        }
        if(!outFile.endsWith(".json")){
            System.out.println("The second argument have a .json extension");
            System.exit(1);
        }

        // B1.2.3 Instantiate and launch loader
        B1TextLoader loader = new B1TextLoader();
        // B1.2.4 Loading text .txt file into HashMap
        loader.loadTextFile(inFile);
        // B1.2.5 saveDocumentsToJSON(outFile) to save content into JSON file
        loader.saveDocumentsToJSON(outFile);
    }

    // B1.3 Loading text .txt file into HashMap
    private void loadTextFile(String filename){
        System.out.println("Loading file...");
        File f = new File(filename);
        // B1.3.1 - use a BufferedReader to read file line by lines, each line is a new document
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            int counter = 0;
            while(line != null){
                if(line.trim().length() > 0){
                    documents.put("doc"+counter, line);
                    counter++;
                }
                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            System.out.println("File load failed!");
        }
        System.out.println("Load complete. Documents loaded: " + documents.size());
    }

    // B1.4 - Method to print the number of words in each document (not essential)
    private void countWordsInDocuments(ConcurrentHashMap<String,String> documents){
        documents.forEach(this::countWordsInSingleDocument);
    }

    // B1.5
    private void countWordsInSingleDocument(String key, String value){
        String[] words = value.split(" ");
        System.out.println(key + " has " + words.length + " words");
    }

    // B1.6
    private void saveDocumentsToJSON(String filename){
        JSONIOHelper jsonIO = new JSONIOHelper();
        jsonIO.createBasicJSONStructure();
        jsonIO.addDocumentsToJSONStructure(documents);
        jsonIO.saveJSON(filename);
    }
}