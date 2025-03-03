package partialorder;

import history.Event;
import history.Key;
import history.KeyValuePair;
import history.Transaction;
import history.History;

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

    public EventMaps(History history) {
        this.afterW_x = computeAfterW_x(history);
        this.afterR_x = computeAfterR_x(history);
        this.beforeW_x = computeBeforeW_x(history);
        this.beforeR_x = computeBeforeR_x(history);
        this.fp = computeFp(history);
    }
    
    // Returns a map from a transaction to the first write event on the same key, that succeeds it (current transaction included) in the same session
    public static Map<Pair<Integer, Integer>, Map<Key, Event.Write>> computeAfterW_x(History history) {
        Map<Pair<Integer, Integer>, Map<Key, Event.Write>> map = new HashMap<>();
        for (int sIdx = 0; sIdx < history.sessions.size(); sIdx++) {
            List<Transaction> session = history.sessions.get(sIdx);
            Map<Key, Event.Write> lastWriteMap = new HashMap<>();
            for (int tIdx = session.size() - 1; tIdx >= 0; tIdx--) {
                Transaction transaction = session.get(tIdx);
                for (int eIdx = transaction.events.size() - 1; eIdx >= 0; eIdx--) {
                    Event e = transaction.events.get(eIdx);
                    if(e instanceof Event.Write) {
                        lastWriteMap.put(((Event.Write) e).getKv().getKey(), (Event.Write) e);
                    }
                }
                map.put(new Pair<>(sIdx, tIdx), new HashMap<>(lastWriteMap));
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
    public static Map<Pair<Integer, Integer>, Map<Key, Event.Read>> computeAfterR_x(History history) {
        Map<Pair<Integer, Integer>, Map<Key, Event.Read>> map = new HashMap<>();
        for (int sIdx = 0; sIdx < history.sessions.size(); sIdx++) {
            List<Transaction> session = history.sessions.get(sIdx);
            Map<Key, Event.Read> lastReadMap = new HashMap<>();
            for (int tIdx = session.size() - 1; tIdx >= 0; tIdx--) {
                Transaction transaction = session.get(tIdx);
                for (int eIdx = transaction.events.size() - 1; eIdx >= 0; eIdx--) {
                    Event e = transaction.events.get(eIdx);
                    if(e instanceof Event.Read) {
                        lastReadMap.put(((Event.Read) e).getKv().getKey(), (Event.Read) e);
                    }
                }
                map.put(new Pair<>(sIdx, tIdx), new HashMap<>(lastReadMap));
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
    public static Map<Pair<Integer, Integer>, Map<Key, Event.Write>> computeBeforeW_x(History history) {
        Map<Pair<Integer, Integer>, Map<Key, Event.Write>> map = new HashMap<>();
        for (int sIdx = 0; sIdx < history.sessions.size(); sIdx++) {
            List<Transaction> session = history.sessions.get(sIdx);
            Map<Key, Event.Write> lastWriteMap = new HashMap<>();
            for (int tIdx = 0; tIdx < session.size(); tIdx++) {
                Transaction transaction = session.get(tIdx);
                for (int eIdx = 0; eIdx < transaction.events.size(); eIdx++) {
                    Event e = transaction.events.get(eIdx);
                    if(e instanceof Event.Write) {
                        lastWriteMap.put(((Event.Write) e).getKv().getKey(), (Event.Write) e);
                    }
                }
                map.put(new Pair<>(sIdx, tIdx), new HashMap<>(lastWriteMap));
            }
        }
        return map;
    }

    // Returns a map from a transaction to the first read event on the same key, that preceeds it (current transaction included) in the same session
    public static Map<Pair<Integer, Integer>, Map<Key, Event.Read>> computeBeforeR_x(History history) {
        Map<Pair<Integer, Integer>, Map<Key, Event.Read>> map = new HashMap<>();
        for (int sIdx = 0; sIdx < history.sessions.size(); sIdx++) {
            List<Transaction> session = history.sessions.get(sIdx);
            Map<Key, Event.Read> lastReadMap = new HashMap<>();
            for (int tIdx = 0; tIdx < session.size(); tIdx++) {
                Transaction transaction = session.get(tIdx);
                for (int eIdx = 0; eIdx < transaction.events.size(); eIdx++) {
                    Event e = transaction.events.get(eIdx);
                    if(e instanceof Event.Read) {
                        lastReadMap.put(((Event.Read) e).getKv().getKey(), ((Event.Read) e));
                    }
                }
                map.put(new Pair<>(sIdx, tIdx), new HashMap<>(lastReadMap));
            }
        }
        return map;
    }

    // Returns a map from a write event to the last read event that reads the same key value pair in all session
    public static Map<Integer, Map<Event.Write, Event.Read>> computeFp(History history) {
        Map<Integer, Map<Event.Write, Event.Read>> map = new HashMap<>();
        for(int sIdx = 0; sIdx < history.sessions.size(); sIdx++) {
            Map<Event.Write, Event.Read> lastReadMap = new HashMap<>();
            List<Transaction> session = history.sessions.get(sIdx);
            for(int tIdx = 0; tIdx < session.size(); tIdx++) {
                Transaction transaction = session.get(tIdx);
                for(Event e : transaction.events) {
                    if(e instanceof Event.Read) {
                        KeyValuePair kv = ((Event.Read) e).getKv();
                        Event.Write w = history.writeMap.get(kv);
                        if(w != null) {
                            lastReadMap.put(w, (Event.Read) e);
                        }
                    }
                }
            }
            map.put(sIdx, lastReadMap);
        }
        return map;
    }

    @Override
    public String toString() {
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
        return sb.toString();
    }
}