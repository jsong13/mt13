package nlp.project;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import nlp.util.*;
import nlp.ling.*;


public class IOUtils{

  public static List<SentencePair<Integer>> loadWA(String path) throws IOException {
    List<SentencePair<Integer>> ret = new ArrayList<SentencePair<Integer>>();
    BufferedReader reader = null;

    try{
      // 1 for lineno, 2 for source, 3 for target, 4 for wa-st 5 for wa-ts
      // and we assume initially that we just finish the last record
      int preStatus = 5;  

      SentencePair<Integer> currentSP = null;
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(path))); 
      String line=null;
 
      while ( (line = reader.readLine()) != null ) {
        String[] parts = line.trim().split("\\s+");

        if (parts.length == 1 && parts[0].startsWith("#") 
            && parts[0].endsWith("##") && preStatus == 5) {
          currentSP = new SentencePair<Integer>();
          preStatus = 1;
          continue;
        }

        if (preStatus == 1) {
          for (String a : parts) 
            currentSP.addSrcWord(Integer.parseInt(a)); 
          preStatus++;
          continue;
        }

        if (preStatus == 2) {
          for (String a : parts) 
            currentSP.addTrgWord(Integer.parseInt(a)); 
          preStatus++;
          continue;
        }

        if (preStatus == 3) {
          for (String a: parts)
            currentSP.addWAs2t(Integer.parseInt(a));
          preStatus++;
          continue;
        }

        if (preStatus == 4) {
          for (String a: parts)
            currentSP.addWAt2s(Integer.parseInt(a));
          
          currentSP.finishRead();
          ret.add(currentSP);
          currentSP = null;
          preStatus++;
          continue;
        }

        throw new IOException();
      }
    } catch (Exception e) {
      throw new IOException("Wrong wa format in " + path);
    } finally {
      reader.close();
    }

    return ret;
  }

    
  public static List<SentencePair<Integer>> loadSNT(String path) throws IOException {
    List<SentencePair<Integer>> ret = new ArrayList<SentencePair<Integer>>();
    BufferedReader reader = null;

    try{
      int preStatus = 1;  // 1 for separator, 2 for source, 3 for target
      SentencePair<Integer> currentSP = null;
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(path))); 
      String line=null;
 
      while ( (line = reader.readLine()) != null ) {
        String[] parts = line.split("\\s+");

        if (parts.length == 1 && parts[0].equals("1") && preStatus != 2) {
          preStatus = 1;
          continue;
        }

        if (parts.length == 0) continue;

        if (preStatus == 1) {
          currentSP = new SentencePair<Integer>();
          for (String a : parts) 
            currentSP.addSrcWord(Integer.parseInt(a)); 
          preStatus = 2;
          continue;
        }

        if (preStatus == 2) {
          for (String a : parts) 
            currentSP.addTrgWord(Integer.parseInt(a)); 
          ret.add(currentSP);
          preStatus = 3;
          continue;
        }
        throw new IOException("Wrong snt format in " + path);
      }
    } catch (Exception e) {
      throw new IOException("Wrong snt format in " + path);
    } finally {
      reader.close();
    }

    return ret;
  }

  public static List<SentencePair<String>> loadParallelText(String spath, String tpath) 
    throws IOException {

    List<SentencePair<String>> ret = new ArrayList<SentencePair<String>>();
    BufferedReader reader1 = null;
    BufferedReader reader2 = null;

    try {
      reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(spath),"UTF-8")); 
      reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(tpath),"UTF-8")); 
      while(true) {
        String line1 = reader1.readLine();
        String line2 = reader2.readLine();
        if ((line1 == null && line2 != null) || (line2 == null && line1 != null))
          throw new IOException();
        if (line1 == null && line2 == null) break;
        SentencePair<String> csp = new SentencePair<String>();
        for (String a : line1.split("\\s+")) csp.addSrcWord(a);
        for (String a : line2.split("\\s+")) csp.addTrgWord(a);
        ret.add(csp);
      }

    } catch( Exception e) {
      throw new IOException("Wrong parallel corpus: " + spath + ", " + tpath); 
    } finally {
      reader1.close();
      reader2.close();
    }
    return ret;
  }

  public static CounterMap<Integer, Integer> loadT3(String path) throws IOException {
    CounterMap<Integer, Integer> ret = new CounterMap();
    BufferedReader reader = null;
    try{
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(path))); 
      String line=null;

      while ((line = reader.readLine()) != null) {
        String[] parts = line.split("\\s+");
        int i = Integer.parseInt(parts[0]);
        int j = Integer.parseInt(parts[1]);
        double f = Double.parseDouble(parts[2]);
        ret.incrementCount(i,j,f);
      }
    } catch(Exception e){
      throw new IOException("Wrong t3 format: " + path);
    } finally {
      reader.close();
    }

    return ret;
  }

  // LP for left parenthesis and RP for right parenthesis
  // both can string, tree node are separted by space
  // assuming all the labels and LP and RP do not have spaces 
  // choose LP and RP carefully to avoid labels in tree node.
  // NOTE: Leaf node are in LP and RP too, because some labels can be empty

  public static <T> String treeToString(Tree<T> tree, String LP, String RP) {
    String ret = "";
    ret += LP+ " ";
    ret += tree.getLabel().toString() + " ";
    for (Tree<T> c : tree.getChildren()) {
      ret += treeToString(c, LP, RP) + " ";
    }
    ret += RP;
    return ret;
  }

  // see above
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

