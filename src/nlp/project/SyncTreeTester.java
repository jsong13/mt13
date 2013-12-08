package nlp.project;

import java.io.*;
import java.util.*;
import nlp.project.*;
import nlp.util.*;
import nlp.ling.Tree;

public class SyncTreeTester {
  static public <A> String indentTree(Tree<A> t){
    return indentTreeHelper(t, "   ", "");
  }

  static private <A> String indentTreeHelper(Tree<A> t, String indent, String pre) {
    String ret = pre + t.getLabel().toString() + "\n";
    for (Tree<A> c : t.getChildren())
      ret += indentTreeHelper(c, indent, pre+indent); 
    return ret;
  }

  public static void main(String[] args) throws IOException{
    Map<String, String> argMap = CommandLineUtils.simpleCommandLineParser(args);

    String vcb_src_path = argMap.get("-vcb_src");
    String vcb_trg_path = argMap.get("-vcb_trg");
    String t3_s2t_path = argMap.get("-t3_s2t");
    String t3_t2s_path = argMap.get("-t3_t2s");
    String wa_path = argMap.get("-wa");
    String dev_src_path = argMap.get("-dev_src");
    String dev_trg_path = argMap.get("-dev_trg");
    String test_src_path = argMap.get("-test_src");
    String test_trg_path = argMap.get("-test_trg");

    System.out.println("vcb_src path\t" + vcb_src_path);
    System.out.println("vcb_trg path\t" + vcb_trg_path);
    System.out.println("wa path\t" + wa_path);
    System.out.println("t3_s2t path\t" + t3_s2t_path);
    System.out.println("t3_t2s path\t" + t3_t2s_path);
    System.out.println("dev_src path\t" + dev_src_path);
    System.out.println("dev_trg path\t" + dev_trg_path);
    System.out.println("test_src path\t" + test_src_path);
    System.out.println("test_trg path\t" + test_trg_path);
  
    Vocabulary srcVcb = new Vocabulary(vcb_src_path);
    Vocabulary trgVcb = new Vocabulary(vcb_trg_path);
    System.out.println("vocabularies loaded!");

    List<SentencePair<String>> dlkfdskkkjev = IOUtils.loadParallelText(dev_src_path, dev_trg_path);
    System.out.println("dev files loaded!");

    CounterMap<Integer, Integer> t3_s2t = IOUtils.loadT3(t3_s2t_path);
    CounterMap<Integer, Integer> t3_t2s = IOUtils.loadT3(t3_t2s_path);
    System.out.println("t3 loaded!");

    List<SentencePair<Integer>> sp = IOUtils.loadWA(wa_path);
    System.out.println("wa loaded");

    // debug output for wa load result
    int sentenceNo = 0;
    if (false) {
    for (SentencePair<Integer> a1 : sp) {
      sentenceNo++;
      if (sentenceNo != 45634) continue;
      SentencePair<String> a = SentencePair.int2string(a1, srcVcb, trgVcb);
      System.out.println("----------");
      System.out.println(a.srcSentence);
      System.out.println(a.trgSentence);
      System.out.println(a.toStringMatrix());
      PhrasePairs ap = new PhrasePairs(a);

      System.out.println(indentTree(ap.getTreeL()));
      System.out.println(indentTree(ap.buildSyncTree()));
      System.out.println(indentTree(SyncTrees.reduce2Src(ap.buildSyncTree(), "S")));
      System.out.println((SyncTrees.reduce2Src(ap.buildSyncTree(), "S")));
    }}

    sentenceNo = 0;
    if (true) {
    for (SentencePair<Integer> a1 : sp) {
      sentenceNo++;
      //System.out.println("#"+sentenceNo+"##");
      if (a1.getSrcSentenceSize() > 40 || a1.getSrcSentenceSize() <= 5 ) {
        System.out.println(String.format("(S TooLong%d)", a1.getSrcSentenceSize()));
        continue;
      }
      SentencePair<String> a = SentencePair.int2string(a1, srcVcb, trgVcb);
      PhrasePairs ap = new PhrasePairs(a);

      //System.out.println(a.getSrcSentence());
      //System.out.println(ap.buildSyncTree()==null);
      //System.out.println(ap.buildSyncTree());
      System.out.println((SyncTrees.reduce2Src(ap.buildSyncTree(), "S")));
      if(sentenceNo%1000==0) System.gc();
    }}

    return;
  }
}

