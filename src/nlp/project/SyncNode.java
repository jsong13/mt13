package nlp.project;

import java.util.*;
import java.util.Collections.*;
import java.io.*;
import nlp.util.Pair;
import nlp.ling.Tree;

// TreeNode used in a synctree or elsewhere
// non-terminals are unnamed in this class
// only the number and the orderings are recorded here
// T is the type of sentence words

class SyncNode<T>{
  // for future use
  T name = null;

  // represent a rule of synchronus grammar: T -> e1e2 A B e3 C, f1 B f2 C A
  // record the order change from target to source
  // t2s[0] = 1, t2s[1] = 2,  t2s[2] = 0,
  List<Integer> ordert2s = new ArrayList<Integer>(); 

  // length = length(NT) + 1
  // use "" to represent empty string
  List<List<T>> paddingSrc = new ArrayList<List<T>>();
  List<List<T>> paddingTrg = new ArrayList<List<T>>();

  public int numNonTerminals(){return ordert2s.size();}

  // dump for debug
  public String toString(){
    String ret = "";

    ret += toSourceString();
    ret += "|"; 
    ret += toTargetString();
 
    return ret;
  }

  public String toSourceString(){
    String ret = "";
    int i=0;
    for (List<T> pads : paddingSrc) {
      for (T p : pads) ret += (" "+p);
      if (i<ordert2s.size())
        ret += " "+String.format("_X_%d", i+1);
      i++;
    }
    ret = ret.replaceAll("\\s+", " ").trim();
    return ret;
  }

  public String toTargetString(){
    String ret = "";
    int i=0;
    for (List<T> pads : paddingTrg) {
      for (T p : pads) ret += " "+p;
      if (i<ordert2s.size())
        ret += " "+String.format("_X_%d", ordert2s.get(i)+1);
      i++;
    }
    ret = ret.replaceAll("\\s+", " ").trim();
    return ret;
  }

}
