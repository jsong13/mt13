package nlp.project;

import java.util.ArrayList; 
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import nlp.project.PCFGParserTester.BinaryRule;
import nlp.project.PCFGParserTester.Grammar;
import nlp.project.PCFGParserTester.Lexicon;
import nlp.project.PCFGParserTester.Parser;
//import nlp.assignments.PCFGParserTester.TreeAnnotations;
import nlp.project.PCFGParserTester.TreeAnnotations;
import nlp.project.PCFGParserTester.UnaryClosure;
import nlp.project.PCFGParserTester.UnaryRule;
import nlp.ling.Tree;
import nlp.ling.Trees;
import nlp.util.CounterMap;
import nlp.util.Indexer;

public class CKYParserK implements Parser{
	static final String root = "NT-";
	static int rootIndex;
//	static final int K = 5;
	static int sumTop = 0;
	
	public void insert(List<Trace> list, Trace trace){
		for (int i = 0; i < list.size(); i++){
			if (list.get(i).score < trace.score){
				list.add(i, trace);
			}
		}
	}

  CounterMap<List<String>, Tree<String>> knownParses;
  CounterMap<Integer, String> spanToCategories;
  Indexer<String> indexer;
  Lexicon lexicon;
  Grammar grammar;
  UnaryClosure uc;
  CounterMap<String, String> tempLexicon;
  	@Override
  	public Tree<String> getBestParse(List<String> sentence){
  		List<Tree<String>> bestParseK = getBestParseK(sentence, 1);
//  		return bestParseK.get(bestParseK.size() - 1);
  		return bestParseK.get(0);
  	}
  	
