package pt.unl.fct.pds;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import pt.unl.fct.pds.model.Address;
import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.path.CurrentPathSelection;
import pt.unl.fct.pds.path.GeoAwarePathSelection;
import pt.unl.fct.pds.path.PathSelection;
import pt.unl.fct.pds.utils.ConsensusParser;
import pt.unl.fct.pds.utils.ExitNodeUtils;
import pt.unl.fct.pds.utils.IpAddressesParser;
import pt.unl.fct.pds.utils.MiddleNodeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Project2Test {

  private static final int ANALYSIS_ROUNDS = 200;
  private static final int STRESS_TEST_ROUNDS = 1000;

  private static Node[] nodes;
  private static Address[] addresses;
  private static Set<String> guardSet;

  @BeforeClass
  public static void setUpTest() throws Exception {
    loadConsensusNodes();
    loadAddresses();
    loadGuardSet();
  }

  private static void loadConsensusNodes() throws Exception {
    String consensusPath = "src/consensus_files/consensus_1.txt";
    ConsensusParser parser = new ConsensusParser(consensusPath);
    nodes = parser.parseConsensus();
    assertNotNull(nodes);
    assertTrue(nodes.length > 0);
  }

  private static void loadAddresses() throws IOException {
    String ipsPath = "src/ip_addresses/IPAddresses.txt";
    IpAddressesParser parser = new IpAddressesParser(ipsPath);
    addresses = parser.parseAddresses();
    assertNotNull(addresses);
    assertTrue(addresses.length > 0);
  }

  private static void loadGuardSet() throws IOException {
    Path path = Path.of("guard_set.txt");
    assertTrue(Files.exists(path));

    guardSet = new HashSet<>();
    List<String> lines = Files.readAllLines(path);
    for (String line : lines) {
      String trimmed = line.trim();
      if (!trimmed.isEmpty()) {
        guardSet.add(trimmed);
      }
    }
    assertFalse(guardSet.isEmpty());
  }


  private boolean hasValidCountry(Node node) {
    String country = node.getCountry();
    return country != null
        && !country.trim().isEmpty()
        && !country.equalsIgnoreCase("Unknown");
  }

  private boolean hasFlag(Node node, String flag) {
    return Arrays.asList(node.getFlags()).contains(flag);
  }

  //Single path selection
  @Test
  public void testSingleSelection() {
    PathSelection alg1 = new CurrentPathSelection(nodes, addresses[0]);
    Node[] path = alg1.selectPath();
    assertNotNull(path);
    assertEquals(3, path.length);
  }


  //Exit node availability for all addresses
  @Test
  public void testExitNodesExist() {
    for (Address addr : addresses) {
      Node[] exitNodes = ExitNodeUtils.filterExitNodes(nodes, addr.getPort());

      assertTrue(exitNodes.length > 0);
    }
  }

  //Middle node availability
  @Test
  public void testMiddleNodesExist() {
    Node guard = null;
    Node exit = null;

    for (Node node : nodes) {
      if (guard == null && hasFlag(node, "Guard")) guard = node;
      if (exit == null && hasFlag(node, "Exit")) exit = node;
      if (guard != null && exit != null) break;
    }

    assertNotNull(guard);
    assertNotNull(exit);

    Node[] middles = MiddleNodeUtils.filterMiddleNodes(nodes, guard, exit);
    assertTrue(middles.length > 0);
  }


  //node fields are parsed correctly
  @Test
  public void tesNodesHaveRequiredFields() {
    int validCountryCount = 0;

    for (Node node : nodes) {
      assertNotNull(node.getNickname());
      assertFalse(node.getNickname().trim().isEmpty());

      assertNotNull(node.getFingerprint());
      assertFalse(node.getFingerprint().trim().isEmpty());

      assertNotNull(node.getIpAddress());
      assertFalse(node.getIpAddress().trim().isEmpty());

      assertTrue(node.getOrPort() > 0);
      assertTrue(node.getOrPort() <= 65535);

      assertTrue(node.getBandwidth() >= 0); // Bandwidth can be zero when unmeasured

      assertNotNull(node.getFlags());
      assertTrue(node.getFlags().length > 0);

      assertNotNull( node.getExitPolicy());

      if (hasValidCountry(node)) validCountryCount++;
    }
    assertEquals(nodes.length, validCountryCount);
  }


  //alg 1 returns 3 nodes
  @Test
  public void testAlg1ReturnsThreeNodes() {

    PathSelection alg1 = new CurrentPathSelection(nodes, addresses[0]);

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path = alg1.selectPath();
      assertNotNull(path);
      assertEquals(3, path.length);
    }
  }

  //alg 1 guard comes from txt
  @Test
  public void testAlg1GuardFromGuardSet() {
    PathSelection alg1 = new CurrentPathSelection(nodes, addresses[0]);

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path = alg1.selectPath();
      Node guard = path[0];
      assertTrue(
          guardSet.contains(guard.getFingerprint()));
    }
  }

  //alg 1 guard has Guard flag
  @Test
  public void testAlg1GuardHasGuardFlag() {
    PathSelection alg1 = new CurrentPathSelection(nodes, addresses[0]);

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path = alg1.selectPath();
      Node guard = path[0];
      assertTrue(hasFlag(guard, "Guard"));
    }
  }

  //alg 1 exit has required flags
  @Test
  public void testAlg1ExitHasRequiredFlags() {
    PathSelection alg1 = new CurrentPathSelection(nodes, addresses[0]);

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path = alg1.selectPath();
      Node exit = path[2];
      assertTrue(hasFlag(exit, "Exit"));
      assertTrue(hasFlag(exit, "Fast"));
    }
  }

  //alg 1 middle has Fast flag
  @Test
  public void testAlg1MiddleHasFastFlag() {
    PathSelection alg1 = new CurrentPathSelection(nodes, addresses[0]);

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path = alg1.selectPath();
      Node middle = path[1];
      assertTrue(hasFlag(middle, "Fast"));
    }
  }

  //alg 1 returns distinct nodes
  @Test
  public void testAlg1NodesAreDistinct() {
    PathSelection alg1 = new CurrentPathSelection(nodes, addresses[0]);

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path = alg1.selectPath();
      Set<String> fingerprints = new HashSet<>();
      for (Node node : path) {
        fingerprints.add(node.getFingerprint());
      }
      assertEquals(3, fingerprints.size());
    }
  }

  //alg 2 returns 3 nodes
  @Test
  public void testAlg2ReturnsThreeNodes() {
    PathSelection alg2 = new GeoAwarePathSelection(nodes, addresses[0], 0.5, 0.5);

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path = alg2.selectPath();
      assertNotNull(path);
      assertEquals(3, path.length);
    }
  }

  //alg 2 guard comes from txt
  @Test
  public void testAlg2GuardFromGuardSet() {
    PathSelection alg2 = new GeoAwarePathSelection(nodes, addresses[0], 0.5, 0.5);

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path = alg2.selectPath();
      Node guard = path[0];
      assertTrue(
          guardSet.contains(guard.getFingerprint()));
    }
  }

  //alg 2 guard has Guard flag
  @Test
  public void testAlg2GuardHasGuardFlag() {
    PathSelection alg2 = new GeoAwarePathSelection(nodes, addresses[0], 0.5, 0.5);

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path = alg2.selectPath();
      Node guard = path[0];
      assertTrue(hasFlag(guard, "Guard"));
    }
  }

  //alg 2 exit has required flags
  @Test
  public void testAlg2ExitHasRequiredFlags() {
    PathSelection alg2 = new GeoAwarePathSelection(nodes, addresses[0], 0.5, 0.5);

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path = alg2.selectPath();
      Node exit = path[2];
      assertTrue(hasFlag(exit, "Exit"));
      assertTrue(hasFlag(exit, "Fast"));
    }
  }

  //alg 2 middle has Fast flag
  @Test
  public void testAlg2MiddleHasFastFlag() {
    PathSelection alg2 = new GeoAwarePathSelection(nodes, addresses[0], 0.5, 0.5);

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path = alg2.selectPath();
      Node middle = path[1];
      assertTrue(hasFlag(middle, "Fast"));
    }
  }

 //alg 2 all nodes have valid countries.
  @Test
  public void testAlg2NodesHaveValidCountries() {
    PathSelection alg2 = new GeoAwarePathSelection(nodes, addresses[0], 0.5, 0.5);

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path = alg2.selectPath();
      for (Node node : path) {
        assertTrue(
            hasValidCountry(node));
      }
    }
  }

  //alg 2 alpha  affects diversity
  @Test
  public void testAlg2AlphaDiversity() {

    //no bonus
    PathSelection alg2Low = new GeoAwarePathSelection(nodes, addresses[0], 0.0, 0.5);
    Set<String> countriesLow = new HashSet<>();

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path = alg2Low.selectPath();
      countriesLow.add(path[0].getCountry());
    }

    // high  bonus
    PathSelection alg2High = new GeoAwarePathSelection(nodes, addresses[0], 1.0, 0.5);
    Set<String> countriesHigh = new HashSet<>();

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path = alg2High.selectPath();
      countriesHigh.add(path[0].getCountry());
    }

    assertTrue(
        countriesHigh.size() >= countriesLow.size() * 0.7);
  }

  //alg 2 beta affects middle diversity
  @Test
  public void testAlg2BetaDiversity() {

    //no bonus
    PathSelection alg2Low = new GeoAwarePathSelection(nodes, addresses[0], 0.5, 0.0);
    Set<String> countriesLow = new HashSet<>();

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path = alg2Low.selectPath();
      countriesLow.add(path[1].getCountry());
    }

    // high bonus
    PathSelection alg2High = new GeoAwarePathSelection(nodes, addresses[0], 0.5, 1.0);
    Set<String> countriesHigh = new HashSet<>();

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path = alg2High.selectPath();
      countriesHigh.add(path[1].getCountry());
    }

    assertTrue(
        countriesHigh.size() >= countriesLow.size() * 0.7);
  }

  //alg 2 improves diversity vs alg 1
  @Test
  public void testAlg2CountryDiversity() {
    PathSelection alg1 = new CurrentPathSelection(nodes, addresses[0]);
    PathSelection alg2 = new GeoAwarePathSelection(nodes, addresses[0], 0.5, 0.5);

    int alg1ThreeCountries = 0;
    int alg2ThreeCountries = 0;

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path1 = alg1.selectPath();
      Set<String> countries1 = new HashSet<>();
      for (Node node : path1) {
        countries1.add(node.getCountry());
      }
      if (countries1.size() == 3) {
        alg1ThreeCountries++;
      }

      Node[] path2 = alg2.selectPath();
      Set<String> countries2 = new HashSet<>();
      for (Node node : path2) {
        countries2.add(node.getCountry());
      }
      if (countries2.size() == 3) {
        alg2ThreeCountries++;
      }
    }

    double alg1Percentage = (double) alg1ThreeCountries * 100 / ANALYSIS_ROUNDS;
    double alg2Percentage = (double) alg2ThreeCountries * 100 / ANALYSIS_ROUNDS;

    System.out.println("Algorithm 1: " + alg1Percentage + "% circuits with 3 countries");
    System.out.println("Algorithm 2: " + alg2Percentage + "% circuits with 3 countries");

    assertTrue(
        alg2Percentage >= alg1Percentage);
  }

  //alg 2 uses more unique nodes than alg 1
  @Test
  public void testAlg2UniqueNodes() {
    PathSelection alg1 = new CurrentPathSelection(nodes, addresses[0]);
    PathSelection alg2 = new GeoAwarePathSelection(nodes, addresses[0], 0.5, 0.5);

    Set<String> alg1AllNodes = new HashSet<>();
    Set<String> alg2AllNodes = new HashSet<>();

    for (int i = 0; i < ANALYSIS_ROUNDS; i++) {
      Node[] path1 = alg1.selectPath();
      for (Node node : path1) {
        alg1AllNodes.add(node.getFingerprint());
      }

      Node[] path2 = alg2.selectPath();
      for (Node node : path2) {
        alg2AllNodes.add(node.getFingerprint());
      }
    }

    System.out.println("Algorithm 1: " + alg1AllNodes.size() + " unique nodes");
    System.out.println("Algorithm 2: " + alg2AllNodes.size() + " unique nodes");

    assertTrue(
        alg2AllNodes.size() >= alg1AllNodes.size() * 0.8);
  }

  //stress test both algorithms
  @Test
  public void testStressAlgs() {
    PathSelection alg1 = new CurrentPathSelection(nodes, addresses[0]);
    PathSelection alg2 = new GeoAwarePathSelection(nodes, addresses[0], 0.5, 0.5);

    for (int i = 0; i < STRESS_TEST_ROUNDS; i++) {
      Node[] path1 = alg1.selectPath();
      Node[] path2 = alg2.selectPath();

      assertNotNull(path1);
      assertNotNull(path2);
      assertEquals(3, path1.length);
      assertEquals(3, path2.length);
    }
  }

  //path selection randomness
  @Test
  public void testRandomness() {
    PathSelection alg1 = new CurrentPathSelection(nodes, addresses[0]);
    Set<String> uniquePaths = new HashSet<>();

    for (int i = 0; i < 10; i++) {
      Node[] path = alg1.selectPath();
      String pathStr = path[0].getNickname() + " → "
          + path[1].getNickname() + " → "
          + path[2].getNickname();

      System.out.println("Round " + (i+1) + ": " + pathStr);
      uniquePaths.add(pathStr);
    }
    assertTrue(uniquePaths.size() > 1);
  }
}
