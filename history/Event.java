package history;

import util.Pair;

public abstract class Event {
    public int sessId;
    public int txnId;

    public Pair<Integer, Integer> getSessTxnPair() {
        return new Pair<>(sessId, txnId);
    }

    public static class Read extends Event {
        private KeyValuePair kv;

        public Read(KeyValuePair kv) {
            this.kv = kv;
        }

        public KeyValuePair getKv() {
            return kv;
        }

        @Override
        public String toString() {
            return "(r, " + kv + ", sessId: " + sessId + ", txnId: " + txnId + ")";
        }
    }

    public static class Write extends Event {
        private KeyValuePair kv;

        public Write(KeyValuePair kv) {
            this.kv = kv;
        }

        public KeyValuePair getKv() {
            return kv;
        }

        @Override
        public String toString() {
            return "(w, " + kv + ", sessId: " + sessId + ", txnId: " + txnId + ")";
        }
    }
}