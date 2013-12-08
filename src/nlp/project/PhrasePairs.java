package nlp.project;

import java.util.*;
import java.util.Collections.*;
import java.io.*;
import nlp.util.Pair;
import nlp.ling.Tree;

// deal with phrase pairs in a sentence
// output a Synchronisze Tree
class PhrasePairs<T> {
  SentencePair<T> sentencePair;
  public PhrasePairs(SentencePair<T> sp) { 
    sentencePair = sp; 
    extract();
  }

  private int[][] Ls2t = null;
  private int[][] Us2t = null;
  private int[][] Lt2s = null;
  private int[][] Ut2s = null;
  private List<List<PairBound>> PBatSS = null;
 
  static class PairBound {
    int sstart = -1;
    int send = -1;
    int tstart = -1;
    int tend = -1;
    public PairBound(){}
    public PairBound(int si, int sj, int ti, int tj) {sstart = si; send = sj; tstart=ti; tend=tj;}
    public int getsstart(){return sstart;}
    public int getsend(){return send;}
    public int getssize(){return send-sstart;}
    public int gettstart(){return tstart;}
    public int gettend(){return tend;}
    public int gettsize(){return tend-tstart;}

    public String toString(){
      return String.format("(%d %d, %d %d)", sstart+1, send+1, tstart+1, tend+1);
    }
  }

  // for tree extraction
  public List<PairBound> getPairBoundsByStart(int start){
    return PBatSS.get(start); 
  }

  // get all legitmate pairs and arrange them in PBatSS by source start positions
  private void extract(){
    int m = sentencePair.getSrcSentenceSize();
    int n = sentencePair.getTrgSentenceSize();

    PBatSS = new ArrayList<List<PairBound>>();
    for (int i=0; i<m; i++) 
      PBatSS.add(new ArrayList<PairBound>());
    

    // need LS, US, LT, UT as bounds
    // positions are counted from 0 to m-1 or n-1
    Ls2t = new int[m][m];
    Us2t = new int[m][m];
    Lt2s = new int[n][n];
    Ut2s = new int[n][n];
    
      // s2t side first
    for (int i=0; i<m; i++) {
      List<Integer> tspan = sentencePair.getTrgPositions(i+1);
      if (tspan.size() == 0) {
        Ls2t[i][i] = m+1;
        Us2t[i][i] = -1;
      } else {
        Ls2t[i][i] = tspan.get(0)-1;
        Us2t[i][i] = tspan.get(tspan.size()-1)-1;
      }
    }

    for (int span = 1; span <= m-1; span++) {
      for (int i=0; i<m; i++) {
        int j = i+span; 
        if (j >= m) break;
        Ls2t[i][j] = Math.min(Ls2t[i][j-1], Ls2t[j][j]); 
        Us2t[i][j] = Math.max(Us2t[i][j-1], Us2t[j][j]);
      }    
    }

    // t2s side 
    for (int i=0; i<n; i++) {
      List<Integer> tspan = sentencePair.getSrcPositions(i+1);
      if (tspan.size() == 0) {
        Lt2s[i][i] = n+1;
        Ut2s[i][i] = -1;
      } else {
        Lt2s[i][i] = tspan.get(0)-1;
        Ut2s[i][i] = tspan.get(tspan.size()-1)-1;
      }
    }

    for (int span = 1; span <= n-1; span++) {
      for (int i=0; i<n; i++) {
        int j = i+span; 
        if (j >= n) break;
        Lt2s[i][j] = Math.min(Lt2s[i][j-1], Lt2s[j][j]); 
        Ut2s[i][j] = Math.max(Ut2s[i][j-1], Ut2s[j][j]);
      }    
    }

    // let's go through
    for (int i=0; i<m; i++) {
      for (int j=i; j<m; j++) {
        int it = Ls2t[i][j];
        int jt = Us2t[i][j];
        if (jt < 0 || jt >= n || it<0 || it >= n) continue;
        int i1 = Lt2s[it][jt];
        int j1 = Ut2s[it][jt];
        if (i1==i && j1==j) {
        // found pair
          PairBound b = new PairBound(i,j,it,jt);
          PBatSS.get(i).add(b);
        }
      }
    }
  }

