import java.util.ArrayList;
import java.util.List;

/**
 * Class is used to store all the connections in a way
 */

public class Way {

    List<Long> connectionList;
    long wayID;
    // added for extra
    String wayName;

    public Way(long id){
        this.wayID = id;
        connectionList = new ArrayList<>();
        wayName = null;
    }

    public void addName(String name){
        this.wayName = name;
    }

    public String getWayName(){
        return wayName;
    }

    public long getID(){
        return wayID;
    }

    public void addConnection(long id){
        connectionList.add(id);
    }

    public List<Long> getConnectionList(){
        return connectionList;
    }

    public int size(){
        return connectionList.size();
    }
}
