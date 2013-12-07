package nlp.project;

import java.util.*;
import java.util.Collections.*;
import java.io.*;
import nlp.util.Pair;
import nlp.ling.Tree;

// deal with phrase pairs in a sentence
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

  // show the tree nicely for debug
  public static String treeToString(Tree<PairBound> t){
    return treeToStringHelper(t, "  ", "");
  }

  private static String treeToStringHelper(Tree<PairBound> t, String indent, String pre){
    String ret = "";
    ret += (pre + t.getLabel().toString() + "\n");
    for (Tree<PairBound> c: t.getChildren()) {
      ret += treeToStringHelper(c, indent, pre+indent);
    }
    return ret;
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

  // make a tree under the node
  private Tree<PairBound> getTreeLHelper(PairBound parent) {
    List<PairBound> children = new ArrayList<PairBound>();

    System.out.println("Tree node " + parent);
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
}
