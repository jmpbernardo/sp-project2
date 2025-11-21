package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;

import java.util.Arrays;
import java.util.List;

import static pt.unl.fct.pds.utils.PathSelectionUtils.isSame16Subnet;
import static pt.unl.fct.pds.utils.PathSelectionUtils.isSameFamily;

public class MiddleNodeUtils {

    public static Node[] getMiddleNodes(Node[] nodes, Node guard, Node exit) {
        return Arrays.stream(nodes)
                .filter(node -> {
                    List<String> flags = Arrays.asList(node.getFlags());
                    return flags.contains("Fast")
                            && !node.getFingerprint().equals(guard.getFingerprint())
                            && !isSame16Subnet(node.getIpAddress(), guard.getIpAddress())
                            && !isSameFamily(node, guard)
                            && !node.getFingerprint().equals(exit.getFingerprint())
                            && !isSame16Subnet(node.getIpAddress(), exit.getIpAddress())
                            && !isSameFamily(node, exit);
                })
                .toArray(Node[]::new);
    }
}
