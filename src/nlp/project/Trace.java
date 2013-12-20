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
	
	@Override
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
	
	@Override 
	public boolean equals(Object obj){
	  if (obj == this) return true;
	  if (obj == null) return false;
	  if (! (obj instanceof Trace)) return false;
	  Trace other = (Trace)obj;
	  if (this.binaryRule != null && !this.binaryRule.equals(other.binaryRule)){
	    return false;
	  }
	  if (this.unaryRule != null && !this.unaryRule.equals(other.unaryRule)){
	    return false;
	  }
	  return this.score == other.score && this.split == other.split
	      && this.leftRank == other.leftRank && this.rightRank == other.rightRank;
	}
	
	@Override
	public int hashCode(){
	  int hash = 7;
	  if (binaryRule != null){
	    hash = hash * 31 + binaryRule.hashCode();
	  }
	  if (unaryRule != null){
	    hash = hash * 31 + unaryRule.hashCode();
	  }
    hash = hash * 31 + split;
    hash = hash * 31 + leftRank;
    hash = hash *31 + rightRank;
	  return hash * 31 + Double.valueOf(score).hashCode();
	  
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
