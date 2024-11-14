import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// B5 - JSON Data Handling Block
public class JSONIOHelper {

    // B5.1 Root of the JSON datastore
    JSONObject rootObject;
    // B5.1 Part of the JSON datastore containing full documents
    JSONObject documentsObject;
    // B5.1 Part of the JSON datastore containing cleaned (lemmatised) documents
    JSONObject lemmasObject;
    // B5.2 New Function - Set to store stop words
    private Set<String> stopWords;

    public JSONIOHelper(){
        this.stopWords = loadStopWords("stopwords.txt");
    }

    // B5.2.1 - Method to load stop words, creating a default file if none exists
    Set<String> loadStopWords(String filePath) {
        Set<String> stopWordsSet = new HashSet<>();
        File stopWordsFile = new File(filePath);

        // B5.2.2 Check if stopwords.txt exists; create if not
        if (!stopWordsFile.exists()) {
            System.out.println("stopwords.txt not found. Creating a default one.");
            try (FileWriter writer = new FileWriter(stopWordsFile)) {
                writer.write("the\nis\nin\nand\nof\na\nto\nit\n"); // basic stop words
                System.out.println("Default stopwords.txt created.");
            } catch (IOException e) {
                System.out.println("Failed to create stopwords.txt.");
                e.printStackTrace();
                return stopWordsSet; // Return empty set if creation fails
            }
        }

        // B5.2.3 Load stop words from the file
        try (BufferedReader br = new BufferedReader(new FileReader(stopWordsFile))) {
            String word;
            while ((word = br.readLine()) != null) {
                stopWordsSet.add(word.trim().toLowerCase());
            }
            System.out.println("Stop words loaded successfully. Total stop words: " + stopWordsSet.size());
        } catch (IOException e) {
            System.out.println("Failed to load stop words. Proceeding without stop word removal.");
            e.printStackTrace();
        }
        return stopWordsSet;
    }

    // B5.3 Method creating an empty datastore structure
    public void createBasicJSONStructure(){
        rootObject = new JSONObject();
        documentsObject = new JSONObject();
        lemmasObject = new JSONObject();
        rootObject.put("documents", documentsObject);
        rootObject.put("lemmas", lemmasObject);
    }

    // B5.4 Method filling the full documents datastore part
    public void addDocumentsToJSONStructure(ConcurrentHashMap<String,String> documents){
        for(Entry<String, String> entry: documents.entrySet()){
            documentsObject.put(entry.getKey(),entry.getValue());
        }
        rootObject.put("documents", documentsObject);
    }

    // B5.5 Method writing the JSON datastore on file
    public void saveJSON(String filename){
        String jsonString = rootObject.toJSONString();
        try(FileWriter writer = new FileWriter(filename)){
            writer.write(jsonString);
            System.out.println("JSON save successful!");
        } catch (Exception e){
            System.out.println("JSON save failed!");
        }
    }

    // B5.6 Method reading the content of a datastore from file
    public void loadJSON(String filename){
        createBasicJSONStructure();
        try(FileReader file = new FileReader(filename)){
            // B5.6.1 Parse the JSON
            JSONParser parser = new JSONParser();
            rootObject = (JSONObject) parser.parse(file);

            if(rootObject.get("documents") != null){
                documentsObject = (JSONObject) rootObject.get("documents");
            }

            if(rootObject.get("lemmas") != null){
                lemmasObject = (JSONObject) rootObject.get("lemmas");
            }
            System.out.println("JSON read successful!");
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("JSON read failed!");
        } finally {
            System.out.println("Original documents loaded: "+documentsObject.size()+" - Lemmatised documents loaded: "+lemmasObject.size());
        }
    }

    // B5.7 Method to extract the documents from the datastore
    public ConcurrentHashMap<String,String> getDocumentsFromJSONStructure(){
        ConcurrentHashMap<String,String> documents = new ConcurrentHashMap<>();
        for(String key: (Iterable<String>)documentsObject.keySet()){
            documents.put(key, (String)documentsObject.get(key));
        }
        return documents;
    }

    // B5.8 Method filling the lemmatised documents datastore part
    public void addLemmasToJSONStructure(ConcurrentHashMap<String,String> lemmas){
        for(Entry<String, String> entry: lemmas.entrySet()){
            lemmasObject.put(entry.getKey(),entry.getValue());
        }
        rootObject.put("lemmas", lemmasObject);
    }

    // B5.9 Method to extract the lemmas from the datastore
    public ConcurrentHashMap<String,String> getLemmasFromJSONStructure(){
        ConcurrentHashMap<String,String> lemmas = new ConcurrentHashMap<>();
        for(String key: (Iterable<String>)lemmasObject.keySet()){
            lemmas.put(key, (String)lemmasObject.get(key));
        }
        return lemmas;
    }

    // B5.10 New - Helper method to filter stop words from text
    public String filterStopWords(String text) {
        StringBuilder result = new StringBuilder();
        for (String word : text.split("\\s+")) {
            if (!stopWords.contains(word.toLowerCase())) {
                result.append(word).append(" ");
            }
        }
        return result.toString().trim();
    }
}