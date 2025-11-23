package pt.unl.fct.pds;


import pt.unl.fct.pds.model.Address;
import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.path.CurrentPathSelection;
import pt.unl.fct.pds.path.PathSelection;
import pt.unl.fct.pds.utils.ConsensusParser;
import pt.unl.fct.pds.utils.IpAddressesParser;

import java.util.Arrays;


/**
 * Application for Tor Path Selection alternatives.
 *
 */
public class Project2 {
    public static void main(String[] args) {
        // Parse consensus file.
        String consensusFilePath = "src/consensus_files/consensus_1.txt";
        ConsensusParser consensusParser = new ConsensusParser(consensusFilePath);
        Node[] nodes = consensusParser.parseConsensus();

        // Parse IP addresses file.
        String ipAddressesFilePath = "src/ip_addresses/IPAddresses.txt";
        IpAddressesParser ipAddressesParser = new IpAddressesParser(ipAddressesFilePath);
        Address[] addresses = ipAddressesParser.parseAddresses();

        // For each address ...
        for (int i = 0; i < addresses.length; i++) {
            // ... select path ...
            PathSelection pathSelection = new CurrentPathSelection(nodes, addresses[i]);
            Node[] pathNodes = pathSelection.selectPath();
            int minBandwidth = Arrays.stream(pathNodes)
                                    .mapToInt(Node::getBandwidth)
                                    .min()
                                    .orElse(0);

            // ... and create circuit.
            Circuit circuit = new Circuit(i, pathNodes, minBandwidth);
            System.out.println("Circuit " + circuit.getId() + ":");
            System.out.println("  Guard: " + pathNodes[0].getFingerprint());
            System.out.println("  Middle: " + pathNodes[1].getFingerprint());
            System.out.println("  Exit: " + pathNodes[2].getFingerprint());
            System.out.println("  Min Bandwidth: " + circuit.getMinBandwidth());
            System.out.println("---------------------------");
        }
    }
}
