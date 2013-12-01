package nlp.project;

import java.io.*;
import java.util.*;
import nlp.project.*;
import nlp.util.*;


class DecodedTranslator implements Translator<String> {
  Translator<Integer>  wt;
  Vocabulary  srcVcb, trgVcb;
  public DecodedTranslator(Translator<Integer> wt, Vocabulary srcVcb, Vocabulary trgVcb){
    this.wt = wt;
    this.srcVcb = srcVcb;
    this.trgVcb = trgVcb;
  }

  public List<String> translate(List<String> sSentence) {

    List<Integer> sSentenceIndex = new ArrayList<Integer>();
    for (String s : sSentence) {
      Integer i = srcVcb.getIndex(s);
      if (i != null )
        sSentenceIndex.add(i);
    }

    List<Integer> tSentenceIndex = wt.translate(sSentenceIndex);
    
    List<String> tSentence = new ArrayList<String>();
    for (int i : tSentenceIndex) {
      String s = trgVcb.getWord(i);
      if (s != null) {
        tSentence.add(s);
      }
    }
    return tSentence;
  }
}
