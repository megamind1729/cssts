package history;

import java.util.*;

public class ReducedTransaction {
    public Map<Key, Event.Read> firstReads;
    public Map<Key, Event.Write> lastWrites;

    public ReducedTransaction(Map<Key, Event.Read> firstReads, Map<Key, Event.Write> lastWrites) {
        this.firstReads = firstReads;
        this.lastWrites = lastWrites;
    }

    @Override
    public String toString() {
        return "ReducedTransaction{" +
                "firstReads=" + firstReads +
                ", lastWrites=" + lastWrites +
                '}';
    }
}