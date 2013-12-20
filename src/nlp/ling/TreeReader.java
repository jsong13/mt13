package nlp.ling;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TreeReader {
	static public Tree<String> stringToTree(String line, String LP, String RP) {
	    String[] parts = line.trim().split("\\s+");    
	    Tree<String> ret = null;
	    Stack<Tree<String>> parents = new Stack<Tree<String>>();
	    for (String word : parts) {
	      Tree<String> parent = null;

	      if (!parents.empty()) parent = parents.peek();

	      if (word.equals(LP)) {
	        Tree<String> tr = new Tree<String>("");
	        if (parent!=null) {
	          // add tr to the current parent, stupid Tree doesn't have addChild API!
	          List<Tree<String>> children = parent.getChildren();
	          List<Tree<String>> newChildren = new ArrayList<Tree<String>>(children);
	          newChildren.add(tr);
	          parent.setChildren(newChildren);
	        }
	        parents.push(tr);
	        continue;
	      } 
	       
	      if (word.equals(RP)) {
	        ret = parents.pop();
	        continue;
	      }
	      
	      parent.setLabel(word);
	    }
	    return ret;
	  }

	  static public List<Tree<String>> readTreesFromFile(String path, String LP, String RP) throws IOException {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8")); 
	    List<Tree<String>> ret = new ArrayList<Tree<String>>();
	    while (true) {
	      String line = reader.readLine();
	      if (line == null) break;
	      if (!line.trim().startsWith(LP)) continue;
	      ret.add(stringToTree(line, LP, RP));
	    }
	    return ret;
	  }
}
