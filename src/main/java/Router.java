import sun.security.provider.certpath.Vertex;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {


    public static List<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                          double destlon, double destlat) {

        /**
         * Return a List of longs representing the shortest path from the node
         * closest to a start location and the node closest to the destination
         * location.
         * @param g The graph to use.
         * @param stlon The longitude of the start location.
         * @param stlat The latitude of the start location.
         * @param destlon The longitude of the destination location.
         * @param destlat The latitude of the destination location.
         * @return A list of node id's in the order visited on the shortest path.
         */

        // might have to make a start id

        long startID = g.closest(stlon, stlat);

        long destinationID = g.closest(destlon, destlat);


        Map<Long, Double> distanceTo = new HashMap<>();

        Map<Long, Long> edges = new HashMap<>();

        Set<Long> marked = new HashSet<>();

        List<Long> path = new ArrayList<>();


        class NodeComparator implements Comparator<Node>{

            @Override
            public int compare(Node node1, Node node2){

                double heuristic1 = distanceTo.get(node1.getId()) + g.distance(node1.getId(), destinationID);

                double heuristic2 = distanceTo.get(node2.getId()) + g.distance(node2.getId(), destinationID);

                if(heuristic1 < heuristic2){

                    return -1;

                } else if(heuristic1 > heuristic2){

                    return 1;
                }

                return 0;
            }
        }

        PriorityQueue<Node> fringe = new PriorityQueue<>(new NodeComparator());

        Node currentNode = g.getNode(startID);

        long currentNodeID = startID;

        fringe.add(currentNode);

        distanceTo.put(currentNodeID, 0.0);

        edges.put(currentNodeID, 0L);

        boolean reachedDestination = false;

        while(!reachedDestination){

            currentNode = fringe.poll();

            currentNodeID = currentNode.getId();

            if(marked.contains(currentNodeID)){

                continue;

            }else if(currentNodeID == destinationID){

                reachedDestination = true;

                continue;

            } else{

                for(long adj: g.adjacent(currentNodeID)){

                    if(distanceTo.containsKey(adj)){ //edit

                        if(distanceTo.get(adj) > distanceTo.get(currentNodeID) + g.distance(currentNodeID, adj)){

                            distanceTo.replace(adj, distanceTo.get(currentNodeID) + g.distance(currentNodeID, adj));

                            edges.replace(adj, currentNodeID);
                        }

                    } else{

                        distanceTo.put(adj, distanceTo.get(currentNodeID) + g.distance(currentNodeID, adj));
                        edges.put(adj, currentNodeID);

                    }

                    fringe.add(g.getNode(adj));
                    //edges.put(adj, currentNodeID);
                }
            }



            marked.add(currentNodeID);



        }

        long idToAdd = destinationID;

        while(idToAdd!= startID){

            path.add(idToAdd);

            idToAdd = edges.get(idToAdd);

        }

        path.add(idToAdd);

        Collections.reverse(path);

        return path;



        }


    /**
     * bearing helper for navigation
     *
     */

    public static String bearingHelper(Node node1, Node node2, GraphDB g){

        double bearingDigit = g.bearing(node1.getId(), node2.getId());

        double absBearing = Math.abs(bearingDigit);

        if(absBearing <= 15.0){

            return NavigationDirection.DIRECTIONS[1];

        } else if(absBearing > 15.0 && absBearing <= 30.0){

            if(bearingDigit < 0.0){
                // turn left
                return NavigationDirection.DIRECTIONS[3];

            }

            // turn right

            return NavigationDirection.DIRECTIONS[2];


        } else if(absBearing > 30.0 && absBearing <= 100.0){

            if(bearingDigit < 0.0){
                //turn left
                return NavigationDirection.DIRECTIONS[4];

            }

            //turn right
            return NavigationDirection.DIRECTIONS[5];


        } else{

            if(bearingDigit < 0.0){
                //turn left
                return NavigationDirection.DIRECTIONS[7];

            }

            //turn right
            return NavigationDirection.DIRECTIONS[6];
        }
    }




    /**
     * Create the list of directions corresponding to a route on the graph.
     * @param g The graph to use.
     * @param route The route to translate into directions. Each element
     *              corresponds to a node from the graph in the route.
     * @return A list of NavigatiionDirection objects corresponding to the input
     * route.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {

        List<NavigationDirection> directions = new ArrayList<>();

        // set direction, way and distance

        String direction = NavigationDirection.DIRECTIONS[0]; // start from int to string

        Node currentNode = g.getNode(route.get(0));

        Node nextNode;

        String way = currentNode.getWayName();

        double distance = 0.0;


        for(int i = 1; i < route.size(); i++){

            // add distance if next way is same as current

            nextNode = g.getNode(route.get(i));

            /*
            next way is the same so update the distance
             */

            if(currentNode.getWayName().equals(nextNode.getWayName())){

                distance = distance + g.distance(currentNode.getId(), nextNode.getId());

            } else {

                /*
                next node is in different way so need to return a navdir object for current node then
                set direction, reset distance, and set new way
                 */

                distance = distance + g.distance(currentNode.getId(), nextNode.getId());


                String navigation = direction + " on " + way + " and continue for " + distance + " miles.";

                directions.add(NavigationDirection.fromString(navigation));


                /*
                set new direction and way and reset distance to zero, may need helper for bearing.

                 */

                direction = bearingHelper(currentNode, nextNode, g);

                way = nextNode.getWayName();

                distance = 0.0;

            }

            currentNode = nextNode;

        }

        String navigation = direction + " on " + way + " and continue for " + distance + " miles.";

        directions.add(NavigationDirection.fromString(navigation));




        return directions; // FIXME
    }


    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /** Integer constants representing directions. */
        public static final int START = 0;
        public static final int STRAIGHT = 1;
        public static final int SLIGHT_LEFT = 2;
        public static final int SLIGHT_RIGHT = 3;
        public static final int RIGHT = 4;
        public static final int LEFT = 5;
        public static final int SHARP_LEFT = 6;
        public static final int SHARP_RIGHT = 7;

        /** Number of directions supported. */
        public static final int NUM_DIRECTIONS = 8;

        /** A mapping of integer values to directions.*/
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        /** Default name for an unknown way. */
        public static final String UNKNOWN_ROAD = "unknown road";
        
        /** Static initializer. */
        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /** The direction a given NavigationDirection represents.*/
        int direction;
        /** The name of the way I represent. */
        String way;
        /** The distance along this way I represent. */
        double distance;

        /**
         * Create a default, anonymous NavigationDirection.
         */
        public NavigationDirection() {
            this.direction = STRAIGHT;
            this.way = UNKNOWN_ROAD;
            this.distance = 0.0;
        }

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Takes the string representation of a navigation direction and converts it into
         * a Navigation Direction object.
         * @param dirAsString The string representation of the NavigationDirection.
         * @return A NavigationDirection object representing the input string.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                    && way.equals(((NavigationDirection) o).way)
                    && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}
