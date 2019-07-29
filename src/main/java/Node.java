import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper Class for creating location points for GraphDB.java.
 */

public class Node {

    long id;

    double lat;

    double lon;

    String name;

    // added for Extra
    String wayName = null;



    /*
    Store all the id's in a way that share a connection with this node. Stores all adjacent node ids.
    create way class just to be able to store connections then use that to connect in the graph


   create second constructor.

     */

    List<Long> connections;


    public Node(long id, double lat, double lon){

        this.id = id;

        this.lat = lat;

        this.lon = lon;

        this.connections = new ArrayList<>();

    }



    public void addConnection(long connection){

        connections.add(connection);


    }

    public void addName(String name){

        this.name = name;

    }

    public boolean hasConnection(){

        if(connections.size() > 0){

            return true;
        }

        return false;
    }

    public List<Long> getConnectionList(){

        return connections;

    }

    public double getLon(){

        return lon;
    }

    public double getLat(){

        return lat;

    }

    public long getId(){

        return id;
    }

    public void addWayName(String wayName){

        this.wayName = wayName;

    }

    public String getWayName(){

        return this.wayName;
    }


}
