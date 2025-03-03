package history;

import java.util.*;

import history.Key;
import history.ReducedTransaction;
import history.KeyValuePair;

public class ReducedHistory {
    public List<List<ReducedTransaction>> sessions;
    public Set<Key> keys;
    public Map<KeyValuePair, Event.Write> writeMap;
    public Set<KeyValuePair> abortedWrites;

    public ReducedHistory() {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ReducedHistory:\n");
        for (int i = 0; i < sessions.size(); i++) {
            sb.append("Session ").append(i).append(":\n");
            List<ReducedTransaction> transactions = sessions.get(i);
            for (int j = 0; j < transactions.size(); j++) {
                sb.append("  ReducedTransaction ").append(j).append(":\n");
                ReducedTransaction transaction = transactions.get(j);
                sb.append("    First Reads: ").append(transaction.firstReads).append("\n");
                sb.append("    Last Writes: ").append(transaction.lastWrites).append("\n");
            }
        }
        sb.append("Keys: ").append(keys).append("\n");
        sb.append("Write Map: ").append(writeMap).append("\n");
        sb.append("Aborted Writes: ").append(abortedWrites).append("\n");
        return sb.toString();
    }
}