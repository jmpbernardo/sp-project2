package pt.unl.fct.pds.utils;

import io.github.cdimascio.dotenv.Dotenv;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.unl.fct.pds.model.Node;

public class ConsensusParser {

  private String filename;

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

    List<Node> nodes = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new FileReader(filename), 16384)) {
      String line;
      Node currentNode = null;

      while ((line = reader.readLine()) != null) {
        if (line.isEmpty())
          continue;

        if (line.startsWith("r ")) {
          if (currentNode != null)
            nodes.add(currentNode);
          currentNode = parseNode(line);
        }
        else if (line.startsWith("a ")) {
          continue;
        }
        else if (line.startsWith("s ")) {
          if (currentNode != null)
            parseFlags(line, currentNode);
        }
        else if (line.startsWith("v ")) {
          if (currentNode != null)
            parseVersion(line, currentNode);
        }
        else if (line.startsWith("pr ")) {
          continue; // protocol list, optional
        }
        else if (line.startsWith("p ")) {
          if (currentNode != null)
            parseExitPolicy(line, currentNode);
        }
        else if (line.startsWith("w ")) {
          if (currentNode != null)
            parseBandwidth(line, currentNode);
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

  //TODO: change due to query limits
  /*public String geolocateIP(String ipAddress) throws IOException {
    if (accountId == null || licenseKey == null) {
      System.err.println("EVN variables not set");
    }

    //try (WebServiceClient client = new WebServiceClient.Builder(Integer.parseInt(accountId), licenseKey).host("geolite.info").build()) {
    try (DatabaseReader reader = new DatabaseReader.Builder(database).withCache(new CHMCache())
        .build()) {
      InetAddress ip = InetAddress.getByName(ipAddress);
      CountryResponse response = reader.country(ip);

      Country country = response.getCountry();
      return country.getName();
    } catch (GeoIp2Exception e) {
      throw new RuntimeException(e);
    }
  }*/
  public String geolocateIP(String ipAddress) throws IOException {
    try {
        URL url = new URL("https://api.ipquery.io/" + ipAddress);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
          throw new RuntimeException("error code: " + conn.getResponseCode());
        }

        // Read response
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
          response.append(line);
        }
        reader.close();

        // Parse JSON with Gson
        JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();

        // Navigate to nested "location" object
        if (jsonObject.has("location")) {
          JsonObject location = jsonObject.getAsJsonObject("location");
          if (location.has("country")) {
            return location.get("country").getAsString();
          }
        }

        return "Unknown";

      } catch (Exception e) {
        System.err.println("Failed to geolocate " + ipAddress + ": " + e.getMessage());
        return "Unknown";
      }
  }


  private Node parseNode(String line) throws IOException {
    if (!line.startsWith("r ")) {
      System.err.println("line " + line);
    }

    Node node = new Node();
    String[] parts = line.split(" ");

      // r <NodeNickname> <Fingerprint> <Digest> <Publication_time> <IP Address> <ORPort> <DIRPort>
      node.setNickname(parts[1]);
      node.setFingerprint(parts[2]);

    String dateTimeStr = parts[4] + " " + parts[5];
    LocalDateTime time = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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
      String[] parts = line.split(" ");
      String[] value = parts[1].split("=");
      int bandwidth = Integer.parseInt(value[1].trim());
      node.setBandwidth(bandwidth);
    }

    private void parseExitPolicy(String line, Node node) {
      //p <policy> <ports>
      String[] parts = line.split(" ", 2);
      String policy = parts[1]; //with ports
      node.setExitPolicy(policy);
    }
}
