package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;

public class CandidateNode {
    private Node node;
    private double weightedBandwidth;

    public CandidateNode(Node node, double weightedBandwidth) {
        this.node = node;
        this.weightedBandwidth = weightedBandwidth;
    }

    public Node getNode() { return node; }
    public double getWeightedBandwidth() { return weightedBandwidth; }

    public void setNode(Node node) { this.node = node; }
    public void setWeightedBandwidth(double weightedBandwidth) { this.weightedBandwidth = weightedBandwidth; }
}
