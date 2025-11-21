package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;

import java.util.Arrays;

public class PathSelectionUtils {

    public static int calculateTotalBandwidth(Node[] nodes) {
        return Arrays.stream(nodes)
                .mapToInt(Node::getBandwidth)
                .sum();
    }

    public static CandidateNode[] createCandidateNodes(Node[] nodes, int totalBandwidth) {
        return Arrays.stream(nodes)
                .flatMap(node ->
                        Arrays.stream(new CandidateNode[] {
                                new CandidateNode(node, node.getBandwidth() / totalBandwidth)
                        })
                ).toArray(CandidateNode[]::new);
    }

    public static Node randomWeightedSelection(CandidateNode[] candidateNodes, int totalBandwidth) {
        int randomValue = (int) (Math.random() * totalBandwidth);
        int cumulativeBandwidth = 0;
        for (CandidateNode candidate : candidateNodes) {
            cumulativeBandwidth += candidate.getWeightedBandwidth();
            if (randomValue < cumulativeBandwidth) {
                return candidate.getNode();
            }
        }
        return null;
    }

    public static boolean isSame16Subnet(String ip1, String ip2) {
        String[] ip1Parts = ip1.split("\\.");
        String[] ip2Parts = ip2.split("\\.");

        // Subnet mask for /16: 255.255.0.0
        return ip1Parts[0].equals(ip2Parts[0]) && ip1Parts[1].equals(ip2Parts[1]);
    }

    // TODO("Use library to parse family")
    public static boolean isSameFamily(Node node1, Node node2) {
        return false;
    }
}
