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
    List<String> wayNames = new ArrayList<>();
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

    public void addWayNames(String wayName){
        this.wayNames.add(wayName);
    }

    public String getWayNames(int i){
        return wayNames.get(i);
    }

    public List<String> getWayNames() {
        return wayNames;
    }

    public boolean shareWays(Node other){

        for(String name : this.wayNames){
            if(other.getWayNames().contains(name)){
                return true;
            }
        }
        return false;
    }

    public String sharedWays(Node other){
        for(String name : this.wayNames){
            if(other.getWayNames().contains(name)){
                return name;
            }
        }
        return "";
    }

    public boolean hasWayName(String name){
        return this.wayNames.contains(name);
    }

}
