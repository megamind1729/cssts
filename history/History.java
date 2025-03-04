package history;

import java.util.*;
// import java.util.function.Consumer;
import java.util.stream.Collectors;

import history.ParseHistoryError.InternalConsistencyError;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.BufferedWriter;
import java.io.IOException;

public class History {

    public List<List<Transaction>> sessions;
    public Set<Key> keys;
    public Map<KeyValuePair, Event.Write> writeMap;
    public Set<KeyValuePair> abortedWrites;

    public History() {
        this.sessions = new ArrayList<>();
        this.keys = new HashSet<>();
        this.writeMap = new HashMap<>();
        this.abortedWrites = new HashSet<>();
    }

    public int[] getSessionLengths() {
        return this.sessions.stream()
                .mapToInt(List::size)
                .toArray();
    }

    // public HistoryChecker checker(ReportMode reportMode, Consumer<ConsistencyViolation> onViolation) {
    //     return new HistoryChecker(this, onViolation, reportMode);
    // }

    // public HistoryStats stats() {
    //     int numSessions = this.sessions.size();
    //     int numTransactions = this.sessions.stream().mapToInt(List::size).sum();
    //     return new HistoryStats(numSessions, numTransactions);
    // }

    public static History parsePlumeHistory(Path path) throws IOException, ParseHistoryError {
        long startTime = System.nanoTime();
        System.err.println("[ parsePlumeHistory ] startTime: " + startTime / 1_000_000.0 + " ms");

        if (!Files.isDirectory(path)) {
            throw new ParseHistoryError.NotADirectory(path);
        }

        List<Path> files = Files.list(path).collect(Collectors.toList());
        if (files.size() != 1) {
            throw new ParseHistoryError.NotAPlumeDirectory(path);
        }

        String contents = Files.readString(files.get(0));
        History history = new History();
        Map<Long, Integer> sessionMap = new HashMap<>();
        Map<Long, Integer> transactionMap = new HashMap<>();
        Set<Key> keys = new HashSet<>();

        for (String line : contents.split("\n")) {
            System.out.println("Parsing Line: " + line);   // Debugging
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            String op = line.substring(0, 1);
            String[] parts = line.substring(2, line.length() - 1).split(",");
            int key = Integer.parseInt(parts[0].trim());
            int val = Integer.parseInt(parts[1].trim());
            long sess = Long.parseLong(parts[2].trim());
            long txn = Long.parseLong(parts[3].trim());

            keys.add(new Key(key));
            KeyValuePair kv = new KeyValuePair(new Key(key), new Value(val));

            if (txn == -1) {
                if (op.equals("w")) {
                    history.abortedWrites.add(kv);
                }
                continue;
            }

            Event event;
            switch (op) {
                case "r":
                    event = new Event.Read(kv);
                    break;
                case "w":
                    event = new Event.Write(kv);
                    break;
                default:
                    throw new ParseHistoryError.InvalidPlumeFormat();
            }

            int sIdx = sessionMap.computeIfAbsent(sess, k -> {
                history.sessions.add(new ArrayList<>());
                return history.sessions.size() - 1;
            });

            int tIdx = transactionMap.computeIfAbsent(txn, k -> {
                history.sessions.get(sIdx).add(new Transaction());
                return history.sessions.get(sIdx).size() - 1;
            });

            event.sessId = sIdx;
            event.txnId = tIdx;
            System.out.println("Event: " + event);   // Debugging

            if(event instanceof Event.Write) {
                if (history.writeMap.containsKey(kv)) {
                    throw new ParseHistoryError.NotUniqueWrites(kv);
                }
                history.writeMap.put(kv, (Event.Write) event);
            }

            history.sessions.get(sIdx).get(tIdx).events.add(event);
        }

        // Adding an initial transaction in which all keys are written with value 0
        // Transaction initTransaction = new Transaction();
        // for (Key key : keys) {
        //     initTransaction.events.add(new Event.Write(new KeyValuePair(key, new Value(0))));
        // }
        // history.sessions.add(Collections.singletonList(initTransaction));

        history.keys = keys;
        
        long endTime = System.nanoTime(); System.err.println("[ parsePlumeHistory ] endTime: " + endTime / 1_000_000.0 + " ms");
        System.err.println("[ parsePlumeHistory ] Time taken: " + (endTime - startTime) / 1_000_000.0 + " ms");

        return history;
    }


