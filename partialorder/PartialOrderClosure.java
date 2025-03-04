/* 
1. In this particular application of checking transactional consistency, the partial order to be formed is between transactions. This is equivalent to the general case in which there is a single lock l and each transaction starts with a acq(l) event and ends with a rel(l) event. 
2. LockClosure() in the general algorithm, will here only represent that the transactions are in a partial order. That is, if any event e1 of transaction t1 is before any event e2 of t2 in the partial order, then the LockClosure(e1, e2) will add an edge from the last event of t1 to the first event of t2, which is equivalent to the transactions being in a partial order.
*/ 

package partialorder;

import cssts.IncrementalCSSTs;
import history.*;
import util.Pair;

import java.util.*;

public class PartialOrderClosure {

    public static IncrementalCSSTs Closure(History history) {
        
        long startTime = System.nanoTime(); System.err.println("[ Closure ] startTime: " + startTime / 1_000_000.0 + " ms");    
    
        // Initialization

        // System.out.println("\nA." + history); // Debugging

        // i) Queue
        Queue<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> Q = new LinkedList<>();

        // ii) DS
        int[] sessionLengths = history.getSessionLengths();
        int width = sessionLengths.length;
        IncrementalCSSTs DS = new IncrementalCSSTs(sessionLengths);
        assert DS.getWidth() == width;

        // System.out.println("\nB." + history); // Debugging

        // iii) EventMaps
        EventMaps eventMaps = new EventMaps(history);
        System.out.println("\nC." + history); // Debugging
        System.out.println(eventMaps);

        // Push the write-read edges
        long t_curr = System.nanoTime(); System.err.println("[ Closure ] [ " + (t_curr - startTime) / 1_000_000.0 + " ms ] Initialized. Now, pushing wr edges to DS"); long t_prev = t_curr;
        for (int sIdx = 0; sIdx < width; sIdx++) {
            for (int tIdx = 0; tIdx < sessionLengths[sIdx]; tIdx++) {
                Transaction t = history.sessions.get(sIdx).get(tIdx);
                for (Event e : t.events) {
                    // System.out.println("Processing " + e); 
                    // System.out.println("\nD." + history); // Debugging
                    if (e instanceof Event.Read) {
                        KeyValuePair kv = ((Event.Read) e).getKv();
                        Event.Write w = history.writeMap.get(kv);
                        if (w != null) {
                            System.out.println("[ wr edges ]: Inserting edge in DS between " + w + " and " + e); // Debugging
                            Pair<Integer, Integer> sIdx_tIdx = new Pair<>(sIdx, tIdx);
                            if(Objects.equals(w.getSessTxnPair(), sIdx_tIdx)){
                                System.out.println("w and r are in same transaction. History not properly processed."); // Debugging
                            }
                            else if(DS.reachable(sIdx_tIdx, w.getSessTxnPair())){
                                System.out.println("w is reachable from r. Closure not feasible."); // Debugging
                                long endTime = System.nanoTime(); // Profiling
                                System.err.println("[ Closure ] endTime: " + endTime / 1_000_000.0 + " ms, Time taken: " + (endTime - startTime) / 1_000_000.0 + " ms");
                                return null;
                            }
                            else if(DS.reachable(w.getSessTxnPair(), sIdx_tIdx)){
                                System.out.println("r is already reachable from w."); // Debugging
                            }
                            else{
                                Set<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> addedEdges = DS.insertEdge(w.getSessTxnPair(), sIdx_tIdx);
                                System.out.println("Added edges: " + addedEdges); // Debugging
                            }
                        }
                        else {
                            System.out.println("Missing corresponding write of " + kv);
                        }
                    }
                }
            }
        }

        // Push partial-order edges
        t_curr = System.nanoTime(); 
        System.err.println("[ Closure ] [ " + (t_curr - startTime) / 1_000_000.0 + " ms ] Time taken for pushing wr edges: " + (t_curr - t_prev) / 1_000_000.0 + " ms. Now, Starting ObsClosure on initial edges."); t_prev = t_curr;

        for (int sIdx1 = 0; sIdx1 < width; sIdx1++) {
            for(int tIdx1 = 0; tIdx1 < sessionLengths[sIdx1]; tIdx1++) {
                for (int sIdx2 = 0; sIdx2 < width; sIdx2++) {
                    int tIdx2 = DS.getSuccessor(new Pair<>(sIdx1, tIdx1), sIdx2);
                    if (tIdx2 != -1) {
                        Pair<Integer, Integer> t1 = new Pair<>(sIdx1, tIdx1);
                        Pair<Integer, Integer> t2 = new Pair<>(sIdx2, tIdx2);
                        System.out.println("[ po edges ]: Running ObsClosure between " + t1 + " and " + t2); // Debugging
                        ObsClosure(t1, t2, Q, history, eventMaps);
                        // DS.insertEdge(new Pair<>(sIdx1, tIdx1), new Pair<>(sIdx2, tIdx2));   // Don't know why it is required
                    }
                }
            }
        }

        int whileLoopCounter = 0;
        int obsClosureCounter = 0;

        t_curr = System.nanoTime(); long t1 = t_curr;
        System.err.println("[ Closure ] [ " + (t_curr - startTime) / 1_000_000.0 + " ms ] Time taken for initial ObsClosures: " + (t_curr - t_prev) / 1_000_000.0 + " ms. Starting while loop."); t_prev = t_curr;
        
        // Main computation
        while (!Q.isEmpty()) {
            t_curr = System.nanoTime(); System.err.println("[ Closure ] [ " + (t_curr - startTime) / 1_000_000.0 + " ms ] Inside while loop (" + whileLoopCounter + "): " + (t_curr - t_prev) / 1_000_000.0 + " ms"); t_prev = t_curr;
            whileLoopCounter++;
            
            System.out.println("\nQueue elements: " + Q);
            Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> edge = Q.poll();
            Pair<Integer, Integer> e1 = edge.getX();
            Pair<Integer, Integer> e2 = edge.getY();

            if (DS.reachable(e2, e1)) {
                t_curr = System.nanoTime(); // Profiling
                System.err.println("[ Closure ] [ " + (t_curr - startTime) / 1_000_000.0 + " ms ] Ending Closure. Time taken for all while loop iterations: " + (t_curr - t1) / 1_000_000.0 + " ms");
                System.err.println("[ Closure ] While loop iterations: " + whileLoopCounter + ", ObsClosure calls: " + obsClosureCounter);
                return null; // Return ⊥
            }
            else if (Objects.equals(e1, e2)) {
                continue;
            }
            else{
                Set<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> addedEdges = DS.insertEdge(e1, e2);
                System.out.println("Inserted edge in DS between " + e1 + " and " + e2); // Debugging
                System.out.println("Added edges: " + addedEdges); // Debugging

                for (Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> insertedEdge : addedEdges) {
                    obsClosureCounter++;
                    ObsClosure(insertedEdge.getX(), insertedEdge.getY(), Q, history, eventMaps);
                }
            }
        }

        t_curr = System.nanoTime(); // Profiling
        System.err.println("[ Closure ] [ " + (t_curr - startTime) / 1_000_000.0 + " ms ] Ending Closure. Time taken for all while loop iterations: " + (t_curr - t1) / 1_000_000.0 + " ms");
        System.err.println("[ Closure ] While loop iterations: " + whileLoopCounter + ", ObsClosure calls: " + obsClosureCounter);

        return DS; // At this point DS represents the closure of P
    }

