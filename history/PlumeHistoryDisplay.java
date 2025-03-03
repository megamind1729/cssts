package history;

import java.util.Formatter;
import java.util.List;

public class PlumeHistoryDisplay {
    private History history;

    public PlumeHistoryDisplay(History history) {
        this.history = history;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);

        for (KeyValuePair aborted : history.abortedWrites) {
            formatter.format("w(%d,%d,0,-1)%n", aborted.getKey().getKey(), aborted.getValue().getValue());
        }

        int tIdx = 0;
        for (int sIdx = 0; sIdx < history.sessions.size(); sIdx++) {
            List<Transaction> session = history.sessions.get(sIdx);
            for (Transaction txn : session) {
                for (Event event : txn.events) {
                    if (event instanceof Event.Read) {
                        KeyValuePair kv = ((Event.Read) event).getKv();
                        formatter.format("r(%d,%d,%d,%d)%n", kv.getKey().getKey(), kv.getValue().getValue(), sIdx, tIdx);
                    } else if (event instanceof Event.Write) {
                        KeyValuePair kv = ((Event.Write) event).getKv();
                        formatter.format("w(%d,%d,%d,%d)%n", kv.getKey().getKey(), kv.getValue().getValue(), sIdx, tIdx);
                    }
                }
                tIdx++;
            }
        }

        formatter.close();
        return sb.toString();
    }
}