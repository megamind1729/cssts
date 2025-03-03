package history;

import java.util.*;

public class Transaction {
    public List<Event> events;

    public Transaction() {
        this.events = new ArrayList<>();
    }

    public Transaction(List<Event> events) {
        this.events = events;
    }
}