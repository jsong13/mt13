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
  // for future, not used now
  T name = null;
  
  // for dump
  public static final String NT = "NT-";
  final T dummy = (T)new Object();

  // represent a rule of synchronus grammar: T -> e1e2 A B e3 C, f1 B f2 C A
  // record the order change from target to source
  // t2s[0] = 1, t2s[1] = 2,  t2s[2] = 0,
  List<Integer> ordert2s;

  // length = length(NT) + 1
  // use "" to represent empty string
  List<List<T>> paddingSrc; 
  List<List<T>> paddingTrg; 

  public int numNonTerminals(){return ordert2s.size();}

  public String toString(){
    String ret = "";

    ret += toSrcString();
    ret += "|"; 
    ret += toTrgString();
 
    return ret;
  }

  public String toSrcString(){
    String ret = "";
    int i=0;
    for (List<T> pads : paddingSrc) {
      for (T p : pads) ret += (" "+p);
      if (i<ordert2s.size())
        ret += " "+String.format("%s%d", NT, i+1);
      i++;
    }
    ret = ret.replaceAll("\\s+", " ").trim();
    return ret;
  }

  public String toTrgString(){
    String ret = "";
    int i=0;
    for (List<T> pads : paddingTrg) {
      for (T p : pads) ret += " "+p;
      if (i<ordert2s.size())
        ret += " "+String.format("%s%d", NT, ordert2s.get(i)+1);
      i++;
    }
    ret = ret.replaceAll("\\s+", " ").trim();
    return ret;
  }
  
  public SyncNode(){
    paddingSrc = new ArrayList<List<T>>();
    paddingTrg = new ArrayList<List<T>>();
    ordert2s = new ArrayList<Integer>();
  }
  // load from string that dumped by toSourceString and toTargetString
  // in case of srcString only, target padding are filled with empty string
  // and order is set to linear
  public SyncNode(String srcString){
    this();
    String[] parts = srcString.trim().split("\\s+");

    List<T> currentPadding = new ArrayList<T>();
    for (int i =0; i<parts.length; i++) {
      if (parts[i].startsWith(NT)){
        paddingSrc.add(currentPadding);
        currentPadding = new ArrayList<T>();
        ordert2s.add(-1);
        continue;
      } else {
        currentPadding.add(toT(parts[i])); 
      }
    }
    paddingSrc.add(currentPadding);
  }

  // see above
  public SyncNode(String srcString, String trgString){
    this(srcString); 

    int j = 0;
    String[] parts = trgString.trim().split("\\s+");
    List<T> currentPadding = new ArrayList<T>();
    for(int i=0; i < parts.length; i++) {
      if (parts[i].startsWith(NT)) {
        paddingTrg.add(currentPadding);
        currentPadding = new ArrayList<T>();
        ordert2s.set(j, -1+Integer.valueOf(parts[i].substring(NT.length())));
        j++;
      } else {
        currentPadding.add(toT(parts[i])); 
      }
    }
    paddingTrg.add(currentPadding);
  }

  // convert String to Type T
  private T toT(String a){
    if (dummy.getClass().getName().equals("java.lang.Integer")) {
      return (T)(Integer.valueOf(a));
    } 
    return (T)a;
  }
}