    private static void ObsClosure(Pair<Integer, Integer> e1, Pair<Integer, Integer> e2, Queue<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> Q, History history, EventMaps eventMaps) {
        System.out.println("\n[ ObsClosure ]: Processing " + e1 + " and " + e2); // Debugging
        if(Objects.equals(e1, e2)){
            System.out.println("e1 and e2 are equal. Returning out of ObsClosure. "); // Debugging
            return;
        }
        for (Key x : history.keys) {
            System.out.println("Processing " + x); // Debugging
            Event.Write w = null;
            Map<Key, Event.Write> temp2 = eventMaps.beforeW_x.get(e1);
            
            if (temp2 != null) { 
                w = temp2.get(x);
                System.out.println("beforeW_x.get(e1).get(x) : w = " + w);
            }
            if(w != null){
                Event.Read r = null; 
                Map<Key, Event.Read> temp1 = eventMaps.afterR_x.get(e2);
                if (temp1 != null) { 
                    r = temp1.get(x);
                    System.out.println("afterR_x.get(e2).get(x) : r = " + r);
                }
                if(r != null){ 
                    Event.Write O_t_r = history.writeMap.get(((Event.Read) r).getKv());
                    System.out.println("writeMap.get(r.getKv()) : O_t_r = " + O_t_r);
                    if (O_t_r != null) {
                        System.out.println("Inserting edge in Q between " + w + " and " + O_t_r); // Debugging
                        if(Objects.equals(w.getSessTxnPair(), O_t_r.getSessTxnPair())){
                            System.out.println("w and O_t_r are in same transaction."); // Debugging
                        }
                        else{
                            Q.add(new Pair<>(w.getSessTxnPair(), O_t_r.getSessTxnPair()));
                        }
                        System.out.println("Queue elements: " + Q); // Debugging
                    }
                }
                Event.Write w_prime = eventMaps.afterW_x.get(e2).get(x);
                System.out.println("afterW_x.get(e2).get(x) : w_prime = " + w_prime);
                if (w_prime != null) {
                    for (int sIdx = 0; sIdx < history.sessions.size(); sIdx++) {
                        Event.Read fpw = eventMaps.fp.get(sIdx).get(w);
                        System.out.println("sIdx = " + sIdx + ", fp.get(sIdx).get(w) : fpw = " + fpw);
                        if (fpw != null) {
                            System.out.println("Inserting edge in Q between " + fpw + " and " + w_prime); // Debugging
                            Q.add(new Pair<>(fpw.getSessTxnPair(), w_prime.getSessTxnPair()));
                            System.out.println("Queue elements: " + Q); // Debugging
                        }
                    }
                }
            }
        }
    }
}