import java.util.concurrent.ConcurrentHashMap;

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
        // Debug message to confirm method call
        System.out.println("Starting to create statistics from: " + filename);

        // Instantiate JSONIOHelper to load JSON data
        JSONIOHelper jsonIO = new JSONIOHelper();
        jsonIO.loadJSON(filename);

        // Create and populate lemmas map
        ConcurrentHashMap<String, String> lemmas = jsonIO.getLemmasFromJSONStructure();

        // Loop through the map to print each lemma entry
        lemmas.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}
