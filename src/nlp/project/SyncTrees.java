package nlp.project;

import java.util.*;
import java.util.Collections.*;
import java.io.*;
import nlp.util.Pair;
import nlp.ling.Tree;

class SyncTrees {
  static <T> Tree<T> reduce2Src(Tree<SyncNode<T>> syncTree, T name){
    Tree<T> ret = new Tree<T>(name);
    SyncNode<T> sn = syncTree.getLabel();
    List<Tree<T>> newchildren =  new ArrayList<Tree<T>>();
    int i = 0;
    for(List<T> padding : sn.paddingSrc) {
      if (!padding.isEmpty()){
        for (T p : padding) {
          newchildren.add(new Tree<T>(p));
        }
      }
      if (i<sn.numNonTerminals()){
        newchildren.add(reduce2Src(syncTree.getChildren().get(i), name));
      }
      i++;
    }

    if (! newchildren.isEmpty()) 
      ret.setChildren(newchildren);
    return ret;
  }
}