	@SuppressWarnings("unchecked")
	public List<Tree<String>> getBestParseK(List<String> sentence, int K) {
		int sizeOfWords = sentence.size();
		int sizeOfNonTer = indexer.size();
		Map<String, List<Trace>>[][] score= new HashMap[sizeOfWords+1][sizeOfWords+1];
//		Map<String, int[]>[][] back = new HashMap[sizeOfWords+1][sizeOfWords+1];
		for (int i = 0; i < sizeOfWords; i++){
			String word = sentence.get(i);
			score[i][i+1] = new HashMap<String, List<Trace>>();
//			back[i][i+1] = new HashMap<String, int[]>();
			List<Trace> bestK = new ArrayList<Trace>();
			bestK.add(new Trace(1.0));
			score[i][i+1].put(word, bestK);
			// initialize the first round in the DP table
			List<UnaryRule> unaryRules = uc.getClosedUnaryRulesByChild(word);
			for (UnaryRule unaryRule : unaryRules){
				bestK = score[i][i+1].get(unaryRule.parent);
				if (bestK != null){
					insert(bestK, new Trace(unaryRule.score, 0, null, unaryRule));
				} else {
					bestK = new ArrayList<Trace>();
					bestK.add(new Trace(unaryRule.score, 0, null, unaryRule));
				}
				score[i][i+1].put(unaryRule.parent, bestK);
			}
		}

		String leftChild;
		String rightChild;
		for (int span = 2; span <= sizeOfWords; span++){
			for (int i = 0; i <= sizeOfWords - span; i++){
				int j = i + span;
				score[i][j] = new HashMap<String, List<Trace>>();
//				back[i][j] = new HashMap<String, int[]>();
				
				for (int parentIndex = 1; parentIndex < sizeOfNonTer; parentIndex++){
					String parent = indexer.get(parentIndex);
					Queue<Trace> maxHeap= new PriorityQueue<Trace>(K+1, Collections.reverseOrder());
					List<BinaryRule> binaryRules = grammar.getBinaryRulesByParent(parent);
					for (BinaryRule binaryRule : binaryRules){
						leftChild = binaryRule.leftChild;
						rightChild = binaryRule.rightChild;
						for (int k = i+1; k < j; k++){
								// set score by binary rule
//									if (score[i][k] != null && score[k][j] != null){
							List<Trace> leftBestK = score[i][k].get(leftChild);
							List<Trace> rightBestK = score[k][j].get(rightChild);
							if (leftBestK != null && rightBestK != null){
								double prob = leftBestK.get(0).score * rightBestK.get(0).score * binaryRule.getScore();
								Trace trace = new Trace(prob, k, binaryRule, null);
								trace.leftRank = 0;
								trace.rightRank = 0;
								maxHeap.add(trace);
								}
							}
					}

					Queue<Trace> bestK = new PriorityQueue<Trace>(K); // min heap
					while (bestK.size() < K && !maxHeap.isEmpty()){
						Trace trace = maxHeap.poll();
						bestK.add(trace);
						if (trace.binaryRule != null){
							List<Trace> leftBestK = score[i][trace.split].get(trace.binaryRule.leftChild);
							List<Trace> rightBestK = score[trace.split][j].get(trace.binaryRule.rightChild);
							if (trace.leftRank < leftBestK.size() -1 ){
								double prob = leftBestK.get(trace.leftRank + 1).score * rightBestK.get(trace.rightRank).score * trace.binaryRule.score;
								Trace trace1 = new Trace(prob, trace.split, trace.binaryRule, null);
								trace1.leftRank++;
								trace1.rightRank = trace.rightRank;
								maxHeap.add(trace1);
							}
							if (trace.rightRank < rightBestK.size() - 1){
								double prob = leftBestK.get(trace.leftRank).score * rightBestK.get(trace.rightRank + 1).score * trace.binaryRule.score;
								Trace trace2 = new Trace(prob, trace.split, trace.binaryRule, null);
								trace2.rightRank++;
								trace2.leftRank = trace.leftRank;
								maxHeap.add(trace2);
							}
						} 
					}
					if (!bestK.isEmpty()){
            List<Trace> bestList = new LinkedList<Trace>();
            while (!bestK.isEmpty()){
              bestList.add(0, bestK.poll());
            }
            score[i][j].put(parent, bestList);
					}

					
					// set score by unary rule
  					if (!bestK.isEmpty()){
  						List<UnaryRule> unaryRules = uc.getClosedUnaryRulesByChild(parent);
  						for (UnaryRule unaryRule : unaryRules){
  							Iterator<Trace> iter = bestK.iterator();
                List<Trace> unaryParentList = score[i][j].get(unaryRule.parent);
                Set<Trace> hashSet = new HashSet<Trace>(unaryParentList);
                Queue<Trace> unaryParentMinHeap = new PriorityQueue<Trace>(hashSet);
  							while (iter.hasNext()){
  								Trace trace = iter.next();			
  								double prob = trace.score * unaryRule.score;
  								Trace trace3 = new Trace(prob, 0, null, unaryRule);
  								if (!hashSet.contains(trace3)){
  								  hashSet.add(trace3);
                    unaryParentMinHeap.add(trace3);
                    if (unaryParentMinHeap.size() > K){
                      unaryParentMinHeap.poll();
                    }
  								}
  						}
  						unaryParentList = new LinkedList<Trace>();
  						while (!unaryParentMinHeap.isEmpty()){
  						  unaryParentList.add(0, unaryParentMinHeap.poll());
  						}
  						score[i][j].put(unaryRule.parent, unaryParentList);
  					}
					}
				}
			}
		}

		List<Tree<String>> annotatedBestParseK = new ArrayList<Tree<String>>();
		List<Trace> bestK = score[0][sizeOfWords].get(root);
		int topK = K;
		if (K > bestK.size()){
			topK = bestK.size();
		} 
		for (int k = 0; k < topK; k++){
			Tree<String> annotatedBestParse = buildTree(sentence, 0, sizeOfWords, root, k, score);
			annotatedBestParseK.add(TreeAnnotations.unAnnotateTree(annotatedBestParse));
		}
//		System.out.print(Trees.PennTreeRenderer.render(annotatedBestParse));
		//sumTop += topK;
		//System.out.println("topK: " + topK +"\n sumTop: " + sumTop);
		return annotatedBestParseK;
	}
	
