package nlp.project;

import nlp.project.PCFGParserTester.BinaryRule;

public class Derivation implements Comparable<Derivation> {
	double score;
	Trace trace;
	public int compareTo(Derivation der){
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
	Derivation(double score, Trace trace){
		this.score = score;
		this.trace = trace;
	}

}
