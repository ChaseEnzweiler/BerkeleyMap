import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class TestTrie {

    @Test
    public void TestPrefixes(){

        Trie trie = new Trie();

        trie.put("abc", "Number1");

        trie.put("asshole", "Number2");

        trie.put("a", "Number3");

        trie.put("last", "Number4");

        List<String> expected = new ArrayList<>();

        List<String> real = trie.getPrefixes("a");

        expected.add("Number3");
        expected.add("Number1");
        expected.add("Number2");

        assertEquals(expected, real);

    }

}
