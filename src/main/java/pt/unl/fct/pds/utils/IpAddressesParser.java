package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Address;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class IpAddressesParser {

    private final Path path;

    public IpAddressesParser(String filePath) {
        this.path = Path.of(filePath);
    }

    public Address[] parseAddresses() {
        List<Address> addresses = new ArrayList<>();
        if (!Files.exists(path)) {
            System.out.println("[Error] Address file does not exist: " + path);
            return null;
        }

        try (Stream<String> lines = Files.lines(path)) {
            lines.map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !line.startsWith("#") || !line.startsWith("//"))
                    .forEach(line -> {
                        String[] parts = line.trim().split(":");
                        Address address = new Address(parts[0], Integer.parseInt(parts[1]));
                        addresses.add(address);
                    });
        } catch (IOException e) {
            System.out.println("[ERROR] Could not read address file: " + e.getMessage());
            return null;
        }
        return addresses.toArray(new Address[0]);
    }
}
