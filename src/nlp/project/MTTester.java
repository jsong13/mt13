package nlp.project;

import java.io.*;
import java.util.*;
import nlp.project.*;
import nlp.util.*;

public class MTTester {
  public static void evaluate(List<SentencePair<String>> sps, Translator<String> translator, int limit) {
    List<List<String>> candidates = new ArrayList<List<String>>();
    List<List<String>> references = new ArrayList<List<String>>();
    for (SentencePair<String> sp : sps) {

      List<String> c = translator.translate(sp.getSrcSentence());
      candidates.add(c);
      references.add(sp.getTrgSentence());

      
      // verbose
      if (true) {
        System.out.println("---" + candidates.size()+"---");
        for (String s :sp.getSrcSentence() ) System.out.print(s+" ");
        System.out.println("");
        for (String s :sp.getTrgSentence() ) System.out.print(s+" ");
        System.out.println("");
        for (String s : c ) System.out.print(s+" ");
        System.out.println("");
      }
      
      if (candidates.size() > limit) break;
    }

    System.out.println("BLEU 1: "+ Evaluator.bleu(candidates, references, 1));
    System.out.println("BLEU 2: "+ Evaluator.bleu(candidates, references, 2));
    System.out.println("BLEU 3: "+ Evaluator.bleu(candidates, references, 3));
    System.out.println("BLEU 4: "+ Evaluator.bleu(candidates, references, 4));
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
    System.out.println("train corpus and word alignment loaded");

    // debug output for wa load result
    for (SentencePair<Integer> a : sp) {
      if (a.wa_s2t.size() != 20) continue;
      System.out.println("----------");
      System.out.println(a.wa_s2t);
      System.out.println(a.wa_t2s);
      System.out.println(a.getWordAlignmentPairs());
      for (int i = 1; i<= a.wa_s2t.size(); i++) {
        System.out.print(i + " ");
        System.out.println(a.getTrgPositions(i));
      }

      for (int i = 1; i<= a.wa_t2s.size(); i++) {
        System.out.print(i + " ");
        System.out.println(a.getSrcPositions(i));
      }
      System.out.println(a.toStringMatrix());
    }

    /*
    Translator<Integer> wt = WordTranslator.buildFromT3(t3_s2t);

    System.out.println("word translator built!");
    evaluate(dev, new DecodedTranslator(wt, srcVcb, trgVcb), 1000); 
    System.out.println("evaluation done!");
    */
    return;
  }
}