    public void serializePlumeHistory(Path path) throws IOException {
        Path filePath = path.resolve("history.txt");
        Files.createDirectories(path);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(new PlumeHistoryDisplay(this).toString());
        }
    }

    public static History parseTestHistory(Path path) throws IOException, ParseHistoryError {
        long startTime = System.nanoTime();
        System.err.println("[ parseTestHistory ] startTime: " + startTime / 1_000_000.0 + " ms");

        String contents = Files.readString(path);
        History history = new History();
        String[] sessionStrings = contents.split("=");
        Set<Key> keys = new HashSet<>();

        int sIdx = -1;
        for (int i = 0; i < sessionStrings.length; i++) {
            String sessionString = sessionStrings[i].trim();
            if (sessionString.isEmpty()) {
                continue;
            }
            sIdx++;

            List<Transaction> transactions = new ArrayList<>();
            String[] transactionStrings = sessionString.split("-");
            
            int tIdx = -1;
            for (int j = 0; j < transactionStrings.length; j++) {
                String transactionString = transactionStrings[j].trim();
                if (transactionString.isEmpty()) {
                    continue;
                }
                tIdx++;
                List<Event> events = new ArrayList<>();
                String[] eventStrings = transactionString.split("\n");

                for (String eventString : eventStrings) {
                    eventString = eventString.trim();
                    System.out.println("Parsing Event: " + eventString);   // Debugging
                    if (eventString.isEmpty()) {
                        continue;
                    }

                    String[] parts = eventString.split("\\s+");
                    String eventType = parts[0];
                    Key key = new Key(Integer.parseInt(parts[1]));
                    Value value = new Value(Integer.parseInt(parts[2]));
                    Event event;

                    switch (eventType) {
                        case "r":
                            event = new Event.Read(new KeyValuePair(key, value));
                            break;
                        case "w":
                            KeyValuePair kv = new KeyValuePair(key, value);
                            if (history.writeMap.containsKey(kv)) {
                                throw new ParseHistoryError.NotUniqueWrites(kv);
                            }
                            event = new Event.Write(kv);
                            history.writeMap.put(kv, (Event.Write) event);
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid event type");
                    }
                    
                    keys.add(key);
                    event.sessId = sIdx;
                    event.txnId = tIdx;
                    System.out.println("Event: " + event);   // Debugging
                    events.add(event);
                }

                transactions.add(new Transaction(events));
            }

            history.sessions.add(transactions);
        }

        history.keys = keys;

        long endTime = System.nanoTime();
        System.err.println("[ parseTestHistory ] endTime: " + endTime / 1_000_000.0 + " ms");
        System.err.println("[ parseTestHistory ] Time taken: " + (endTime - startTime) / 1_000_000.0 + " ms");

        return history;
    }

    public void serializeTestHistory(Path path) throws IOException {
        Path filePath = path;
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(new TestHistoryDisplay(this).toString());
        }
    }

    // Checks Internal Consistency and returns a reduced history, with each transaction having only the last write for each key and only the reads that are not preceded by writes to the same key in the same transaction.
    public History reduceHistory() throws InternalConsistencyError {
        
        long startTime = System.nanoTime();
        System.err.println("[ reduceHistory ] startTime: " + startTime / 1_000_000.0 + " ms");    
    
        History reducedHistory = new History();
        reducedHistory.keys = new HashSet<>(this.keys);
        reducedHistory.abortedWrites = new HashSet<>(this.abortedWrites);

        for (List<Transaction> session : this.sessions) {
            List<Transaction> reducedSession = new ArrayList<>();
            for (Transaction transaction : session) {
                Map<Key, Event.Write> lastWrites = new HashMap<>();
                List<Event> reducedEvents = new ArrayList<>();

                for (Event event : transaction.events) {
                    if (event instanceof Event.Write) {
                        lastWrites.put(((Event.Write) event).getKv().getKey(), (Event.Write) event);
                    } else if (event instanceof Event.Read) {
                        Key key = ((Event.Read) event).getKv().getKey();
                        if (!lastWrites.containsKey(key)) {
                            reducedEvents.add(event);
                        } else {
                            KeyValuePair kv = ((Event.Read) event).getKv();
                            if (!kv.getValue().equals(lastWrites.get(key).getKv().getValue())) {
                                throw new InternalConsistencyError("Read value does not match the last written value in the same transaction.");
                            }
                        }
                    }
                }

                reducedEvents.addAll(lastWrites.values());
                reducedSession.add(new Transaction(reducedEvents));
            }
            reducedHistory.sessions.add(reducedSession);
        }

        long endTime = System.nanoTime();
        System.err.println("[ reduceHistory ] endTime: " + endTime / 1_000_000.0 + " ms");
        System.err.println("[ reduceHistory ] Time taken: " + (endTime - startTime) / 1_000_000.0 + " ms");

        return reducedHistory;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("History:\n");
        for (int i = 0; i < sessions.size(); i++) {
            sb.append("Session ").append(i).append(":\n");
            List<Transaction> transactions = sessions.get(i);
            for (int j = 0; j < transactions.size(); j++) {
                sb.append("  Transaction ").append(j).append(":\n");
                List<Event> events = transactions.get(j).events;
                for (Event event : events) {
                    sb.append("    ").append(event).append("\n");
                }
            }
        }
        sb.append("Keys: ").append(keys).append("\n");
        sb.append("Write Map: ").append(writeMap).append("\n");
        sb.append("Aborted Writes: ").append(abortedWrites).append("\n");
        return sb.toString();
    }
}