package history;

import java.nio.file.Path;

public class ParseHistoryError extends Exception {
    public static class NotADirectory extends ParseHistoryError {
        public NotADirectory(Path path) {
            super("Not a directory: " + path);
        }
    }

    public static class NotAPlumeDirectory extends ParseHistoryError {
        public NotAPlumeDirectory(Path path) {
            super("Not a Plume directory: " + path);
        }
    }

    public static class InvalidPlumeFormat extends ParseHistoryError {
        public InvalidPlumeFormat() {
            super("Invalid Plume format");
        }
    }

    public static class NotUniqueWrites extends ParseHistoryError {
        public NotUniqueWrites(KeyValuePair kv) {
            super("Multiple writes found for the key-value pair: " + kv);
        }
    }

    public static class InternalConsistencyError extends ParseHistoryError {
        public InternalConsistencyError(String message) {
            super("Internal consistency error: " + message);
        }
    }
    
    public ParseHistoryError(String message) {
        super(message);
    }
}