  public Tree<PairBound> getTreeL(){
    // locate the largest phrase pair in the sentence
    // there is one and only one largest pair
    Tree<PairBound> ret = null;
    for (int i=0; i<sentencePair.getSrcSentenceSize(); i++) {
      List<PairBound> lpb = getPairBoundsByStart(i);
      if (lpb.isEmpty()) continue;
      PairBound largest = lpb.get(lpb.size()-1); 
      ret = getTreeLHelper(largest); 
      break;
    }
    return ret;
  }

  // helper make a tree with one phrase-pair
  // pick out the (left, longest) sub phrase pairs
  private Tree<PairBound> getTreeLHelper(PairBound parent) {
    List<PairBound> children = new ArrayList<PairBound>();

    int i = parent.getsstart();
    while (i<=parent.getsend()){
      List<PairBound> candidates = getPairBoundsByStart(i);
      if (candidates.isEmpty()) { i++; continue;}
      for (int j = candidates.size()-1; j>=0; j--) {
        if (candidates.get(j).getssize() < parent.getssize()) {
          children.add(candidates.get(j));
          i = candidates.get(j).getsend();
          break; // to the end of while body
        }
      }
      i++;
    }

    if (children.isEmpty()) {
      return new Tree<PairBound>(parent);
    } else {
      List<Tree<PairBound>> childrenTree = new ArrayList<Tree<PairBound>>();
      for (PairBound c : children){
        childrenTree.add(getTreeLHelper(c));
      }
      return new Tree<PairBound>(parent, childrenTree);
    }
  }


  // transform a pairbound tree into a synchronized tree
  Tree<SyncNode> expandToSyncTree(Tree<PairBound> tpb) {
    if (tpb == null) return null;
    List<Tree<SyncNode>> newchildren = new ArrayList<Tree<SyncNode>>();
    SyncNode<T> ret = new SyncNode();

    PairBound pb = tpb.getLabel();
    int ss = pb.getsstart();
    int se = pb.getsend();
    int ts = pb.gettstart();
    int te = pb.gettend();
    Map<Integer, Integer> sourcemark = new HashMap<Integer, Integer>();
    Map<Integer, Integer> targetmark = new HashMap<Integer, Integer>();

    for (int i=ss; i<=se; i++) sourcemark.put(i, -1);
    for (int i=ts; i<=te; i++) targetmark.put(i, -1);

    for(int i = 0; i<tpb.getChildren().size(); i++) {
      PairBound cpb = tpb.getChildren().get(i).getLabel();
      for (int j = cpb.getsstart(); j<=cpb.getsend(); j++)  sourcemark.put(j, i);
      for (int j = cpb.gettstart(); j<=cpb.gettend(); j++) targetmark.put(j, i);
    }

    int premark = -1;
    List<T> padding = new ArrayList<T>();
    for (int cs=ss; cs<=se; cs++){
      int cmark = sourcemark.get(cs);
      if (cmark == -1) {
        padding.add(sentencePair.getSrcSentence().get(cs));
        premark = cmark;
        continue;
      } 
      if (premark != cmark) {
        ret.paddingSrc.add(padding);
        padding = new ArrayList<T>();
        premark = cmark;
        continue;
      }
    }
    ret.paddingSrc.add(padding);

    premark = -1;
    padding = new ArrayList<T>();
    for (int cs=ts; cs<=te; cs++){
      int cmark = targetmark.get(cs);
      if (cmark == -1) {
        padding.add(sentencePair.getTrgSentence().get(cs));
        premark = cmark;
        continue;
      } 
      if (premark != cmark) {
        ret.ordert2s.add(cmark);
        ret.paddingTrg.add(padding);
        padding = new ArrayList<T>();
        premark = cmark;
        continue;
      }
    }
    ret.paddingTrg.add(padding);
    

    for(Tree<PairBound> ctpb : tpb.getChildren()) {
      newchildren.add(expandToSyncTree(ctpb));
    }
    return new Tree<SyncNode>(ret, newchildren);
  }

  // for convenience
  public Tree<SyncNode> buildSyncTree(){
    return expandToSyncTree(getTreeL());
  }
}
