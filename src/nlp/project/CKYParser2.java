package nlp.project;

import java.util.ArrayList; 
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

public class CKYParser2 implements Parser {
	static final String root = "S";
	static int rootIndex;

  CounterMap<List<String>, Tree<String>> knownParses;
  CounterMap<Integer, String> spanToCategories;
  Indexer<String> indexer;
  Lexicon lexicon;
  Grammar grammar;
  UnaryClosure uc;
  CounterMap<String, String> tempLexicon;
	@SuppressWarnings("unchecked")
	@Override
	public Tree<String> getBestParse(List<String> sentence) {
		int sizeOfWords = sentence.size();
		int sizeOfNonTer = indexer.size();
		Map<String, Double>[][] score= new HashMap[sizeOfWords+1][sizeOfWords+1];
		Map<String, int[]>[][] back = new HashMap[sizeOfWords+1][sizeOfWords+1];
		for (int i = 0; i < sizeOfWords; i++){
			String word = sentence.get(i);
			score[i][i+1] = new HashMap<String, Double>();
			back[i][i+1] = new HashMap<String, int[]>();
			score[i][i+1].put(word, 1.0);
			// initialize the first round in the DP table
			List<UnaryRule> unaryRules = uc.getClosedUnaryRulesByChild(word);
			for (UnaryRule unaryRule : unaryRules){
				score[i][i+1].put(unaryRule.parent, unaryRule.score);
				int[] trace = {indexer.indexOf(unaryRule.child)}; // the size of trace is 1 if it is a unary rule;
				back[i][i+1].put(unaryRule.parent, trace);
			}
		}

		String leftChild;
		String rightChild;
		for (int span = 2; span <= sizeOfWords; span++){
			for (int i = 0; i <= sizeOfWords - span; i++){
				int j = i + span;
				score[i][j] = new HashMap<String, Double>();
				back[i][j] = new HashMap<String, int[]>();
				
				for (int parentIndex = 1; parentIndex < sizeOfNonTer; parentIndex++){
					String parent = indexer.get(parentIndex);
					List<BinaryRule> binaryRules = grammar.getBinaryRulesByParent(parent);
					for (BinaryRule binaryRule : binaryRules){
						leftChild = binaryRule.leftChild;
						rightChild = binaryRule.rightChild;
						for (int k = i+1; k < j; k++){
								// set score by binary rule
//									if (score[i][k] != null && score[k][j] != null){
										if (score[i][k].containsKey(leftChild) && score[k][j].containsKey(rightChild)){
											double prob = score[i][k].get(leftChild) * score[k][j].get(rightChild) * binaryRule.getScore();
											Double currentScore = score[i][j].get(parent);
											if (currentScore == null || prob > currentScore){
												score[i][j].put(parent, prob);
												int[] trace = {k, indexer.indexOf(leftChild), indexer.indexOf(rightChild)};
												back[i][j].put(parent, trace);
											}
										}
//									}
								}
					}
						// set score by unary rule
//						List<UnaryRule> unaryRules = uc.getClosedUnaryRulesByParent(parent);
						List<UnaryRule> unaryRules = uc.getClosedUnaryRulesByChild(parent);
						for (UnaryRule unaryRule : unaryRules){
//							if (score[i][j] != null){
								Double prob = score[i][j].get(unaryRule.child);
								if (prob != null){
									prob *= unaryRule.getScore();
									Double currentScore = score[i][j].get(unaryRule.parent);
									if (currentScore == null || prob > currentScore){
										score[i][j].put(unaryRule.parent, prob);
										int[] trace = {indexer.indexOf(unaryRule.child)};
										back[i][j].put(unaryRule.parent, trace);
									}
								}
//							}
						}
//					}
				}
			}
		}

		Tree<String> annotatedBestParse = buildTree(sentence, 0, sizeOfWords,  indexer.indexOf(root), back);
//		System.out.print(Trees.PennTreeRenderer.render(annotatedBestParse));
		return TreeAnnotations.unAnnotateTree(annotatedBestParse);
	}
	
	public Tree<String> buildUnaryTree(List<String> path, List<Tree<String>> leaves){
		List<Tree<String>> trees = leaves;
//		List<Tree<String>> trees = new ArrayList<Tree<String>>();
		for (int k = path.size() - 1; k >= 0; k--){
			trees = Collections.singletonList(new Tree<String>(path.get(k), trees));
		}
		return trees.get(0);
	}
	
	public Tree<String> buildTree(List<String> sentence, int i, int j, int parentIndex, Map<String, int[]>[][] back){
		String parent = indexer.get(parentIndex);
		int[] trace = back[i][j].get(parent);
			if (parentIndex == rootIndex && trace == null){
				return new Tree<String> (parent);
			}
			if (i==j-1){ 
				if (trace == null){
//						return new Tree<String>(parent, Collections.singletonList(new Tree<String>(sentence.get(i))));
					return new Tree<String>(sentence.get(i));
					} else {
						List<String> path = uc.getPath(new UnaryRule(parent,indexer.get(trace[0])));
						List<Tree<String>> emptyList = Collections.emptyList();
						Tree<String> leaf = new Tree<String>(sentence.get(i), emptyList);
						return buildUnaryTree(path.subList(0, path.size() - 1), Collections.singletonList(leaf));
					}
			}
			if (trace.length == 1){
				Tree<String> leaves = buildTree(sentence, i, j, trace[0], back);
				UnaryRule unaryRule = new UnaryRule(parent, indexer.get(trace[0]));
				List<String> path = uc.getPath(unaryRule);
				Tree<String> unaryTree = buildUnaryTree(path.subList(0, path.size() - 1), Collections.singletonList(leaves));
				return unaryTree;
			}
			else{
				// for binary rule
				Tree<String> leftTree = buildTree(sentence, i, trace[0], trace[1], back);
				Tree<String> rightTree = buildTree(sentence, trace[0], j, trace[2], back);
				List<Tree<String>> childrenList = new ArrayList<Tree<String>>(2);
				childrenList.add(leftTree);
				childrenList.add(rightTree);
				return new Tree<String>(parent, childrenList);
			}

	}
		
  public CKYParser2(List<Tree<String>> trainTrees) {
  	
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

  public CKYParser2(Grammar grammar, CounterMap<String, String> lexicon) {
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
  	Parser parser = new CKYParser2(grammar, lexicon);
  	String[] strArr = {"fish", "people", "fish", "tanks"};
  	List<String> sentence = Arrays.asList(strArr);
  	String root = "S";
  	Tree<String> tree = parser.getBestParse(sentence);
  	System.out.println(tree.toString());
  }

}
