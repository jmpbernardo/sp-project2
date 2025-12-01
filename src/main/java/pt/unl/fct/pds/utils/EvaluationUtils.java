package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Address;
import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.model.Node;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class EvaluationUtils {
    public static void printCircuit(Circuit circuit, Address address, String algorithm) {
        Node guard = circuit.getNodes()[0];
        Node middle = circuit.getNodes()[1];
        Node exit = circuit.getNodes()[2];

        System.out.println("[" + algorithm + "] " + "Circuit " + circuit.getId() + " (destination: " + address.toString() + "):");
        System.out.print("    Guard:  ");
        printNodeInfo(guard);
        System.out.print("    Middle: ");
        printNodeInfo(middle);
        System.out.print("    Exit:   ");
        printNodeInfo(exit);
        System.out.println("    Min Bandwidth: " + circuit.getMinBandwidth());
    }

    private static void printNodeInfo(Node node) {
        System.out.println(
                node.getFingerprint() + "  "
                        + node.getIpAddress() + "  "
                        + node.getBandwidth() + "  "
                        + node.getCountry()
        );

    }

    public static void incrementNodeCount(HashMap<Node, Integer> map, Node node) {
        map.put(node, map.getOrDefault(node, 0) + 1);
    }

    public static double computeShannonEntropy(Map<Node, Integer> counts, double total) {
        if (total <= 0 || counts.isEmpty()) return 0.0;

        double entropy = 0.0;
        for (int xi : counts.values()) {
            double pxi = xi / total;                            // xi / nt or xi / m
            double log2pxi = Math.log(pxi) / Math.log(2);
            entropy += pxi * log2pxi;
        }
        return -entropy;
    }

    public static void writeBandwidthResultsToCsv(Map<Integer, Integer> results, String filePath) throws IOException {
        try (PrintWriter pw = new PrintWriter(
                Files.newBufferedWriter(Paths.get("src/results/" + filePath)))) {

            pw.println("circuit_id,min_bandwidth");
            results.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> pw.println(e.getKey() + "," + e.getValue()));
        }
    }

    public static void writeMetricsResultsToCsv(
            double sizeGlobal, double entropyGlobal,
            double sizeGuard, double entropyGuard,
            double sizeMiddle, double entropyMiddle,
            double sizeExit, double entropyExit,
            String filePath
    ) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Paths.get("src/results/" + filePath)))) {

            pw.println("set,size,entropy");
            pw.println("Global," + sizeGlobal + "," + entropyGlobal);
            pw.println("Guard," + sizeGuard + "," + entropyGuard);
            pw.println("Middle," + sizeMiddle + "," + entropyMiddle);
            pw.println("Exit," + sizeExit + "," + entropyExit);
        }
    }
}
