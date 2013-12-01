package nlp.project;

import java.util.*;
import java.io.*;

public class SentencePair<T>{
  List<T> srcSentence = new ArrayList<T>();
  List<T> trgSentence = new ArrayList<T>();

  public void addSrcWord(T i){
    srcSentence.add(i); 
  }
  public void addTrgWord(T i){
    trgSentence.add(i); 
  }
  public List<T> getSrcSentence(){
    return srcSentence; 
  }
  public List<T> getTrgSentence(){
    return trgSentence; 
  }


  public String toString(){
    String ret = "";
    for (T i : srcSentence)  ret += (" "+i);
    ret += "\n";
    for (T i : trgSentence)  ret += (" "+i);
    ret += "\n";
    return ret;
  }
}

