package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;

import java.util.Arrays;
import java.util.List;

import static pt.unl.fct.pds.utils.PathSelectionUtils.isSame16Subnet;
import static pt.unl.fct.pds.utils.PathSelectionUtils.isSameFamily;

public class MiddleNodeUtils {

  public static Node[] filterMiddleNodes(Node[] nodes, Node guard, Node exit) {
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

  public static CandidateNode[] geoWeightedCandidates(
      Node[] middleNodes,
      String guardCountry,
      String exitCountry,
      double beta) {

    CandidateNode[] candidates = new CandidateNode[middleNodes.length];
    int[] weights = new int[middleNodes.length];
    int totalWeight = 0;

    for (int i = 0; i < middleNodes.length; i++) {
      Node node = middleNodes[i];
      int weight = node.getBandwidth(); // Wi
      String nodeCountry = node.getCountry();

      int c;
      if (!nodeCountry.equals(guardCountry) && !nodeCountry.equals(exitCountry)
          && !guardCountry.equals(exitCountry)) {
        c = 3;
      } else if (!nodeCountry.equals(guardCountry) && !nodeCountry.equals(exitCountry)) {
        c = 2;
      } else {
        c = 1;
      }

      weight = (int) (weight * (1 + (beta * c)));
      weights[i] = weight;
      totalWeight += weight;
    }
    for (int i = 0; i < middleNodes.length; i++) {
      double normalized = (double) weights[i] / (double) totalWeight;
      candidates[i] = new CandidateNode(middleNodes[i], normalized);
    }

    return candidates;
  }
}
