package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.unl.fct.pds.utils.PathSelectionUtils.calculateTotalBandwidth;
import static pt.unl.fct.pds.utils.PathSelectionUtils.createCandidateNodes;
import static pt.unl.fct.pds.utils.PathSelectionUtils.isSame16Subnet;
import static pt.unl.fct.pds.utils.PathSelectionUtils.isSameFamily;
import static pt.unl.fct.pds.utils.PathSelectionUtils.randomWeightedSelection;

public class GuardNodeUtils {

    private static final String GUARD_SET_FILENAME = "guard_set.txt";
    private static final Path GUARD_SET_PATH = Path.of(GUARD_SET_FILENAME);

    public static Set<Node> createOrLoadGuardSet(Node[] nodes) {
        // Load
        Set<Node> loaded = loadGuardSet(nodes);
        if (!loaded.isEmpty()) {
            return loaded;
        }

        // Create
        Node[] guardNodes = Arrays.stream(nodes)
                .filter(node -> {
                    List<String> flags = Arrays.asList(node.getFlags());
                    return flags.contains("Guard") && flags.contains("Running");
                })
                .toArray(Node[]::new);

        int totalBandwidthGuardNodes = calculateTotalBandwidth(guardNodes);
        CandidateNode[] candidatesGuardNodes = createCandidateNodes(guardNodes, totalBandwidthGuardNodes);

        Set<Node> guardSet = new HashSet<>();
        while (guardSet.size() < 3) {
            guardSet.add(randomWeightedSelection(candidatesGuardNodes, totalBandwidthGuardNodes));
        }
        saveGuardSet(guardSet);

        return guardSet;
    }

    private static Set<Node> loadGuardSet(Node[] nodes) {
        if (!Files.exists(GUARD_SET_PATH)) {
            return Collections.emptySet();
        }
        Set<Node> result = new HashSet<>();
        try {
            List<String> lines = Files.readAllLines(GUARD_SET_PATH);
            for (String line : lines) {
                String fingerprint = line.trim();
                Arrays.stream(nodes)
                        .filter(node -> node.getFingerprint().equals(fingerprint))
                        .findFirst()
                        .ifPresent(result::add);
            }
            // NODE: The possibility that the nodes might no longer be in consensus or might be unavailable was not considered.
        } catch (IOException e) {
            return Collections.emptySet();
        }
        return result;
    }

    private static void saveGuardSet(Set<Node> guardSet) {
        List<String> fingerprints = guardSet.stream()
                .map(Node::getFingerprint)
                .collect(Collectors.toList());
        try {
            Files.write(
                    GUARD_SET_PATH,
                    fingerprints,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            System.out.println("[ERROR] Could not save guard set to file: " + e.getMessage());
        }
    }

    public static Node[] filterGuardSetNodes(Set<Node> guardSet, Node exit, Node[] nodes) {
        while (true) {
            Node[] guardSetFiltered =  guardSet.stream()
                    .filter(node -> {
                        List<String> flags = Arrays.asList(node.getFlags());
                        return flags.contains("Guard") && flags.contains("Running")
                                && !node.getFingerprint().equals(exit.getFingerprint())
                                && !isSame16Subnet(node.getIpAddress(), exit.getIpAddress())
                                && !isSameFamily(node, exit);
                    })
                    .toArray(Node[]::new);

            // TODO: Confirm how Tor behaves in this case
            if (guardSetFiltered.length == 0) {
                guardSet.clear();
                guardSet.addAll(createOrLoadGuardSet(nodes));
                continue;
            }
            return guardSetFiltered;
        }
    }
}
