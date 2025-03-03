package history;

import java.util.Formatter;
import java.util.List;

public class TestHistoryDisplay {
    private History history;

    public TestHistoryDisplay(History history) {
        this.history = history;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);

        for (int sIdx = 0; sIdx < history.sessions.size(); sIdx++) {
            List<Transaction> session = history.sessions.get(sIdx);
            if (sIdx > 0) {
                formatter.format("=====%n");
            }
            for (int tIdx = 0; tIdx < session.size(); tIdx++) {
                Transaction txn = session.get(tIdx);
                if (tIdx > 0) {
                    formatter.format("-----%n");
                }
                for (Event event : txn.events) {
                    if (event instanceof Event.Read) {
                        KeyValuePair kv = ((Event.Read) event).getKv();
                        formatter.format("r %d %d%n", kv.getKey().getKey(), kv.getValue().getValue());
                    } else if (event instanceof Event.Write) {
                        KeyValuePair kv = ((Event.Write) event).getKv();
                        formatter.format("w %d %d%n", kv.getKey().getKey(), kv.getValue().getValue());
                    }
                }
            }
        }

        formatter.close();
        return sb.toString();
    }
}