	public Tree<String> buildUnaryTree(List<String> path, List<Tree<String>> leaves){
		List<Tree<String>> trees = leaves;
//		List<Tree<String>> trees = new ArrayList<Tree<String>>();
		for (int k = path.size() - 1; k >= 0; k--){
			trees = Collections.singletonList(new Tree<String>(path.get(k), trees));
		}
		return trees.get(0);
	}
	
	public Tree<String> buildTree(List<String> sentence, int i, int j, String parent, int rank, Map<String, List<Trace>>[][] score){
//		String parent = indexer.get(parentIndex);
//		int[] trace = back[i][j].get(parent);
		List<Trace> bestK = score[i][j].get(parent);
		Trace trace = bestK.get(rank);
			if (parent == root && trace.unaryRule == null && trace.binaryRule == null){
				return new Tree<String> (parent);
			}
			if (i==j-1){ 
				if (trace.unaryRule == null && trace.binaryRule == null){
//						return new Tree<String>(parent, Collections.singletonList(new Tree<String>(sentence.get(i))));
					return new Tree<String>(sentence.get(i));
					} else {
						List<String> path = uc.getPath(trace.unaryRule);
						List<Tree<String>> emptyList = Collections.emptyList();
						Tree<String> leaf = new Tree<String>(sentence.get(i), emptyList);
						return buildUnaryTree(path.subList(0, path.size() - 1), Collections.singletonList(leaf));
					}
			}
			if (trace.unaryRule != null){
				Tree<String> leaves = buildTree(sentence, i, j, trace.unaryRule.child, 0, score);
//				UnaryRule unaryRule = new UnaryRule(parent, indexer.get(trace[0]));
				List<String> path = uc.getPath(trace.unaryRule);
				Tree<String> unaryTree = buildUnaryTree(path.subList(0, path.size() - 1), Collections.singletonList(leaves));
				return unaryTree;
			}
			else{
				// for binary rule
				Tree<String> leftTree = buildTree(sentence, i, trace.split, trace.binaryRule.leftChild, trace.leftRank, score);
				Tree<String> rightTree = buildTree(sentence, trace.split, j, trace.binaryRule.rightChild, trace.rightRank, score);
				List<Tree<String>> childrenList = new ArrayList<Tree<String>>(2);
				childrenList.add(leftTree);
				childrenList.add(rightTree);
				return new Tree<String>(parent, childrenList);
			}

	}
		
  public CKYParserK(List<Tree<String>> trainTrees) {
  	
    System.out.print("Annotating / binarizing training trees ... ");
    List<Tree<String>> annotatedTrainTrees = annotateTrees(trainTrees);
//	System.out.print(Trees.PennTreeRenderer.render(annotatedTrainTrees.get(0)));
    System.out.println("done.");

    System.out.print("Building grammar ... ");
    grammar = new Grammar(annotatedTrainTrees);
    System.out.println("done. (" + grammar.getStates().size() + " states)");
    indexer = new Indexer<String>(grammar.getStates().size()+1);
    indexer.add("");
    indexer.addAll(grammar.getStates());
    rootIndex = indexer.indexOf(root);
    uc = new UnaryClosure(grammar);
//    System.out.println(uc);

//    lexicon = new Lexicon(annotatedTrainTrees);
//    knownParses = new CounterMap<List<String>, Tree<String>>();
//    spanToCategories = new CounterMap<Integer, String>();
//    for (Tree<String> trainTree : annotatedTrainTrees) {
//      List<String> tags = trainTree.getPreTerminalYield();
//      knownParses.incrementCount(tags, trainTree, 1.0);
//      tallySpans(trainTree, 0);
//    }
    System.out.println("done.");
  }

