package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;

import java.util.Arrays;
import java.util.List;

public class ExitNodeUtils {

    public static Node[] getExitNodes(Node[] nodes, int destinationPort) {
        return Arrays.stream(nodes)
                .filter(node -> {
                    List<String> flags = Arrays.asList(node.getFlags());
                    return flags.contains("Exit") && flags.contains("Fast")
                            && isSuitableExitPolicy(node, destinationPort);
                })
                .toArray(Node[]::new);
    }

    // TODO("Fix parser")
    private static boolean isSuitableExitPolicy(Node node, int destinationPort) {
        return true;
    }
}
