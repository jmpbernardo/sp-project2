package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;

import java.util.Arrays;
import java.util.List;

public class ExitNodeUtils {

    public static Node[] filterExitNodes(Node[] nodes, int destinationPort) {
        return Arrays.stream(nodes)
                .filter(node -> {
                    List<String> flags = Arrays.asList(node.getFlags());
                    return flags.contains("Exit") && flags.contains("Fast")
                            && isSuitableExitPolicy(node, destinationPort);
                })
                .toArray(Node[]::new);
    }

    private static boolean isSuitableExitPolicy(Node node, int destinationPort) {
        String exitPolicy = node.getExitPolicy().trim();
        if (exitPolicy.isEmpty()) return false;

        String[] parts = exitPolicy.split("\\s+", 2);
        String condiction = parts[0].toLowerCase(); // "accept" or "reject"
        String ranges = parts[1];                   // 43,53 or 22-34,41-42

        boolean isInRanges = portIsInRanges(ranges, destinationPort);
        if ("accept".equals(condiction)) {
            return isInRanges;
        } else if ("reject".equals(condiction)) {
            return !isInRanges;
        }
        return false;
    }

    private static boolean portIsInRanges(String ranges, int destinationPort) {
        String[] parts = ranges.split(",");
        for (String part : parts) {
            if (part.contains("-")) {
                String[] bounds = part.split("-");
                int lower = Integer.parseInt(bounds[0].trim());
                int upper = Integer.parseInt(bounds[1].trim());
                if (destinationPort >= lower && destinationPort <= upper) {
                    return true;
                }
            } else {
                int port = Integer.parseInt(part);
                if (port == destinationPort) {
                    return true;
                }
            }
        }
        return false;
    }
}
