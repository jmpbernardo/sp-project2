package pt.unl.fct.pds.path;

import static pt.unl.fct.pds.utils.ExitNodeUtils.filterExitNodes;
import static pt.unl.fct.pds.utils.GuardNodeUtils.createOrLoadGuardSet;
import static pt.unl.fct.pds.utils.GuardNodeUtils.filterGuardSetNodes;
import static pt.unl.fct.pds.utils.GuardNodeUtils.geoWeightedCandidates;
import static pt.unl.fct.pds.utils.MiddleNodeUtils.geoWeightedCandidates;
import static pt.unl.fct.pds.utils.MiddleNodeUtils.filterMiddleNodes;
import static pt.unl.fct.pds.utils.PathSelectionUtils.calculateTotalBandwidth;
import static pt.unl.fct.pds.utils.PathSelectionUtils.createCandidateNodes;
import static pt.unl.fct.pds.utils.PathSelectionUtils.randomWeightedSelection;

import java.util.HashSet;
import java.util.Set;

import pt.unl.fct.pds.model.Address;
import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.CandidateNode;

public class GeoAwarePathSelection implements PathSelection {

    private final Node[] nodes;
    private final Address destinationAddress;
    private final double alpha; //bonus for guard
    private final double beta; //bonus for middle

    public GeoAwarePathSelection(Node[] nodes, Address destinationAddress, double alpha,
                                 double beta) {
        this.nodes = nodes;
        this.destinationAddress = destinationAddress;
        this.alpha = alpha;
        this.beta = beta;
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

        Set<String> circuitCountries = new HashSet<>();
        circuitCountries.add(exit.getCountry());

        CandidateNode[] candidates = geoWeightedCandidates(
                guardSetFiltered, circuitCountries, alpha);
        int totalBandwidth = calculateTotalBandwidth(guardSetFiltered);

        return randomWeightedSelection(candidates, totalBandwidth);
    }

    @Override
    public Node selectMiddle(Node guard, Node exit) {
        Node[] middleNodes = filterMiddleNodes(nodes, guard, exit);
        CandidateNode[] candidates = geoWeightedCandidates(
                middleNodes, guard.getCountry(), exit.getCountry(), beta);
        int totalBandwidth = calculateTotalBandwidth(middleNodes);

        return randomWeightedSelection(candidates, totalBandwidth);
    }
}
