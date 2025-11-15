package pt.unl.fct.pds;


import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.ConsensusParser;


/**
 * Application for Tor Path Selection alternatives.
 *
 */
public class Project2 
{
    public static void main( String[] args )
    {
      ConsensusParser parser = new ConsensusParser("src/consensus_files/consensus_1.txt");
      Node[] nodes = parser.parseConsensus();
      System.out.println(nodes.length + '\n');

      System.out.println(nodes[0].getBandwidth());
      System.out.println(nodes[0].getIpAddress());
      System.out.println(nodes[0].getCountry());
    }
}
