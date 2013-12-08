package nlp.project;

import java.util.*;
import java.util.Collections.*;
import java.io.*;
import nlp.util.Pair;

public class SentencePair<T>{
  // source and target sentence in T=String or Integer format
  List<T> srcSentence = new ArrayList<T>();
  List<T> trgSentence = new ArrayList<T>();

  // alignment position is counting from 1, where 0 is reserved for NULL
  List<Integer> wa_s2t = new ArrayList<Integer>();
  List<Integer> wa_t2s = new ArrayList<Integer>();

  // effective sentence size
  int  srcLength=-1;
  int  trgLength=-1;

  // read data sequentially from elsewhere
  public void addSrcWord(T i){ srcSentence.add(i); }
  public void addTrgWord(T i){ trgSentence.add(i); }
  public void addWAs2t(int j) { wa_s2t.add(j);}
  public void addWAt2s(int i) { wa_t2s.add(i);}
  
  // fix broken or truncated alignments
  public void finishRead(){
    srcLength = wa_s2t.size();
    trgLength = wa_t2s.size();

    // alignments out of bound
    for (int i=0; i<wa_s2t.size(); i++){
      if (wa_s2t.get(i) < 0 || wa_s2t.get(i) > trgSentence.size()) {
        wa_s2t.set(i, 0);
      }
    }
    for (int i=0; i<wa_t2s.size(); i++){
      if (wa_t2s.get(i) < 0 || wa_t2s.get(i) > srcSentence.size()) {
        wa_t2s.set(i, 0);
      }
    }
    for (int i:wa_t2s) srcLength = (i > srcLength) ? i : srcLength;
    for (int i:wa_s2t) trgLength = (i > trgLength) ? i : trgLength;
    
    int s1 = wa_s2t.size();
    for (int j=0; j<srcLength-s1; j++) wa_s2t.add(0);
    int s2 = wa_t2s.size();
    for (int j=0; j<trgLength-s2; j++) wa_t2s.add(0);
  }

  // effective size in case of truncated word alignment
  public int getSrcSentenceSize() { return srcLength; }
  public int getTrgSentenceSize() { return trgLength; }

  public List<T> getSrcSentence(){ return srcSentence; }
  public List<T> getTrgSentence(){ return trgSentence; }

  // return an unordered list word alignment tuples
  public List<Pair<Integer, Integer>> getWordAlignmentPairs(){
    List<Pair<Integer, Integer>> ret = new ArrayList<Pair<Integer,Integer>>(); 

    for (int i=0; i<wa_s2t.size(); i++) { 
      int j = wa_s2t.get(i);
      if (j>0) ret.add(Pair.makePair(i+1, j));
    }

    for (int j=0; j<wa_t2s.size(); j++) { 
      int i = wa_t2s.get(j);
      if (i>0) {
        if (! ret.contains(Pair.makePair(i, j+1)))
          ret.add(Pair.makePair(i, j+1));
      }
    }
    return ret;
  }

  // returns a sorted list of all the target positions that matches srcPos.
  // null is excluded, returned list does not have same elements
  // return empty List if no matches
  public List<Integer> getTrgPositions(int srcPos){
    List<Integer> ret = new ArrayList<Integer>();
    if (srcPos <= 0 || srcPos > wa_s2t.size()) return ret;

    for (int j=1; j<=wa_t2s.size(); j++) {
      int i = wa_t2s.get(j-1);
      if (i==srcPos)  { ret.add(j);}
    }

    int j = wa_s2t.get(srcPos-1);
    if (j>0) {
      int r = Collections.binarySearch(ret, j);  
      if (r<0) {ret.add(-r-1, j);}
    }
    return ret; 
  }

  // same as above, but return all source positions when target position is given
  public List<Integer> getSrcPositions(int trgPos){
    List<Integer> ret = new ArrayList<Integer>();
    if (trgPos <= 0 || trgPos > wa_t2s.size()) return ret;

    for (int i=1; i<=wa_s2t.size(); i++) {
      int j = wa_s2t.get(i-1);
      if (j==trgPos)  ret.add(i);
    }

    int i = wa_t2s.get(trgPos-1);
    if (i>0) {
      int r = Collections.binarySearch(ret, i);  
      if (r<0) ret.add(-r-1, i); 
    }

    return ret; 
  }

  public String toString(){
    String ret = "";
    for (T i : srcSentence)  ret += (" "+i);
    ret += "\n";
    for (T i : trgSentence)  ret += (" "+i);
    ret += "\n";
    return ret;
  }

  // return a string resprenting the word alignment matrix
  public String toStringMatrix(){
    String ret = "";
    ret += "    ";
    for (int i=0; i<getSrcSentenceSize(); i++) {
      ret += String.format(" %02d", i+1);
    }
    ret +="\n";
    for (int j=0; j<getTrgSentenceSize(); j++) {
      ret += String.format("%02d  ", j+1);
      for (int i=0; i<wa_s2t.size(); i++){
        if (wa_s2t.get(i) == j+1 || wa_t2s.get(j)== i+1) ret += " x ";
        else ret += "   ";
      }
      ret += "\n";
    }
    return ret;
  }

  public static SentencePair<String> int2string(SentencePair<Integer> sp, Vocabulary vs, Vocabulary vt) {
    SentencePair<String> ret = new SentencePair<String>();
    ret.wa_t2s = sp.wa_t2s;
    ret.wa_s2t = sp.wa_s2t;
    for (Integer i:sp.getSrcSentence())
      ret.srcSentence.add(vs.getWord(i));
    for (Integer i:sp.getTrgSentence())
      ret.trgSentence.add(vt.getWord(i));
    ret.finishRead();
    return ret;
  }
}

