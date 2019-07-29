import com.sun.xml.internal.xsom.impl.scd.Iterators;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.*;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */

    Map<Long, Node> nodeMap = new HashMap<>();

     public Trie prefixTrie = new Trie();

     public HashMap<String, Object> locations = new HashMap<>();







    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * adds new node into the graph
     * @param node node to be added
     */


    void addNode(Node node){

        nodeMap.put(node.id, node);

    }

    /**
     * removes specified node from the graph (this might not be needed)
     * @param node input node
     */


    public void removeNode(Node node){

        nodeMap.remove(node.id);

    }

    void connectNodes(Way way){

        // maybe add way name to help with router directions so we can access way name from nodes.

        long previousID;
        long currentID;
        long nextID;
        List<Long> connectionList = way.getConnectionList();

        //currentID = way.getID(); // make a get id method for ways so cannot change actual instance var of way.

        /*
        the id of the way is not in the graph, it only represents the way as an object.
        Therefore we only need to connect the nodes that are in the connection list of the way.
         */

        // put some documentation here helping

        //currentID = connectionList.get(0);

        //nextID = connectionList.get(0);

       // nodeMap.get(currentID).addConnection(nextID);

        /*
        need to be at least 2 nodes in connection list of way to even have any connections
         */

        /*
        this is to get the for loop kicked off
         */

        currentID = connectionList.get(0);

        nextID = connectionList.get(1);

        nodeMap.get(currentID).addConnection(nextID);

        //added extra

        nodeMap.get(currentID).addWayName(way.getWayName());




        for (int index = 1; index < connectionList.size() - 1; index++){



            previousID = connectionList.get(index - 1);

            currentID = connectionList.get(index);

            nextID = connectionList.get(index + 1);

            // add previous and next node ids to list of current node

            nodeMap.get(currentID).addConnection(previousID);

            nodeMap.get(currentID).addConnection(nextID);

            //added extra
            nodeMap.get(currentID).addWayName(way.getWayName());

        }

        nodeMap.get(nextID).addConnection(currentID);

        //added extra

        nodeMap.get(nextID).addWayName(way.getWayName());



    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        // TODO: Your code here.

        /*
        modifying and iterating through nodeMap at the same time need to fix this
         */

        Node currentNode;

        List<Long> keysToRemove = new ArrayList<>();

        for(long key : nodeMap.keySet()){

            currentNode = nodeMap.get(key);

            if(!currentNode.hasConnection()){

                keysToRemove.add(key);

            }

        }

        for(long key : keysToRemove){

            nodeMap.remove(key);

        }

    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
        //return new ArrayList<Long>(); do keyset to arraylist

        Set<Long> keys = nodeMap.keySet();

        return new ArrayList<>(keys);
    }

    /**
     * Returns ids of all vertices adjacent to v.
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {

        Node currentNode = nodeMap.get(v);

        return currentNode.getConnectionList();

    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    static double distance(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {
        // get values from map and put into list
        // iterate through until finding the min distance and return id of that node.

        Collection<Node> values = nodeMap.values();

        ArrayList<Node> nodeList = new ArrayList<>(values);

        double minDistance = Double.MAX_VALUE;

        long minID = 0;

        double distanceBetweenPoints;

        for (Node node : nodeList){

            distanceBetweenPoints = distance(lon, lat, node.getLon(), node.getLat());

            if(distanceBetweenPoints < minDistance){

                minDistance = distanceBetweenPoints;

                // probably should make a get method for id for Node class

                minID = node.id;

            }

        }

        return minID;
    }

    /**
     * Gets the longitude of a vertex.
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {

        Node currentNode = nodeMap.get(v);

        return currentNode.getLon();

    }

    /**
     * Gets the latitude of a vertex.
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {

        Node currentNode = nodeMap.get(v);

        return currentNode.getLat();
    }

    /**
     *
     */

    Node getNode(long id){

        return nodeMap.get(id);

    }


}


