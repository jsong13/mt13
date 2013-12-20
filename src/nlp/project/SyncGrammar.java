package nlp.project;

import java.util.*;
import java.util.Collections.*;
import java.io.*;
import nlp.util.Pair;
import nlp.util.CounterMap;
import nlp.ling.Tree;

class SyncGrammar<T> {
  CounterMap<String, String> s2t = new CounterMap<String, String>();
  boolean isTrainComplete = false;
  public static final String NT = SyncNode.NT;

  public void increment(SyncNode<T> sn){
    s2t.incrementCount(sn.toSrcString(), sn.toTrgString(), 1.0);
  }

  public void increment(Tree<SyncNode<T>> snt){
    for (Tree<SyncNode<T>> t : snt.getPreOrderTraversal())
      this.increment(t.getLabel());
  }
  
  // must be called to normalize and filter if any
  public void finishTraining(){
    s2t.normalize();
    isTrainComplete = true;
  }

  static public <S> Tree<SyncNode<S>> initSyncTree(Tree<S> srcTree) {
    if (srcTree.isLeaf()) {
      // tree root must be an non-terminal as root, throw a runtime Exception
      throw new IllegalArgumentException("Tree root must be an non-terminal "+ srcTree.toString());
    }

    String srcstr= "";
    int j = 0;
    for (Tree<S> c : srcTree.getChildren()) {
      if (c.isLeaf()) {
        srcstr += (c.getLabel().toString() + " ");
      } else {
        srcstr += String.format("%s%d", NT, j+1);
        j++;
      }
    }

    Tree<SyncNode<S>> ret = new Tree<SyncNode<S>>(new SyncNode<S>(srcstr));
    List<Tree<SyncNode<S>>> children = new ArrayList<Tree<SyncNode<S>>>();
    for (Tree<S> c : srcTree.getChildren()) {
      if (!c.isLeaf()) 
        children.add(initSyncTree(c)); 
    }
    ret.setChildren(children);

    return ret;
  }
  
  void fillBestOne(Tree<SyncNode<T>> unfilledTree) {
    for (Tree<SyncNode<T>> c : unfilledTree.getPreOrderTraversal()) {
      String srcString = c.getLabel().toSrcString();
      String trgString = s2t.getCounter(srcString).argMax(); 
      c.setLabel(new SyncNode(srcString, trgString));
    } 
  }

  static public <S> List<S> getSrcYield(Tree<SyncNode<S>> filledTree){
    List<S> ret = new ArrayList<S>();
    if (filledTree.isLeaf()) {
      throw new IllegalArgumentException("Tree root must be an non-terminal "+ filledTree.toString());
    }
    return ret;
  }

  static public <S> List<S> getTrgYield(Tree<SyncNode<S>> filledTree){
    List<S> ret = new ArrayList<S>();

    SyncNode<S> sn = filledTree.getLabel();
    for (int i=0; i<sn.paddingTrg.size(); i++) {
      for (S a : sn.paddingTrg.get(i)) ret.add(a);
      if (i<sn.paddingTrg.size()-1) {
        for (S a : getTrgYield(filledTree.getChildren().get(sn.ordert2s.get(i)))) ret.add(a);
      }
    }
    return ret;
  }
}

