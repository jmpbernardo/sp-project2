package pt.unl.fct.pds;

import java.util.Set;
import pt.unl.fct.pds.model.Address;
import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.path.CurrentPathSelection;
import pt.unl.fct.pds.path.GeoAwarePathSelection;
import pt.unl.fct.pds.path.PathSelection;
import pt.unl.fct.pds.utils.ConsensusParser;
import pt.unl.fct.pds.utils.IpAddressesParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import static pt.unl.fct.pds.utils.EvaluationUtils.computeShannonEntropy;
import static pt.unl.fct.pds.utils.EvaluationUtils.incrementNodeCount;
import static pt.unl.fct.pds.utils.EvaluationUtils.printCircuit;
import static pt.unl.fct.pds.utils.EvaluationUtils.writeBandwidthResultsToCsv;
import static pt.unl.fct.pds.utils.EvaluationUtils.writeMetricsResultsToCsv;

/**
 * Application for Tor Path Selection alternatives.
 */
public class Project2 {
    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(System.in);

        // Read file paths.
        System.out.print("Enter consensus file path (or ENTER to use the most recent one): ");
        String consensusFilePath = in.nextLine().trim();

        System.out.print("Enter IP addresses file path (or ENTER to use the default one): ");
        String ipAddressesFilePath = in.nextLine().trim();

        // Read parameters for Geography-aware path selection.
        System.out.print("Enter alpha (guard bonus) [default=0.5]: ");
        String alphaLine = in.nextLine().trim();
        double alpha = alphaLine.isEmpty() ? 0.5 : Double.parseDouble(alphaLine); // guard bonus

        System.out.print("Enter beta (middle bonus) [default=0.5]: ");
        String betaLine = in.nextLine().trim();
        double beta = betaLine.isEmpty() ? 0.5 : Double.parseDouble(betaLine); // middle bonus

        // Parse consensus file.
        if (consensusFilePath.isEmpty()) {
            consensusFilePath = loadMostRecentConsensusDocument();
        }
        ConsensusParser consensusParser = new ConsensusParser(consensusFilePath);
        Node[] nodes = consensusParser.parseConsensus();

        // Parse IP addresses file.
        if (ipAddressesFilePath.isEmpty()) {
            ipAddressesFilePath = "src/ip_addresses/IPAddresses.txt";
        }
        IpAddressesParser ipAddressesParser = new IpAddressesParser(ipAddressesFilePath);
        Address[] addresses = ipAddressesParser.parseAddresses();

        // Prepare data structures to store results.
        HashMap<Integer, Integer> bandwidthResultsAlg1 = new HashMap<>();    // circuit id -> min bandwidth
        HashMap<Node, Integer> globalSetAlg1 = new HashMap<>();              // node -> count
        HashMap<Node, Integer> guardSetAlg1 = new HashMap<>();               // node -> count
        HashMap<Node, Integer> middleSetAlg1 = new HashMap<>();              // node -> count
        HashMap<Node, Integer> exitSetAlg1 = new HashMap<>();                // node -> count

        HashMap<Integer, Integer> bandwidthResultsAlg2 = new HashMap<>();   // circuit id -> min bandwidth
        HashMap<Node, Integer> globalSetAlg2 = new HashMap<>();             // node -> count
        HashMap<Node, Integer> guardSetAlg2 = new HashMap<>();              // node -> count
        HashMap<Node, Integer> middleSetAlg2 = new HashMap<>();             // node -> count
        HashMap<Node, Integer> exitSetAlg2 = new HashMap<>();               // node -> count

        // For each address ...
        for (int i = 0; i < addresses.length; i++) {
            Address address = addresses[i];

            // ... execute Algorithm 1: Current path selection ...
            PathSelection pathSelectionAlg1 = new CurrentPathSelection(nodes, address);
            Node[] pathNodesAlg1 = pathSelectionAlg1.selectPath();
            int minBandwidthAlg1 = Arrays.stream(pathNodesAlg1)
                    .mapToInt(Node::getBandwidth)
                    .min()
                    .orElse(0);
            Circuit circuitAlg1 = new Circuit(i, pathNodesAlg1, minBandwidthAlg1);

            // Print and save results of Algorithm 1.
            printCircuit(circuitAlg1, address, "Alg 1");
            bandwidthResultsAlg1.put(i, minBandwidthAlg1);
            Node guardNodeAl1 = pathNodesAlg1[0];
            Node middleNodeAl1 = pathNodesAlg1[1];
            Node exitNodeAl1 = pathNodesAlg1[2];
            incrementNodeCount(globalSetAlg1, guardNodeAl1);
            incrementNodeCount(globalSetAlg1, middleNodeAl1);
            incrementNodeCount(globalSetAlg1, exitNodeAl1);
            incrementNodeCount(guardSetAlg1, guardNodeAl1);
            incrementNodeCount(middleSetAlg1, middleNodeAl1);
            incrementNodeCount(exitSetAlg1, exitNodeAl1);

            // ... and execute Algorithm 2: Geography-aware path selection.
            PathSelection pathSelectionAlg2 = new GeoAwarePathSelection(nodes, address, alpha, beta);
            Node[] pathNodesAlg2 = pathSelectionAlg2.selectPath();
            int minBandwidthAlg2 = Arrays.stream(pathNodesAlg2)
                    .mapToInt(Node::getBandwidth)
                    .min()
                    .orElse(0);
            Circuit geoCircuit = new Circuit(i, pathNodesAlg2, minBandwidthAlg2);

            // Print and save results of Algorithm 2.
            printCircuit(geoCircuit, address, "Alg 2");
            bandwidthResultsAlg2.put(i, minBandwidthAlg2);
            Node guardNodeAl2 = pathNodesAlg2[0];
            Node middleNodeAl2 = pathNodesAlg2[1];
            Node exitNodeAl2 = pathNodesAlg2[2];
            incrementNodeCount(globalSetAlg2, guardNodeAl2);
            incrementNodeCount(globalSetAlg2, middleNodeAl2);
            incrementNodeCount(globalSetAlg2, exitNodeAl2);
            incrementNodeCount(guardSetAlg2, guardNodeAl2);
            incrementNodeCount(middleSetAlg2, middleNodeAl2);
            incrementNodeCount(exitSetAlg2, exitNodeAl2);
        }


