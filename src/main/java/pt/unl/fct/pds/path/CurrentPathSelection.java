package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Address;
import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.CandidateNode;

import java.util.Set;

import static pt.unl.fct.pds.utils.ExitNodeUtils.filterExitNodes;
import static pt.unl.fct.pds.utils.GuardNodeUtils.createOrLoadGuardSet;
import static pt.unl.fct.pds.utils.GuardNodeUtils.filterGuardSetNodes;
import static pt.unl.fct.pds.utils.MiddleNodeUtils.filterMiddleNodes;
import static pt.unl.fct.pds.utils.PathSelectionUtils.calculateTotalBandwidth;
import static pt.unl.fct.pds.utils.PathSelectionUtils.createCandidateNodes;
import static pt.unl.fct.pds.utils.PathSelectionUtils.randomWeightedSelection;

public class CurrentPathSelection implements PathSelection {

    private final Node[] nodes;
    private final Address destinationAddress;

    public CurrentPathSelection(Node[] nodes, Address destinationAddress) {
        this.nodes = nodes;
        this.destinationAddress = destinationAddress;
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
        Node[] exitNodes = filterExitNodes(nodes, destinationAddress.getPort());

        int totalBandwidth = calculateTotalBandwidth(exitNodes);
        CandidateNode[] candidates = createCandidateNodes(exitNodes, totalBandwidth);
        return randomWeightedSelection(candidates, totalBandwidth);
    }

    @Override
    public Node selectGuard(Node exit) {
        Set<Node> guardSet = createOrLoadGuardSet(nodes);
        Node[] guardSetFiltered = filterGuardSetNodes(guardSet, exit, nodes);

        int totalBandwidth = calculateTotalBandwidth(guardSetFiltered);
        CandidateNode[] candidates = createCandidateNodes(guardSetFiltered, totalBandwidth);
        return randomWeightedSelection(candidates, totalBandwidth);
    }

    @Override
    public Node selectMiddle(Node guard, Node exit) {
        Node[] middleNodes = filterMiddleNodes(nodes, guard, exit);

        int totalBandwidth = calculateTotalBandwidth(middleNodes);
        CandidateNode[] candidates = createCandidateNodes(middleNodes, totalBandwidth);
        return randomWeightedSelection(candidates, totalBandwidth);
    }
}
