package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.CandidateNode;

import java.util.Set;

import static pt.unl.fct.pds.utils.ExitNodeUtils.getExitNodes;
import static pt.unl.fct.pds.utils.GuardNodeUtils.createOrLoadGuardSet;
import static pt.unl.fct.pds.utils.GuardNodeUtils.getGuardSetNodes;
import static pt.unl.fct.pds.utils.MiddleNodeUtils.getMiddleNodes;
import static pt.unl.fct.pds.utils.PathSelectionUtils.calculateTotalBandwidth;
import static pt.unl.fct.pds.utils.PathSelectionUtils.createCandidateNodes;
import static pt.unl.fct.pds.utils.PathSelectionUtils.randomWeightedSelection;

public class CurrentPathSelection implements PathSelection {

    private final Node[] nodes;
    private final String destinationIp;
    private final int destinationPort;

    public CurrentPathSelection(Node[] nodes, String destinationIp, int destinationPort) {
        this.nodes = nodes;
        this.destinationIp = destinationIp;
        this.destinationPort = destinationPort;
    }

    @Override
    public Node[] selectPath() {
        Node exit = selectExit();
        Node guard = selectGuard(exit);
        Node middle = selectMiddle(guard, exit);

        return new Node[]{guard, middle, exit};
    }

    @Override
    public Node selectExit() {
        Node[] exitNodes = getExitNodes(nodes, destinationPort);

        int totalBandwidth = calculateTotalBandwidth(exitNodes);
        CandidateNode[] candidates = createCandidateNodes(exitNodes, totalBandwidth);
        return randomWeightedSelection(candidates, totalBandwidth);
    }

    @Override
    public Node selectGuard(Node exit) {
        Set<Node> guardSet = createOrLoadGuardSet(nodes);
        Node[] guardSetNodes = getGuardSetNodes(guardSet, exit);

        int totalBandwidth = calculateTotalBandwidth(guardSetNodes);
        CandidateNode[] candidates = createCandidateNodes(guardSetNodes, totalBandwidth);
        return randomWeightedSelection(candidates, totalBandwidth);
    }

    @Override
    public Node selectMiddle(Node guard, Node exit) {
        Node[] middleNodes = getMiddleNodes(nodes, guard, exit);

        int totalBandwidth = calculateTotalBandwidth(middleNodes);
        CandidateNode[] candidates = createCandidateNodes(middleNodes, totalBandwidth);
        return randomWeightedSelection(candidates, totalBandwidth);
    }
}
