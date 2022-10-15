import java.util.ArrayList;
import java.util.List;

public class Trie {

    //support lowercase letters in the alphabet
    private static final int R = 128;


    private class Node {
        /* up to R links */
        boolean exists;
        Node[] links;
        String fullName;

        public Node() {

            exists = false;
            links = new Node[R];
            fullName = null;
        }

        public void setName(String name) {

            fullName = name;
        }
    }

    private Node root = new Node();

    // array stores nodes containing values and those arrays are indexed by characters
    //they don't store characters.


    public void put(String key, String name) {
        put(root, key, 0, name);
    }

    private Node put(Node x, String key, int d, String name) {

        if (x == null) {
            x = new Node();
        }

        if (d == key.length()) {
            x.exists = true;
            x.setName(name);
            return x;
        }

        char c = key.charAt(d);
        x.links[c] = put(x.links[c], key, d + 1, name);
        return x;
    }



    // needs a search that takes all prefixes
    public List<String> getPrefixes(String key) {

        List<String> prefixes = new ArrayList<>();

        // first get to proper node from key
        Node currentNode = root;

        for (int i = 0; i < key.length(); i++) {
            currentNode = currentNode.links[key.charAt(i)];
            if(currentNode == null){
                return prefixes;
            }
        }

        // now we have correct node and we need to get all prefixes
        prefixSearch(prefixes, currentNode);
        return prefixes;
    }


    private void prefixSearch(List<String> result, Node n){
        if(n.exists){
            result.add(n.fullName);
        }

        for(Node x : n.links){
            if(x != null){
                prefixSearch(result,x);
            }
        }

    }

}
