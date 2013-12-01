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

    String vcb_src_path = argMap.get("-vcbsrc");
    String vcb_trg_path = argMap.get("-vcbtrg");
    String train_snt_path = argMap.get("-trainsnt");
    String t3_path = argMap.get("-t3");
    String dev_src_path = argMap.get("-devsrc");
    String dev_trg_path = argMap.get("-devtrg");
    String test_src_path = argMap.get("-testsrc");
    String test_trg_path = argMap.get("-testtrg");

    System.out.println("vcb src path\t" + vcb_src_path);
    System.out.println("vcb trg path\t" + vcb_trg_path);
    System.out.println("train snt path\t" + train_snt_path);
    System.out.println("t3 path\t" + t3_path);
    System.out.println("dev src path\t" + dev_src_path);
    System.out.println("dev trg path\t" + dev_trg_path);
    System.out.println("test src path\t" + test_src_path);
    System.out.println("test trg path\t" + test_trg_path);
  
    Vocabulary srcVcb = new Vocabulary(vcb_src_path);
    Vocabulary trgVcb = new Vocabulary(vcb_trg_path);
    System.out.println("vocabularies loaded!");
    List<SentencePair<String>> dev = IOUtils.loadParallelText(dev_src_path, dev_trg_path);
    System.out.println("dev files loaded!");
    CounterMap<Integer, Integer> t3 = IOUtils.loadT3(t3_path);
    System.out.println("t3 loaded!");
    Translator<Integer> wt = WordTranslator.buildFromT3(t3);

    System.out.println("word translator built!");
    evaluate(dev, new DecodedTranslator(wt, srcVcb, trgVcb), 1000); 
    System.out.println("evaluation done!");
    return;
  }
}
