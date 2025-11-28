package pt.unl.fct.pds;


import pt.unl.fct.pds.model.Address;
import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.path.CurrentPathSelection;
import pt.unl.fct.pds.path.GeoAwarePathSelection;
import pt.unl.fct.pds.path.PathSelection;
import pt.unl.fct.pds.utils.ConsensusParser;
import pt.unl.fct.pds.utils.IpAddressesParser;

import java.util.Arrays;


/**
 * Application for Tor Path Selection alternatives.
 *
 */
public class Project2 {
    public static void main(String[] args) throws Exception {
        // Parse consensus file.
        String consensusFilePath = "src/consensus_files/consensus_1.txt";
        ConsensusParser consensusParser = new ConsensusParser(consensusFilePath);
        Node[] nodes = consensusParser.parseConsensus();

        // Parse IP addresses file.
        String ipAddressesFilePath = "src/ip_addresses/IPAddresses.txt";
        IpAddressesParser ipAddressesParser = new IpAddressesParser(ipAddressesFilePath);
        Address[] addresses = ipAddressesParser.parseAddresses();

        double alpha = 0.5; // guard bonus
        double beta = 0.5;  // middle bonus

        // For each address ...
        for (int i = 0; i < addresses.length; i++) {
            // ... select path ...
            Address address = addresses[i];

            //Alg 1
            PathSelection pathSelection = new CurrentPathSelection(nodes, address);
            Node[] pathNodes = pathSelection.selectPath();
            int minBandwidth = Arrays.stream(pathNodes)
                                    .mapToInt(Node::getBandwidth)
                                    .min()
                                    .orElse(0);

            // ... and create circuit.
            Circuit circuit = new Circuit(i, pathNodes, minBandwidth);

          // --- Algorithm 2: Geography-aware path selection ---
          PathSelection geoPathSelection =
              new GeoAwarePathSelection(nodes, address, alpha, beta);
          Node[] geoPathNodes = geoPathSelection.selectPath();
          int geoMinBw = Arrays.stream(geoPathNodes)
              .mapToInt(Node::getBandwidth)
              .min()
              .orElse(0);
          Circuit geoCircuit = new Circuit(i, geoPathNodes, geoMinBw);

          System.out.println("== Alg 1 ==");
          printCircuit(circuit, address);

          System.out.println("=== Alg 2==");
          printCircuit(geoCircuit, address);

/*          Node guard = pathNodes[0];
            Node middle = pathNodes[1];
            Node exit = pathNodes[2];

            System.out.println("Circuit " + circuit.getId() + " (destination: " + address.toString() + "):");
            System.out.print("  Guard: ");
            printNodeInfo(guard);
            System.out.print("  Middle: ");
            printNodeInfo(middle);
            System.out.print("  Exit: ");
            printNodeInfo(exit);
            System.out.println("  Minimum Bandwidth: " + circuit.getMinBandwidth());
            System.out.println("---------------------------");*/
        }
    }

    private static void printNodeInfo(Node node) {
        System.out.println(node.getNickname() + " " + node.getFingerprint() + " " + node.getIpAddress());
    }

  private static void printCircuit(Circuit circuit, Address address) {
    Node guard = circuit.getNodes()[0];
    Node middle = circuit.getNodes()[1];
    Node exit = circuit.getNodes()[2];

    System.out.println("Circuit " + circuit.getId() + " (destination: " + address.toString() + "):");
    System.out.print("    Guard:  ");
    printNodeInfo(guard);
    System.out.print("    Middle: ");
    printNodeInfo(middle);
    System.out.print("    Exit:   ");
    printNodeInfo(exit);
    System.out.println("    Min Bandwidth: " + circuit.getMinBandwidth());
  }
}
