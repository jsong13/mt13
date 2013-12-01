package nlp.project;

import java.util.*;
import nlp.project.IOUtils;
import nlp.util.*;

class WordTranslator implements Translator<Integer>{
  CounterMap<Integer, Integer> t3 = null; 
  private WordTranslator(){}

  static public Translator<Integer> buildFromT3(CounterMap<Integer, Integer> t3) {
    WordTranslator wti = new WordTranslator();
    wti.t3 = t3;
    return wti;
  }

  public List<Integer> translate(List<Integer> srcSentence) {
    List<Integer> ret = new ArrayList<Integer>();
    for (int i : srcSentence) {
      if ( t3.containsKey(i) ) {
        Counter<Integer> tt = t3.getCounter(i);
        ret.add(tt.argMax());
      } else {
        // unknown word skip at the moment
      }
    }
    return ret;
  }
}