  public CKYParserK(Grammar grammar, CounterMap<String, String> lexicon) {
  	this.grammar = grammar;
  	this.tempLexicon = lexicon;
    indexer = new Indexer<String>(grammar.getStates().size()+1);
    indexer.add("");
    indexer.addAll(grammar.getStates());
    uc = new UnaryClosure(grammar);
  }

	private List<Tree<String>> annotateTrees(List<Tree<String>> trees) {
    List<Tree<String>> annotatedTrees = new ArrayList<Tree<String>>();
    for (Tree<String> tree : trees) {
      annotatedTrees.add(TreeAnnotations.annotateTree(tree));
    }
    return annotatedTrees;
  }

  private int tallySpans(Tree<String> tree, int start) {
    if (tree.isLeaf() || tree.isPreTerminal()) return 1;
    int end = start;
    for (Tree<String> child : tree.getChildren()) {
      int childSpan = tallySpans(child, end);
      end += childSpan;
    }
    String category = tree.getLabel();
    if (!category.equals(root))
      spanToCategories.incrementCount(end - start, category, 1.0);
    return end - start;
  }
  
  public static void main(String[] args){
  	CounterMap<String, String> lexicon = new CounterMap<String, String>();
  	Grammar grammar = new Grammar();
  	BinaryRule binaryRule;
  	binaryRule = new BinaryRule("S", "NP", "VP");
  	binaryRule.setScore(0.9);
  	grammar.addBinary(binaryRule);
  	binaryRule = new BinaryRule("VP", "V", "NP");
  	binaryRule.setScore(0.5);
  	grammar.addBinary(binaryRule);
  	binaryRule = new BinaryRule("VP", "V", "@VP_V");
  	binaryRule.setScore(0.3);
  	grammar.addBinary(binaryRule);
  	binaryRule = new BinaryRule("VP", "V", "PP");
  	binaryRule.setScore(0.1);
  	grammar.addBinary(binaryRule);
  	binaryRule = new BinaryRule("@VP_V", "NP", "PP");
  	binaryRule.setScore(1.0);
  	grammar.addBinary(binaryRule);
  	binaryRule = new BinaryRule("NP", "NP", "NP");
  	binaryRule.setScore(0.1);
  	grammar.addBinary(binaryRule);
  	binaryRule = new BinaryRule("NP", "NP", "PP");
  	binaryRule.setScore(0.2);
  	grammar.addBinary(binaryRule);
  	binaryRule = new BinaryRule("PP", "P", "NP");
  	binaryRule.setScore(1.0);
  	grammar.addBinary(binaryRule);
  	UnaryRule unaryRule;
  	unaryRule = new UnaryRule("S", "VP");
  	unaryRule.setScore(0.1);
  	grammar.addUnary(unaryRule);
  	unaryRule = new UnaryRule("VP", "V");
  	unaryRule.setScore(0.1);
  	grammar.addUnary(unaryRule);
  	unaryRule = new UnaryRule("NP", "N");
  	unaryRule.setScore(0.7);
  	grammar.addUnary(unaryRule);
  	lexicon.setCount("people", "N", 0.5);
  	lexicon.setCount("fish", "N", 0.2);
  	lexicon.setCount("tanks", "N", 0.2);
  	lexicon.setCount("rods", "N", 0.1);
  	lexicon.setCount("people", "V", 0.1);
  	lexicon.setCount("fish", "V", 0.6);
  	lexicon.setCount("tanks", "V", 0.3);
  	lexicon.setCount("with", "P", 1.0);
  	Parser parser = new CKYParserK(grammar, lexicon);
  	String[] strArr = {"fish", "people", "fish", "tanks"};
  	List<String> sentence = Arrays.asList(strArr);
  	String root = "S";
  	Tree<String> tree = parser.getBestParse(sentence);
  	System.out.println(tree.toString());
  }

}
