package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;

public class CandidateNode {
    private Node node;
    private int weightedBandwidth;

    public CandidateNode(Node node, int weightedBandwidth) {
        this.node = node;
        this.weightedBandwidth = weightedBandwidth;
    }

    public Node getNode() { return node; }
    public int getWeightedBandwidth() { return weightedBandwidth; }

    public void setNode(Node node) { this.node = node; }
    public void setWeightedBandwidth(int weightedBandwidth) { this.weightedBandwidth = weightedBandwidth; }
}
