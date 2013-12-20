package nlp.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import nlp.project.PCFGParserTester.BinaryRule;
import nlp.project.PCFGParserTester.Grammar;
import nlp.project.PCFGParserTester.Lexicon;
import nlp.project.PCFGParserTester.Parser;
import nlp.project.PCFGParserTester.TreeAnnotations;
import nlp.project.PCFGParserTester.UnaryClosure;
import nlp.project.PCFGParserTester.UnaryRule;
import nlp.ling.Tree;
import nlp.ling.Trees;
import nlp.util.Counter;
import nlp.util.CounterMap;
import nlp.util.Indexer;

public class CKYParser4 implements Parser {
	static final String root = "S";
	static int rootIndex;

  CounterMap<List<String>, Tree<String>> knownParses;
  CounterMap<Integer, String> spanToCategories;
  Indexer<String> indexer;
  Lexicon lexicon;
  Grammar grammar;
  UnaryClosure uc;
  CounterMap<String, String> tempLexicon;
//	int[] triple = new int[3]; // A->BC, [0]=split, [1]=B, [2]=C; [0]=0 means unary
	@Override
	public Tree<String> getBestParse(List<String> sentence) {
		int sizeOfWords = sentence.size();
		int sizeOfNonTer = indexer.size();
		double[][][] score= new double[sizeOfWords+1][sizeOfWords+1][sizeOfNonTer];
		int[][][][] backTrack = new int[sizeOfWords+1][sizeOfWords+1][sizeOfNonTer][3];
		for (int i=0; i<sizeOfWords; i++){
			
			Counter<String> tagCounter = lexicon.wordToTagCounters.getCounter(sentence.get(i));
			Set<String> tagSet;
			if (tagCounter.isEmpty()) {
				tagSet = lexicon.getAllTags();
			} else {
				tagSet = tagCounter.keySet();
			}
			for (String tag:tagSet){
				score[i][i+1][indexer.indexOf(tag)] = lexicon.scoreTagging(sentence.get(i), tag);
			
//			Counter<String> tagCounter = tempLexicon.getCounter(sentence.get(i));
//			for (Entry<String, Double> entry:tagCounter.getEntrySet()){
//				String tag = entry.getKey();
//				score[i][i+1][indexer.indexOf(tag)] = entry.getValue();
//				System.out.format("%d %d %s %.12f%n", i, i+1, tag +" -> " + sentence.get(i),entry.getValue());

//				System.out.format("%d %d %s %.12f%n", i, i+1, tag +" -> " + sentence.get(i),lexicon.scoreTagging(sentence.get(i), tag));
				List<UnaryRule> unaryRules = uc.getClosedUnaryRulesByChild(tag);
				for (UnaryRule unaryRule:unaryRules){
					double prob = score[i][i+1][indexer.indexOf(tag)];
					if (prob != 0){
						prob *= unaryRule.score;
						if (prob > score[i][i+1][indexer.indexOf(unaryRule.parent)]){
							score[i][i+1][indexer.indexOf(unaryRule.parent)] = prob;
							backTrack[i][i+1][indexer.indexOf(unaryRule.parent)][0]=i;
							backTrack[i][i+1][indexer.indexOf(unaryRule.parent)][1]=indexer.indexOf(tag);
//							System.out.format("%d %d %s %.12f%n", i, i+1,unaryRule.parent + " -> " + tag,prob);
						}
					}
				}
			}
//			System.out.println("-------------------");
		}
		int leftIndex;
		int rightIndex;
		for (int span = 2; span <= sizeOfWords; span++){
			for (int i = 0; i <= sizeOfWords - span; i++){
				int j = i + span;
				for (int parentIndex = 1; parentIndex < sizeOfNonTer; parentIndex++){
//						System.out.println(indexer.get(parentIndex));
						//set score by binary rule
					List<BinaryRule> binaryRules = grammar.getBinaryRulesByParent(indexer.get(parentIndex));
					for (BinaryRule binaryRule:binaryRules){
						leftIndex = indexer.indexOf(binaryRule.leftChild);
						rightIndex = indexer.indexOf(binaryRule.rightChild);
						for (int k = i+1; k < j; k++){
							double leftChildScore = score[i][k][leftIndex];
							double rightChildScore = score[k][j][rightIndex];
							if (leftChildScore!=0 && rightChildScore!=0){
								double prob = leftChildScore * rightChildScore * binaryRule.getScore();
								if (prob > score[i][j][parentIndex]){
									score[i][j][parentIndex] = prob;
									backTrack[i][j][parentIndex][0] = k;
									backTrack[i][j][parentIndex][1] = leftIndex;
									backTrack[i][j][parentIndex][2] = rightIndex;
//									System.out.format("%d %d %s %.12f%n", i, j, binaryRule.parent +" -> " + binaryRule.leftChild +" " + binaryRule.rightChild,prob);
								}			
							}
						}
					}
						//set score by unary rule
					List<UnaryRule> unaryRules = uc.getClosedUnaryRulesByParent(indexer.get(parentIndex));						
//					boolean hasUpdated = true;
//					while (hasUpdated == true) {
//						hasUpdated = false;
						for (UnaryRule unaryRule:unaryRules){
							double prob = score[i][j][indexer.indexOf(unaryRule.child)];
							if (prob!=0){
								prob *= unaryRule.getScore();
								if (prob > score[i][j][parentIndex]){
//									hasUpdated = true;
									score[i][j][parentIndex] = prob;
									backTrack[i][j][parentIndex][0]=0;
									backTrack[i][j][parentIndex][1]=indexer.indexOf(unaryRule.child);
//									System.out.format("%d %d %s %.12f%n", i, j, unaryRule.parent +" -> " + unaryRule.child ,prob);
								}
							}
						}
//					}
//					System.out.println("--------------------------------");
				}
			}
		}
		Tree<String> annotatedBestParse = buildTree(sentence, 0, sizeOfWords,  indexer.indexOf(root), backTrack);
//		System.out.print(Trees.PennTreeRenderer.render(annotatedBestParse));
		return TreeAnnotations.unAnnotateTree(annotatedBestParse);
	}
	
