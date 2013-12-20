package nlp.project;

import nlp.project.PCFGParserTester.BinaryRule;
import nlp.project.PCFGParserTester.UnaryRule;

public class Trace implements Comparable<Trace> {
	Double score;
	int split;
//	String leftChild;
//	String rightChild;
	BinaryRule binaryRule;
	UnaryRule unaryRule;
	int leftRank;
	int rightRank;
	Trace(double score){
		this.score = score;
	}
	
	public int compareTo(Trace der){
		if (this != der){
			if (score > der.score){
				return 1;
			} else if (score < der.score){
				return -1;
			} else {
				return 0;
			}
		}
		return 0;
	}
	
	Trace(double score, int split, BinaryRule binaryRule, UnaryRule unaryRule){
		this.score = score;
		this.split = split;
		this.binaryRule = binaryRule;
		this.unaryRule = unaryRule;
	}
	
	@Override
	public String toString(){
		if (binaryRule != null){
			return binaryRule.toString() + " score: " + score + " split: " + split + " leftRank: " + leftRank + " rightRank: " + rightRank;
		} else if (unaryRule != null){
			return unaryRule.toString() + " score: " + score + " split: " + split;
		} else {
			return "score: " + score + " split: " + split;
		}

	}

}
