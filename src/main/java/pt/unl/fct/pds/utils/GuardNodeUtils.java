package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static pt.unl.fct.pds.utils.PathSelectionUtils.calculateTotalBandwidth;
import static pt.unl.fct.pds.utils.PathSelectionUtils.createCandidateNodes;
import static pt.unl.fct.pds.utils.PathSelectionUtils.isSame16Subnet;
import static pt.unl.fct.pds.utils.PathSelectionUtils.isSameFamily;
import static pt.unl.fct.pds.utils.PathSelectionUtils.randomWeightedSelection;

public class GuardNodeUtils {

    // TODO("Persist guard set")
    public static Set<Node> createOrLoadGuardSet(Node[] nodes) {
        Node[] guardNodes = Arrays.stream(nodes)
                .filter(node -> {
                    List<String> flags = Arrays.asList(node.getFlags());
                    return flags.contains("Guard") && flags.contains("Running");
                })
                .toArray(Node[]::new);

        int totalBandwidthGuardNodes = calculateTotalBandwidth(guardNodes);
        CandidateNode[] candidatesGuardNodes = createCandidateNodes(guardNodes, totalBandwidthGuardNodes);

        Set<Node> guardSet = new HashSet<>();
        while (true) {
            guardSet.add(randomWeightedSelection(candidatesGuardNodes, totalBandwidthGuardNodes));
            if (guardSet.size() == 3) {
                return guardSet;
            }
        }
    }

    // TODO("Check guard set empty")
    public static Node[] getGuardSetNodes(Set<Node> guardSet, Node exit) {
        return guardSet.stream()
                .filter(node -> {
                    List<String> flags = Arrays.asList(node.getFlags());
                    return flags.contains("Guard") && flags.contains("Running")
                            && !node.getFingerprint().equals(exit.getFingerprint())
                            && !isSame16Subnet(node.getIpAddress(), exit.getIpAddress())
                            && !isSameFamily(node, exit);
                })
                .toArray(Node[]::new);
    }
}