	public Tree<String> buildUnaryTree(List<String> path, List<Tree<String>> leaves){
		List<Tree<String>> trees = leaves;
		for (int k = path.size() - 1; k >= 0; k--){
			trees = Collections.singletonList(new Tree<String>(path.get(k), trees));
		}
		return trees.get(0);
	}
	
	public Tree<String> buildTree(List<String> sentence, int i, int j, int parent, int[][][][] backTrack){
		int leftChildIndex = backTrack[i][j][parent][1];
//		System.out.println("parent: " + indexer.get(parent));
//		System.out.println("leftChild: " + indexer.get(leftChildIndex));
//		System.out.println("rightChild: " + indexer.get(backTrack[i][j][parent][2]));
//		System.out.println("------------------------------------");
//		if (leftChildIndex == 0){
//			return new Tree<String>(indexer.get(parent), Collections.singletonList(new Tree<String>(sentence.get(leftChildIndex))));
//		}
//		if (leftChildIndex != 0){
			if (parent == rootIndex && leftChildIndex == 0){
				return new Tree<String> (indexer.get(parent));
			}
			if (i==j-1){ 
//				if (uc.getClosedUnaryRulesByParent(indexer.get(parent)).size() == 0){
			if (leftChildIndex == 0){
					return new Tree<String>(indexer.get(parent), Collections.singletonList(new Tree<String>(sentence.get(i))));
				} else {
					List<String> path = uc.getPath(new UnaryRule(indexer.get(parent),indexer.get(leftChildIndex)));
					List<Tree<String>> emptyList = Collections.emptyList();
					Tree<String> leaf = new Tree<String>(sentence.get(i), emptyList);
					return buildUnaryTree(path, Collections.singletonList(leaf));
				}
			}
//		}
			// for unary rule
//			try{
				if (backTrack[i][j][parent][0] == 0){
					Tree<String> leaves = buildTree(sentence, i, j, leftChildIndex, backTrack);
					UnaryRule unaryRule = new UnaryRule(indexer.get(parent), indexer.get(leftChildIndex));
					List<String> path = uc.getPath(unaryRule);
					Tree<String> unaryTree = buildUnaryTree(path.subList(0, path.size() - 1), Collections.singletonList(leaves));
					return unaryTree;
				}
//			} catch (ArrayIndexOutOfBoundsException e){
////				System.out.format("i: %d, j: %d, parent: %d", i, j, parent);
//				throw e;
//			}
//			}
			
			// for binary rule
			Tree<String> leftTree = buildTree(sentence, i, backTrack[i][j][parent][0], leftChildIndex, backTrack);
			Tree<String> rightTree = buildTree(sentence, backTrack[i][j][parent][0], j, backTrack[i][j][parent][2], backTrack);
			List<Tree<String>> childrenList = new ArrayList<Tree<String>>(2);
			childrenList.add(leftTree);
			childrenList.add(rightTree);
			return new Tree<String>(indexer.get(parent), childrenList);

	}
		
  public CKYParser4(List<Tree<String>> trainTrees) {
  	
    System.out.print("Annotating / binarizing training trees ... ");
    List<Tree<String>> annotatedTrainTrees = annotateTrees(trainTrees);
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

  public CKYParser4(Grammar grammar, CounterMap<String, String> lexicon) {
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
    if (!category.equals("ROOT"))
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
  	Parser parser = new CKYParser4(grammar, lexicon);
  	String[] strArr = {"fish", "people", "fish", "tanks"};
  	List<String> sentence = Arrays.asList(strArr);
  	String root = "S";
  	Tree<String> tree = parser.getBestParse(sentence);
  	System.out.println(tree.toString());
  }

}
