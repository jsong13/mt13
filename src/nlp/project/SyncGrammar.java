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

  int totalSentences = 0;
  int totalNodes = 0;

  public void increment(SyncNode<T> sn){
    totalNodes += 1;
    s2t.incrementCount(sn.toSrcString(), sn.toTrgString(), 1.0);
  }

  public void increment(Tree<SyncNode<T>> snt){
    totalSentences += 1;
    for (Tree<SyncNode<T>> t : snt.getPreOrderTraversal())
      this.increment(t.getLabel());
  }
  
  // must be called to normalize and filter if any
  public void finishTraining(){
    s2t.normalize();
    isTrainComplete = true;
    System.out.println("SyncGrammar Train finished");
    System.out.println("SyncGrammar no of sentences read " + totalSentences);
    System.out.println("SyncGrammar no of keys " + s2t.size());
    System.out.println("SyncGrammar no of entryies " + s2t.totalSize());
    System.out.println("SyncGrammar count of rules " + totalNodes);


  }

  // MAIN api: translate to target sentence from a source tree
  public List<T> translateFromSrcTree(Tree<T> srcTree){
    Tree<SyncNode<T>> guessedSyncTree = initSyncTree(srcTree);
    fillBestOne(guessedSyncTree);
    List<T> trgSentence = SyncGrammar.getTrgYield(guessedSyncTree);
    return trgSentence;
  }

  static <S> Tree<SyncNode<S>> initSyncTree(Tree<S> srcTree) {
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
        srcstr += String.format("%s%d", NT, j+1) +" ";
        j++;
      }
      //System.out.println("debug: " + srcstr);
    }
    srcstr = srcstr.trim().replaceAll("\\s+", " ");

    // debug
    //System.out.println("debug");
    //System.out.println(srcTree);
    //System.out.println(srcstr);

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
    if (! isTrainComplete) throw new IllegalStateException("finishTraining() has not yet been called !");
    for (Tree<SyncNode<T>> c : unfilledTree.getPreOrderTraversal()) {
      String srcString = c.getLabel().toSrcString();
      String trgString = s2t.getCounter(srcString).argMax(); 
      if (trgString == null)
        throw new IllegalArgumentException("source node: '" + srcString + "' doesn't exist");
      c.setLabel(new SyncNode(srcString, trgString));
    } 
  }

  static public <S> List<S> getSrcYield(Tree<SyncNode<S>> filledTree){
    List<S> ret = new ArrayList<S>();

    SyncNode<S> sn = filledTree.getLabel();
    for (int i=0; i<sn.paddingSrc.size(); i++) {
      for (S a : sn.paddingSrc.get(i)) ret.add(a);
      if (i<sn.paddingSrc.size()-1) {
        for (S a : getSrcYield(filledTree.getChildren().get(i))) ret.add(a);
      }
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

  // for training CKY parser in the source side
  static <S> Tree<S> getSrcTree(Tree<SyncNode<S>> syncTree, S name){
    Tree<S> ret = new Tree<S>(name);
    SyncNode<S> sn = syncTree.getLabel();
    List<Tree<S>> newchildren =  new ArrayList<Tree<S>>();
    int i = 0;
    for(List<S> padding : sn.paddingSrc) {
      if (!padding.isEmpty()){
        for (S p : padding) {
          newchildren.add(new Tree<S>(p));
        }
      }
      if (i<sn.numNonTerminals()){
        newchildren.add(getSrcTree(syncTree.getChildren().get(i), name));
      }
      i++;
    }

    if (! newchildren.isEmpty()) 
      ret.setChildren(newchildren);
    return ret;
  }
 

}

