// package partialorder;

// import cssts.CSSTs;
// import history.Event;
// import java.util.List;

// public class Main {
//     public static void main(String[] args) {
//         // Create a trace and a partial order P
//         List<Event> trace = ...; // Initialize with your trace
//         CSSTs P = ...; // Initialize with your partial order

//         // Compute the closure
//         CSSTs closure = PartialOrderClosure.Closure(trace, P);

//         if (closure == null) {
//             System.out.println("The partial order is not feasible.");
//         } else {
//             System.out.println("The closure of the partial order has been computed.");
//             // Print or process the closure
//         }
//     }
// }

package partialorder;

import history.*;
import cssts.IncrementalCSSTs;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import util.Pair;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Main <inputPath> [-f format]");
            return;
        }

        String format = "plume"; // Default format
        if (args.length >= 3 && args[1].equals("-f")) {
            format = args[2];
        }

      try {
            // Parse the history based on the format
            Path inputPath = Paths.get(args[0]);
            History history;
            if (format.equals("plume")) {
                history = History.parsePlumeHistory(inputPath);
            } else if (format.equals("test")) {
                history = History.parseTestHistory(inputPath);
            } else {
                System.out.println("Invalid format");
                return;
            }

            // Find the closure of the parsed history
            // history = history.reduceHistory();
            IncrementalCSSTs closure = PartialOrderClosure.Closure(history);

            if (closure == null) {
                System.out.println("The partial order is not feasible.");
            } else {
                System.out.println("The closure of the partial order has been computed.");
                
                // Print or process the closure (inefficient)
                // closure.print();
                for (int i1 = 0; i1 < closure.getWidth(); i1++) {
                    for (int t1 = 0; t1 < closure.getChainLength(i1); t1++) {
                        System.out.println("\nSession " + i1 + ", Transaction " + t1 + ": ");
                        int[] predecessors = closure.getPredecessors(new Pair<Integer, Integer>(i1, t1));
                        int[] successors = closure.getSuccessors(new Pair<Integer, Integer>(i1, t1));
                        System.out.println("(Session: Predecessor: Successor): ");
                        for (int i2 = 0; i2 < closure.getWidth(); i2++) {
                            System.out.print("(" + i2 + ":" + predecessors[i2] + ":" + successors[i2] + "), ");
                        }
                    }
                    System.out.println("\n");
                }
            }

        } catch (IOException | ParseHistoryError e) {
            e.printStackTrace();
        }

    }
}