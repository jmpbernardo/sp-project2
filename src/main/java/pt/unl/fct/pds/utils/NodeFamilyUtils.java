package pt.unl.fct.pds.utils;

import org.apache.commons.codec.binary.Hex;
import org.torproject.descriptor.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class NodeFamilyUtils {

    public static HashMap<String, String[]> getNodeFamilies() throws Exception {
        HashMap<String, String[]> nodeFamilies = new HashMap<>();
        File descriptorsDir = new File("src/server-descriptors");
        loadMostRecentServerDescriptor(descriptorsDir);
        DescriptorReader descriptorReader = DescriptorSourceFactory.createDescriptorReader();
        for (Descriptor descriptorFile : descriptorReader.readDescriptors(descriptorsDir)) {
            if (descriptorFile instanceof ServerDescriptor) {
                ServerDescriptor serverDescriptor = (ServerDescriptor) descriptorFile;
                String fingerprint = serverDescriptor.getFingerprint();
                List<String> familyEntries = serverDescriptor.getFamilyEntries();
                if (fingerprint != null && !fingerprint.isEmpty() && familyEntries != null && !familyEntries.isEmpty()) {
                    String nodeFingerprintBase64 = convertFingerprintToBase64(fingerprint);
                    String[] nodeFamilyBase64 = familyEntries.stream()
                            .map(NodeFamilyUtils::extractHexFingerprint)
                            .filter(Objects::nonNull)
                            .map(fingerprintHex -> {
                                try {
                                    return convertFingerprintToBase64(fingerprintHex);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .distinct()
                            .toArray(String[]::new);
                    nodeFamilies.putIfAbsent(nodeFingerprintBase64, nodeFamilyBase64);
                }
            }
        }
        return nodeFamilies;
    }

    public static String[] getNodeFamily(String fingerprint, HashMap<String, String[]> nodeFamilies) {
        if (nodeFamilies.containsKey(fingerprint)) {
            return nodeFamilies.get(fingerprint);
        }
        return new String[]{fingerprint};
    }

    // NOTE: Note used because performance issues when loading many descriptors.
    private static void loadMostRecentsServerDescriptors(File descriptorsDir) {
        // Delete old descriptors.
        String descriptorsSubDir = "/recent/relay-descriptors/server-descriptors/";
        File[] files = new File(descriptorsDir, descriptorsSubDir).listFiles();
        if (files != null) {
            for (File f : files) f.delete();
        }

        // Download recent server descriptors.
        DescriptorCollector descriptorCollector = DescriptorSourceFactory.createDescriptorCollector();
        descriptorCollector.collectDescriptors(
                "https://collector.torproject.org",                              // Base URL
                new String[] { descriptorsSubDir },                                 // Only server descriptors
                //System.currentTimeMillis() -60 * 60 * 1000,                       // Last hour
                System.currentTimeMillis() - 24L * 60 * 60 * 1000,                  // Last day
                descriptorsDir,                                                     // Local directory
                false                                                               // Do not delete extra local files
        );
    }

    private static void loadMostRecentServerDescriptor(File descriptorsDir) throws IOException {
        // Ensure directory exists.
        if (!descriptorsDir.exists()) {
            descriptorsDir.mkdir();
        }

        // Delete old descriptors.
        File[] files = descriptorsDir.listFiles();
        if (files != null) {
            for (File f : files) f.delete();
        }

        // Download latest server descriptor.
        URL url = new URL("http://217.196.147.77/tor/server/all");
        Path filepath = descriptorsDir.toPath().resolve("server-descriptor");
        try (InputStream in = url.openStream()) {
            Files.copy(in, filepath);
        } catch (IOException e) {
            System.out.println("[ERROR] Could not download server descriptor: " + e.getMessage());
        }
    }

    private static String extractHexFingerprint(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.startsWith("$")) {
            s = s.substring(1);
        }
        s = s.toUpperCase(Locale.ROOT);
        if (!Pattern.compile("[0-9A-F]{40}").matcher(s).matches()) {
            // Not a fingerprint
            return null;
        }
        return s;
    }

    private static String convertFingerprintToBase64(String fingerprintHex) throws Exception {
        byte[] fingerprintBytes = Hex.decodeHex(fingerprintHex.toCharArray());
        return Base64.getEncoder().encodeToString(fingerprintBytes).replace("=", "");
    }
}