        int m = addresses.length;

        // Compute and save results for Algorithm 1.
        double sizeGlobalSetAlg1 = globalSetAlg1.size();
        double entropyGlobalSetAlg1 = computeShannonEntropy(globalSetAlg1, 3.0 * m);
        double sizeGuardSetAlg1 = guardSetAlg1.size();
        double entropyGuardSetAlg1 = computeShannonEntropy(guardSetAlg1, 1.0 * m);
        double sizeMiddleSetAlg1 = middleSetAlg1.size();
        double entropyMiddleSetAlg1 = computeShannonEntropy(middleSetAlg1, 1.0 * m);
        double sizeExitSetAlg1 = exitSetAlg1.size();
        double entropyExitSetAlg1 = computeShannonEntropy(exitSetAlg1, 1.0 * m);

        writeBandwidthResultsToCsv(bandwidthResultsAlg1, "bandwidth_results_alg1.csv");
        writeMetricsResultsToCsv(
                sizeGlobalSetAlg1, entropyGlobalSetAlg1,
                sizeGuardSetAlg1, entropyGuardSetAlg1,
                sizeMiddleSetAlg1, entropyMiddleSetAlg1,
                sizeExitSetAlg1, entropyExitSetAlg1,
                "metrics_results_alg1.csv"
        );

        // Compute and save results for Algorithm 2.
        double sizeGlobalSetAlg2 = globalSetAlg2.size();
        double entropyGlobalSetAlg2 = computeShannonEntropy(globalSetAlg2, 3.0 * m);
        double sizeGuardSetAlg2 = guardSetAlg2.size();
        double entropyGuardSetAlg2 = computeShannonEntropy(guardSetAlg2, 1.0 * m);
        double sizeMiddleSetAlg2 = middleSetAlg2.size();
        double entropyMiddleSetAlg2 = computeShannonEntropy(middleSetAlg2, 1.0 * m);
        double sizeExitSetAlg2 = exitSetAlg2.size();
        double entropyExitSetAlg2 = computeShannonEntropy(exitSetAlg2, 1.0 * m);

        writeBandwidthResultsToCsv(bandwidthResultsAlg2, "bandwidth_results_alg2.csv");
        writeMetricsResultsToCsv(
                sizeGlobalSetAlg2, entropyGlobalSetAlg2,
                sizeGuardSetAlg2, entropyGuardSetAlg2,
                sizeMiddleSetAlg2, entropyMiddleSetAlg2,
                sizeExitSetAlg2, entropyExitSetAlg2,
                "metrics_results_alg2.csv"
        );

      countCountriesSet(globalSetAlg1.keySet(), 1, "global set");
      countCountriesSet(guardSetAlg1.keySet(), 1, "guard set");
      countCountriesSet(middleSetAlg1.keySet(), 1, "middle set");
      countCountriesSet(exitSetAlg1.keySet(), 1, "exit set");

      countCountriesSet(globalSetAlg2.keySet(), 2, "global set");
      countCountriesSet(guardSetAlg2.keySet(), 2, "guard set");
      countCountriesSet(middleSetAlg2.keySet(), 2, "middle set");
      countCountriesSet(exitSetAlg2.keySet(), 2, "exit set");
    }

    private static void countCountriesSet(Set<Node> nodes, int algorithmNumber, String setName) {
      long countriesCount = nodes
          .stream()
          .map(Node::getCountry)
          .filter(country -> !country.equals("Unknown"))
          .distinct()
          .count();

      System.out.println("[Alg " + algorithmNumber + "] Number of distinct countries in the " + setName + ": " + countriesCount);
    }

    private static String loadMostRecentConsensusDocument() throws IOException {
        File consensusDir = new File("src/consensus_files");
        if (!consensusDir.exists()) {
            consensusDir.mkdir();
        }

        // Build filename with timestamp: consensus_YYYYMMDD_HHMMSS.txt
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = now.format(formatter);
        String fileName = "consensus_" + timestamp + ".txt";

        URL url = new URL("http://217.196.147.77/tor/status-vote/current/consensus");
        Path filepath = consensusDir.toPath().resolve(fileName);

        try (InputStream in = url.openStream()) {
            Files.copy(in, filepath);
            System.out.println("[INFO] Downloaded consensus document to: " + filepath);
        } catch (IOException e) {
            System.out.println("[ERROR] Could not download consensus document: " + e.getMessage());
        }

        return filepath.toString();
    }
}
