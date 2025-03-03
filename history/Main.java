package history;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.err.println("Usage: java Main <inputPath> <outputPath> [-f format]");
                System.exit(1);
            }

            String format = "plume"; // Default format
            if (args.length >= 3 && args[2].equals("-f")) {
                format = args[3];
            }
    
            Path inputPath = Paths.get(args[0]);
            Path outputPath = Paths.get(args[1]);

            // Parse the test history from the input file
            if (format.equals("plume")) {
                History history = History.parsePlumeHistory(inputPath);
                // History reduced_history = history.reduceHistory();
                // reduced_history.serializePlumeHistory(outputPath);
                // System.out.println("History has been serialized to: " + outputPath.resolve("history.txt"));
            } else if (format.equals("test")) {
                // History history = History.parseTestHistory(inputPath);
                // History reduced_history = history.reduceHistory();
                // reduced_history.serializeTestHistory(outputPath);
                // System.out.println("History has been serialized to: " + outputPath);
            } else {
                System.err.println("Invalid format");
                System.exit(1);
            }
        
        } catch (IOException | ParseHistoryError e) {
            e.printStackTrace();
        }
    }
}