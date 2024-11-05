import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

// B3 - JSON Data Handling Block
public class JSONIOHelper {

    // B3.1 Root of the JSON datastore
    JSONObject rootObject;
    // B3.1 Part of the JSON datastore containing full documents
    JSONObject documentsObject;
    // B3.1 Part of the JSON datastore containing cleaned (lemmatised) documents
    JSONObject lemmasObject;

    // B3.2 Method creating an empty datastore structure
    public void createBasicJSONStructure(){
        rootObject = new JSONObject();
        documentsObject = new JSONObject();
        lemmasObject = new JSONObject();
        rootObject.put("documents", documentsObject);
        rootObject.put("lemmas", lemmasObject);
    }

    /**
     * B3.3 Method filling the full documents datastore part
     * @param documents Hashmap from which to get the documents data
     */
    public void addDocumentsToJSONStructure(ConcurrentHashMap<String,String> documents){
        for(Entry<String, String> entry: documents.entrySet()){
            documentsObject.put(entry.getKey(),entry.getValue());
        }
        rootObject.put("documents", documentsObject);
    }

    /**
     * B3.4 Method writing the JSON datastore on file
     * @param filename JSON file on which to write the datastore
     */
    public void saveJSON(String filename){
        String jsonString = rootObject.toJSONString();
        try(FileWriter writer = new FileWriter(filename)){
            writer.write(jsonString);
            System.out.println("JSON save successful!");
        } catch (Exception e){
            System.out.println("JSON save failed!");
        }
    }

    /**
     * B3.5 Method reading the content of a datastore from file
     * @param filename Json file from which to read the datastore content
     */
    public void loadJSON(String filename){
        // B3.5.1 First, create an empty structure
        createBasicJSONStructure();
        try(FileReader file = new FileReader(filename)){
            // B3.5.2 Parse the JSON
            JSONParser parser = new JSONParser();
            rootObject = (JSONObject) parser.parse(file);

            // B3.5.3 Load documents section if it exists
            if(rootObject.get("documents") != null){
                // if documents exist, fill the documentsObject
                documentsObject = (JSONObject) rootObject.get("documents");
            }

            // B3.5.4 Load lemmas section if it exists
            if(rootObject.get("lemmas") != null){
                // if lemmas exist, fill the lemmasObject
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

    /**
     * B3.6 Method to extract the documents from the datastore
     * @return The documents as a Hashmap object (key: doc id, value: text)
     */
    public ConcurrentHashMap<String,String> getDocumentsFromJSONStructure(){
        ConcurrentHashMap<String,String> documents = new ConcurrentHashMap<>();
        for(String key: (Iterable<String>)documentsObject.keySet()){
            documents.put(key, (String)documentsObject.get(key));
        }
        return documents;
    }

    /**
     * B3.7 Method filling the lemmatised documents datastore part
     * @param lemmas Hashmap from which to get the lemmas data
     */
    public void addLemmasToJSONStructure(ConcurrentHashMap<String,String> lemmas){
        for(Entry<String, String> entry: lemmas.entrySet()){
            lemmasObject.put(entry.getKey(),entry.getValue());
        }
        rootObject.put("lemmas", lemmasObject);
    }

    /**
     * B3.8 Method to extract the lemmas from the datastore
     * @return The lemmas as a Hashmap object (key: doc id, value: lemmatised text)
     */
    public ConcurrentHashMap<String,String> getLemmasFromJSONStructure(){
        ConcurrentHashMap<String,String> lemmas = new ConcurrentHashMap<>();
        for(String key: (Iterable<String>)lemmasObject.keySet()){
            lemmas.put(key, (String)lemmasObject.get(key));
        }
        return lemmas;
    }
}