package nlp.project;

import java.util.*;
import java.util.Collections.*;
import java.io.*;
import nlp.util.Pair;

class PhrasePair<T> {
  List<T> srcWords = new ArrayList<T>(); 
  List<T> trgWords = new ArrayList<T>();

  public PhrasePair(List<T> s, List<T> t){
     srcWords = s;
     trgWords = t;
  }

  // return a list of PhrasePair
  public static List<PhrasePair<T>> extractPhrasePairs(SentencePair<T> sp){

    List<PhrasePair<T>> ret = new ArrayList<PhrasePair<T>>();
    int m = sp.getSrcSentenceSize();
    int n = sp.getTrgSentenceSize();

    // need LS, US, LT, UT as bounds
    int[][] Ls2t = new int[m][n];
    int[][] Us2t = new int[m][n];
    int[][] Lt2s = new int[m][n];
    int[][] Ut2s = new int[m][n];
    
    // s2t side first
    for (int i=0; i<m; i++) {
      List<Integer> tspan = sp.getTrgPositions(i);
      if (tspan.size() == 0) {
        Ls2t[i][i] = m;
        Us2t[i][i] = -1;
      } else {
        Ls2t[i][i] = tspan[0];
        Us2t[i][i] = tspan[tspan.size()-1];
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
    for (int i=0; i<n; j++) {
      List<Integer> tspan = sp.getSrcPositions(i);
      if (tspan.size() == 0) {
        Lt2s[i][i] = n;
        Ut2s[i][i] = -1;
      } else {
        Lt2s[i][i] = tspan[0];
        Ut2s[i][i] = tspan[tspan.size()-1];
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
        if (jt < 0 || it >= n) continue;
        int i1 = Lt2s[it][jt];
        int j1 = Ut2s[it][jt];
        if (i1==i && j1==j) {
          // found a tight pair

        }
      }
    }
    
  }

}
