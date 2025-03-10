package partialorder;

import history.Event;
import history.Key;
import history.KeyValuePair;
import history.ReducedTransaction;
import history.ReducedHistory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Pair;

public class EventMaps {

    public Map<Pair<Integer, Integer>, Map<Key, Event.Write>> afterW_x;
    public Map<Pair<Integer, Integer>, Map<Key, Event.Read>> afterR_x;
    public Map<Pair<Integer, Integer>, Map<Key, Event.Write>> beforeW_x;
    public Map<Pair<Integer, Integer>, Map<Key, Event.Read>> beforeR_x;
    public Map<Integer, Map<Event.Write, Event.Read>> fp;

    public EventMaps(ReducedHistory history) {
        long startTime = System.nanoTime(); System.err.println("\t[ EventMaps ] startTime: " + startTime / 1_000_000.0 + " ms"); 
        long t_prev = startTime; long t_curr = t_prev; // Debugging
        this.afterW_x = computeAfterW_x(history);
        t_curr = System.nanoTime(); System.err.println("\t[ EventMaps ] AfterW_x: " + (t_curr - t_prev) / 1_000_000.0 + " ms"); t_prev = t_curr;
        this.afterR_x = computeAfterR_x(history);
        t_curr = System.nanoTime(); System.err.println("\t[ EventMaps ] AfterR_x: " + (t_curr - t_prev) / 1_000_000.0 + " ms"); t_prev = t_curr;
        this.beforeW_x = computeBeforeW_x(history);
        t_curr = System.nanoTime(); System.err.println("\t[ EventMaps ] BeforeW_x: " + (t_curr - t_prev) / 1_000_000.0 + " ms"); t_prev = t_curr;
        this.beforeR_x = computeBeforeR_x(history);
        t_curr = System.nanoTime(); System.err.println("\t[ EventMaps ] BeforeR_x: " + (t_curr - t_prev) / 1_000_000.0 + " ms"); t_prev = t_curr;
        this.fp = computeFp(history);
        t_curr = System.nanoTime(); System.err.println("\t[ EventMaps ] F_p: " + (t_curr - t_prev) / 1_000_000.0 + " ms"); t_prev = t_curr;

        long endTime = System.nanoTime(); // Debugging
        System.err.println("\t[ EventMaps ] endTime: " + endTime / 1_000_000.0 + " ms. Time taken: " + (endTime - startTime) / 1_000_000.0 + " ms");       

    }
    
    // Returns a map from a transaction to the first write event on the same key, that succeeds it (current transaction included) in the same session
    public static Map<Pair<Integer, Integer>, Map<Key, Event.Write>> computeAfterW_x(ReducedHistory history) {
        
        Map<Pair<Integer, Integer>, Map<Key, Event.Write>> map = new HashMap<>();
        for (int sIdx = 0; sIdx < history.sessions.size(); sIdx++) {
            List<ReducedTransaction> session = history.sessions.get(sIdx);
            Map<Key, Event.Write> lastWriteMap = new HashMap<>();
            for (int tIdx = session.size() - 1; tIdx >= 0; tIdx--) {
                ReducedTransaction transaction = session.get(tIdx);
                map.put(new Pair<>(sIdx, tIdx), new HashMap<>(lastWriteMap));

                for (Map.Entry<Key, Event.Write> entry : transaction.lastWrites.entrySet()) {
                    Key key = entry.getKey();
                    Event.Write writeEvent = entry.getValue();
                    lastWriteMap.put(key, writeEvent);
                }
            }
        }
        return map;
    }

