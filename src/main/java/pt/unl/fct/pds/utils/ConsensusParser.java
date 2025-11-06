package pt.unl.fct.pds.utils;

import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import pt.unl.fct.pds.model.Node;

public class ConsensusParser {

  private final int N_NODES = 60;
  private String filename;
  private static final String accountId = System.getenv("ACCOUNT_ID");
  private static final String licenseKey = System.getenv("LICENSE_KEY");


  public ConsensusParser() {
  }

  public ConsensusParser(String filename) {
    this.filename = filename;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public Node[] parseConsensus() {
    if (filename == null )
      System.err.println("filename not found");

    List<Node> nodes = new ArrayList<>(N_NODES);
    try (BufferedReader reader = new BufferedReader(new FileReader(filename), 16384)) {
      String line;
      Node currentNode = null;

      while ((line = reader.readLine()) != null) {
        if (line.isEmpty())
          continue;

        char lineType = line.charAt(0);
        switch (lineType) {
          case 'r':
            if (currentNode != null)
              nodes.add(currentNode);

            currentNode = parseNode(line);
            break;

          case 'a':
            continue; //optional

          case 's':
            if (currentNode != null)
              parseFlags(line, currentNode);
            break;

          case 'v':
            if (currentNode != null)
              parseVersion(line, currentNode);
            break;

          case 'p':
            if (currentNode != null) {
              if (line.startsWith("pr"))
                continue; // optional??
              else
                parseExitPolicy(line, currentNode);
            }
            break;

          case 'w':
            if (currentNode != null)
              parseBandwidth(line, currentNode);
            break;


          default:
            break;
        }
      }

      if (currentNode != null)
        nodes.add(currentNode);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return nodes.toArray(new Node[0]);
  }

  public String geolocateIP(String ipAddress) throws IOException {

    try (WebServiceClient client = new WebServiceClient.Builder(Integer.parseInt(accountId), licenseKey).build()) {

      InetAddress ip = InetAddress.getByName(ipAddress);
      CountryResponse response = client.country(ip);

      Country country = response.getCountry();
      return country.getName();
    } catch (GeoIp2Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Node parseNode(String line) throws IOException {
    Node node = new Node();
    String[] parts = line.split(" ");

      // r <NodeNickname> <Fingerprint> <Digest> <Publication_time> <IP Address> <ORPort> <DIRPort>
      node.setNickname(parts[1]);
      node.setFingerprint(parts[2]);

      String timeStr = parts[4];
      //TODO: Check time format ("yyyy-MM-dd HH:mm:ss")
      LocalDateTime time = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
      node.setTimePublished(time);

      String ip = parts[6];
      node.setIpAddress(ip);

      node.setOrPort(Integer.parseInt(parts[7]));
      node.setDirPort(Integer.parseInt(parts[8]));

      String country = geolocateIP(ip);
      node.setCountry(country);

    return node;
  }

    private void parseFlags(String line, Node node) {
      // <Flag1> ... <FlagN>
      String[] parts = line.split(" ");
      String[] flags = Arrays.copyOfRange(parts, 1, parts.length);
      node.setFlags(flags);
    }

    private void parseVersion(String line, Node node) {
      // v <VersionNumber>
      String[] parts = line.split(" ");
      node.setVersion(parts[1]);
    }


    private void parseBandwidth(String line, Node node) {
      // w Bandwidth=<value>
      String[] parts = line.split("=");
      int bandwidth = Integer.parseInt(parts[1].trim());
      node.setBandwidth(bandwidth);
    }

    private void parseExitPolicy(String line, Node node) {
      //p <ExitPolicy>
      String[] parts = line.split(" ");
      node.setVersion(parts[1]);
    }
}
