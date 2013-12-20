
package nlp.project;

import java.io.*;
import java.util.*;
import java.util.PriorityQueue;

import nlp.project.*;
import nlp.util.*;
import nlp.langmodel.*;
import nlp.ling.Tree;

public class MTTester1 {
  static String joinList(List<String> lst){
    if (lst.isEmpty()) return "";

    String ret = "";
    for (String a : lst) ret += " "+a;
    return ret.substring(1);
  
  }
    
  static public <A> String indentTree(Tree<A> t){
    return indentTreeHelper(t, "  ", "");
  }

  static private <A> String indentTreeHelper(Tree<A> t, String indent, String pre) {
    String ret = pre + t.getLabel().toString() + "\n";
    for (Tree<A> c : t.getChildren())
      ret += indentTreeHelper(c, indent, pre+indent); 
    return ret;
  }


  public static void main(String[] args) throws Exception{
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

    String train_tree_src_path = argMap.get("-train_tree_src");

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
    System.out.println("word alignment loaded!");

    /////////////////////////////////
    // train the countermap in grammar
    // and get trainTrees for Parser
    int testNo = 2000;
    int sentenceNo = 0;

    List<Tree<SyncNode<String>>> trainSyncTrees = new ArrayList<Tree<SyncNode<String>>>();
    List<Tree<String>> trainTrees = new ArrayList<Tree<String>>();
    List<List<String>> trainingCollectionForLM = new ArrayList<List<String>>();

    SyncGrammar<String> syncGrammar = new SyncGrammar();
    for (SentencePair<Integer> a1 : sp) {
      sentenceNo++;
      if (a1.getSrcSentenceSize() > 40 || a1.getSrcSentenceSize() <= 5 ) {
        continue;
      }
      if (sentenceNo < testNo) continue;

      SentencePair<String> a = SentencePair.int2string(a1, srcVcb, trgVcb);
      PhrasePairs ap = new PhrasePairs(a);
      Tree<SyncNode<String>> syncTree = ap.buildSyncTree();
      trainSyncTrees.add(syncTree);

      trainTrees.add(SyncGrammar.getSrcTree(syncTree, "NT-"));
      trainingCollectionForLM.add(SyncGrammar.getTrgYield(syncTree));

      syncGrammar.increment(syncTree);

      if(sentenceNo%1000==0) {
        System.gc();
        System.out.println("training syncGrammar: " + sentenceNo);
      }

    }

    syncGrammar.finishTraining();
    System.out.println("sync grammar train finished");
    System.out.println("dump syncGrammar below");
    System.out.println(syncGrammar.s2t);

    LanguageModel languageModel = new EmpiricalTrigramLanguageModel(trainingCollectionForLM);
    System.out.println("Language model training is done.");


    // train CKY Parser
    CKYParserK parser = new CKYParserK(trainTrees);

    System.out.println("Parser train done");





    // test on orginal trained set where source trees are available
    sentenceNo = 0;
    int numGramBleu = 4;
    double[] sumOfBleu = new double[numGramBleu];
    double[] sumOfBleuLM = new double [numGramBleu];

    for (Tree<SyncNode<String>> syncTree : trainSyncTrees) {
      List<List<String>> candidates = new ArrayList<List<String>>();
      List<List<String>> references = new ArrayList<List<String>>();

      sentenceNo++;
      if (sentenceNo >= testNo) continue;
      //if (sentenceNo < 352) continue;

      Tree<String> srcTree = SyncGrammar.getSrcTree(syncTree, "NT-");
      List<String> srcSentence = SyncGrammar.getSrcYield(syncTree);
      List<String> gold = SyncGrammar.getTrgYield(syncTree);

      System.out.println("--------------------");
      System.out.println(sentenceNo);
      System.out.println(joinList(srcSentence));
      System.out.println(joinList(gold));

      List<String> guess = null;
      List<Tree<String>> guessedTrees = null;
      List<List<String>> LMguessList = new ArrayList<List<String>>();
      FastPriorityQueue<List<String>> heap = new FastPriorityQueue<List<String>>(candidates.size());
     try{
      guessedTrees = parser.getBestParseK(srcSentence, 5);
      for (Tree<String> guessedTree : guessedTrees){
          guess = syncGrammar.translateFromSrcTree(guessedTree);
          candidates.add(guess);
          double prob = languageModel.getSentenceProbability(guess);
          heap.setPriority(guess, prob);
          System.out.println(joinList(guess));
          System.out.println(prob);
//          System.out.println(indentTree(srcTree));
//          System.out.println(indentTree(guessedTree));

      }
     }catch(IllegalArgumentException e){
      System.out.println(indentTree(srcTree));
      System.out.println("cannot translate");
     }
     references.add(gold);
     System.out.println("  BLEU before language model rescoring:");
     for (int i = 0; i < numGramBleu; i++){
       double bleuScore = Evaluator.bleu(Collections.singletonList(candidates.get(0)), references, i+1);
       sumOfBleu[i] += bleuScore;
       System.out.print(" BLEU" + (i+1) + " : " + bleuScore);
     }
     System.out.println();
     while (heap.hasNext()){
       LMguessList.add(heap.next());
     }
     System.out.println(" BLEU after language model rescoring:");
     for (int i = 0; i < numGramBleu; i++){
       double bleuScore = Evaluator.bleu(Collections.singletonList(LMguessList.get(0)), references, i+1);
       sumOfBleuLM[i] += bleuScore;
       System.out.print(" BLEU" + (i+1) + " : " + bleuScore);
     }
     System.out.println();
    }
    System.out.println("BLEU before language model rescoring:");
    for (int i = 0; i < numGramBleu; i++){
      System.out.println("  BLEU" + (i+1) + " : " + sumOfBleu[i]/sentenceNo);
    }
    System.out.println("BLEU after language model rescoring:");
    for (int i = 0; i < numGramBleu; i++){
      System.out.println("  BLEU" + (i+1) + " : " + sumOfBleuLM[i]/sentenceNo);
    }


    return;
  }
}