    // public static Map<Pair<Pair<Integer, Integer>, Key>, Event> AfterR_x(History history) {
    //     Map<Pair<Pair<Integer, Integer>, Key>, Event> map = new HashMap<>();
    //     for (int sIdx = 0; sIdx < history.getSessions().size(); sIdx++) {
    //         Session session = history.getSessions().get(sIdx);
    //         for (int tIdx = 0; tIdx < session.getTransactions().size(); tIdx++) {
    //             Transaction transaction = session.getTransactions().get(tIdx);
    //             Event lastRead = null;
    //             for (Event e : transaction.getEvents()) {
    //                 if (e instanceof ReadEvent) {
    //                     Pair<Pair<Integer, Integer>, Key> key = new Pair<>(new Pair<>(sIdx, tIdx), e.getKey());
    //                     if (lastRead != null) {
    //                         map.put(new Pair<>(new Pair<>(sIdx, tIdx), lastRead.getKey()), e);
    //                     }
    //                     lastRead = e;
    //                 }
    //             }
    //         }
    //     }
    //     return map;
    // }

    // Returns a map from a transaction to the first read event on the same key, that succeeds it (current transaction included) in the same session
    public static Map<Pair<Integer, Integer>, Map<Key, Event.Read>> computeAfterR_x(ReducedHistory history) {
        Map<Pair<Integer, Integer>, Map<Key, Event.Read>> map = new HashMap<>();
        for (int sIdx = 0; sIdx < history.sessions.size(); sIdx++) {
            List<ReducedTransaction> session = history.sessions.get(sIdx);
            Map<Key, Event.Read> lastReadMap = new HashMap<>();
            for (int tIdx = session.size() - 1; tIdx >= 0; tIdx--) {
                ReducedTransaction transaction = session.get(tIdx);
                map.put(new Pair<>(sIdx, tIdx), new HashMap<>(lastReadMap));

                for (Map.Entry<Key, Event.Read> entry : transaction.firstReads.entrySet()) {
                    Key key = entry.getKey();
                    Event.Read readEvent = entry.getValue();
                    lastReadMap.put(key, readEvent);
                }
            }
        }
        return map;
    }

    // public static Map<Pair<Pair<Integer, Integer>, Key>, Event> BeforeW_x(History history) {
    //     Map<Pair<Pair<Integer, Integer>, Key>, Event> map = new HashMap<>();
    //     for (int sIdx = 0; sIdx < history.getSessions().size(); sIdx++) {
    //         Session session = history.getSessions().get(sIdx);
    //         for (int tIdx = 0; tIdx < session.getTransactions().size(); tIdx++) {
    //             Transaction transaction = session.getTransactions().get(tIdx);
    //             Event lastWrite = null;
    //             for (int eIdx = transaction.getEvents().size() - 1; eIdx >= 0; eIdx--) {
    //                 Event e = transaction.getEvents().get(eIdx);
    //                 if (e instanceof WriteEvent) {
    //                     Pair<Pair<Integer, Integer>, Key> key = new Pair<>(new Pair<>(sIdx, tIdx), e.getKey());
    //                     if (lastWrite != null) {
    //                         map.put(new Pair<>(new Pair<>(sIdx, tIdx), e.getKey()), lastWrite);
    //                     }
    //                     lastWrite = e;
    //                 }
    //             }
    //         }
    //     }
    //     return map;
    // }

    // Returns a map from a transaction to the first write event on the same key, that preceeds it (current transaction included) in the same session
    public static Map<Pair<Integer, Integer>, Map<Key, Event.Write>> computeBeforeW_x(ReducedHistory history) {
        Map<Pair<Integer, Integer>, Map<Key, Event.Write>> map = new HashMap<>();
        for (int sIdx = 0; sIdx < history.sessions.size(); sIdx++) {
            List<ReducedTransaction> session = history.sessions.get(sIdx);
            Map<Key, Event.Write> lastWriteMap = new HashMap<>();
            for (int tIdx = 0; tIdx < session.size(); tIdx++) {
                ReducedTransaction transaction = session.get(tIdx);
                map.put(new Pair<>(sIdx, tIdx), new HashMap<>(lastWriteMap));

                for (Map.Entry<Key, Event.Write> entry : transaction.lastWrites.entrySet()) {
                    Key key = entry.getKey();
                    Event.Write writeEvent = entry.getValue();
                    lastWriteMap.put(key, writeEvent);
                }
            }
        }
        return map;
    }

    // Returns a map from a transaction to the first read event on the same key, that precedes it (current transaction included) in the same session
    public static Map<Pair<Integer, Integer>, Map<Key, Event.Read>> computeBeforeR_x(ReducedHistory history) {
        Map<Pair<Integer, Integer>, Map<Key, Event.Read>> map = new HashMap<>();
        for (int sIdx = 0; sIdx < history.sessions.size(); sIdx++) {
            List<ReducedTransaction> session = history.sessions.get(sIdx);
            Map<Key, Event.Read> lastReadMap = new HashMap<>();
            for (int tIdx = 0; tIdx < session.size(); tIdx++) {
                ReducedTransaction transaction = session.get(tIdx);
                map.put(new Pair<>(sIdx, tIdx), new HashMap<>(lastReadMap));

                for (Map.Entry<Key, Event.Read> entry : transaction.firstReads.entrySet()) {
                    Key key = entry.getKey();
                    Event.Read readEvent = entry.getValue();
                    lastReadMap.put(key, readEvent);
                }
            }
        }
        return map;
    }

    // Returns a map from a write event to the last read event that reads the same key value pair in all session
    public static Map<Integer, Map<Event.Write, Event.Read>> computeFp(ReducedHistory history) {
        Map<Integer, Map<Event.Write, Event.Read>> map = new HashMap<>();
        for(int sIdx = 0; sIdx < history.sessions.size(); sIdx++) {
            Map<Event.Write, Event.Read> lastReadMap = new HashMap<>();
            List<ReducedTransaction> session = history.sessions.get(sIdx);
            for(int tIdx = 0; tIdx < session.size(); tIdx++) {
                ReducedTransaction transaction = session.get(tIdx);
                
                for (Map.Entry<Key, Event.Read> entry : transaction.firstReads.entrySet()) {
                    Key key = entry.getKey();
                    Event.Read e = entry.getValue();
                    KeyValuePair kv = e.getKv();
                    Event.Write w = history.writeMap.get(kv);
                    if(w != null) {
                        lastReadMap.put(w, e);
                    }
                }
            }
            map.put(sIdx, lastReadMap);
        }
        return map;
    }

    @Override
    public String toString() {
        long startTime = System.nanoTime(); System.err.println("\t[ EventMaps.toString() ] startTime: " + startTime / 1_000_000.0 + " ms");

        StringBuilder sb = new StringBuilder();
        sb.append("AfterW_x:\n");
        for (Map.Entry<Pair<Integer, Integer>, Map<Key, Event.Write>> entry : afterW_x.entrySet()) {
            sb.append("Key: ").append(entry.getKey())
              .append(", Value: ").append(entry.getValue()).append("\n");
        }
        sb.append("AfterR_x:\n");
        for (Map.Entry<Pair<Integer, Integer>, Map<Key, Event.Read>> entry : afterR_x.entrySet()) {
            sb.append("Key: ").append(entry.getKey())
              .append(", Value: ").append(entry.getValue()).append("\n");
        }
        sb.append("BeforeW_x:\n");
        for (Map.Entry<Pair<Integer, Integer>, Map<Key, Event.Write>> entry : beforeW_x.entrySet()) {
            sb.append("Key: ").append(entry.getKey())
              .append(", Value: ").append(entry.getValue()).append("\n");
        }
        sb.append("BeforeR_x:\n");
        for (Map.Entry<Pair<Integer, Integer>, Map<Key, Event.Read>> entry : beforeR_x.entrySet()) {
            sb.append("Key: ").append(entry.getKey())
              .append(", Value: ").append(entry.getValue()).append("\n");
        }
        sb.append("Fp:\n");
        for (Map.Entry<Integer, Map<Event.Write, Event.Read>> entry : fp.entrySet()) {
            sb.append("Key: ").append(entry.getKey())
              .append(", Value: ").append(entry.getValue()).append("\n");
        }

        long endTime = System.nanoTime(); // Debugging
        System.err.println("\t[ EventMaps.toString() ] endTime: " + endTime / 1_000_000.0 + " ms. Time taken: " + (endTime - startTime) / 1_000_000.0 + " ms");

        return sb.toString();
    }
}