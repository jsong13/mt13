package nlp.project;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import nlp.util.Counter;

public class Evaluator {
	
	/**
	 * get the score from 1 to n gram evaluation, calculate their geometric median and multiply the brevity penalty;
	 * @param candidates - possible translated results
	 * @param references - human translation references
	 * @param n - the highest order of n gram number, we will calculate from 1 to n
	 * @return double value between 0.0 and 1.0, return -1.0 if input 
	 */
	public static double bleu(List<List<String>> candidates, List<List<String>> references, int n){
		if (candidates.size() == 0 || references.size() == 0){
			throw new IllegalStateException();
		}
		double score = 0.0;
		// c is the total length of the candidates' corpus, 
		// r is the sum of best matched length from references to each candidate
		int c = 0, r = 0;
		for (List<String> candidate : candidates){
			int candidateLength = candidate.size();	
			c += candidateLength;
			int closestRefLength = references.get(0).size();
			int distance = Math.abs( closestRefLength - candidateLength);
			if (references.size() > 1){ //we usually have only one reference translation
				for (List<String> reference : references){
					int newDistance = Math.abs(reference.size() - candidateLength);
					if (distance > newDistance){
						distance = newDistance;
						closestRefLength = reference.size();
					}
				}
			}
			r += closestRefLength;
		}
		double brevityPenalty = 0.0;
		if (c > r){
			brevityPenalty = 1.0;
		} else {
			brevityPenalty = Math.exp(1-r/c);
		}
		for (int i = 1; i <= n; i++){
			score += Math.log(score(candidates, references, i));
		}
		return brevityPenalty * Math.exp(score/n);
	}
	
	/**
	 * We first count the maximum number of times a n gram is used in any single reference translation. 
	 * The count of each candidate word is then clipped by this maximum reference count.
	 */
	public static double score(List<List<String>> candidates, List<List<String>> references, int n){
		candidates = generateNGramListOfList(candidates, n);
		references = generateNGramListOfList(references, n);
		double total = 0.0, total_clip = 0.0;
		List<Counter<String>> candidateCounterList = new ArrayList<Counter<String>>();
		for (List<String> candidate : candidates){
			Counter<String> candidateCounter = new Counter<String>();
			for (String word : candidate){
				candidateCounter.incrementCount(word, 1.0);
				total += 1.0;
			}
			candidateCounterList.add(candidateCounter);
		}
		
		List<Counter<String>> referenceCounterList = new ArrayList<Counter<String>>();
		for (List<String> reference : references){
			Counter<String> referenceCounter = new Counter<String>();
			for (String word : reference){
				referenceCounter.incrementCount(word, 1.0);
			}
			referenceCounterList.add(referenceCounter);
		}
		
		for (Counter<String> candidateCounter : candidateCounterList){
			for (Entry<String, Double> entry : candidateCounter.getEntrySet()){
				double maxMatchNum = 0.0;
				double maxAllowedMatchNum = entry.getValue();
				String matchWord = entry.getKey();
				for (Counter<String> referenceCounter : referenceCounterList){
					double countInThisReference = referenceCounter.getCount(matchWord);
					if ( countInThisReference > maxMatchNum && countInThisReference <= maxAllowedMatchNum){
						maxMatchNum = countInThisReference;
					}
				}
				total_clip += maxMatchNum;
			}
		}
		return total_clip/total;
	}
	
	public static List<List<String>> generateNGramListOfList(List<List<String>> sentences, int n){
		List<List<String>> nGramSentences = new ArrayList<List<String>>(sentences.size());
		for (List<String> sentence : sentences){
			nGramSentences.add(generateNGramList(sentence, n));
		}
		return nGramSentences;
	}
	
	/**
	 * concatenating n gram by "+"; make all words to lower case
	 */
	public static List<String> generateNGramList(List<String> sentence, int n){
		List<String> nGramSentence = new ArrayList<String>(sentence.size());
		if (sentence.size() < n){
			return nGramSentence;
		}
		for (int i = 0; i < sentence.size()-n+1; i++){
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < n; j++){
				sb.append(sentence.get(i+j).toLowerCase());
				sb.append("+");
			}
			nGramSentence.add(sb.toString());
		}
		return nGramSentence;
	}
	
	
	public static void main(String[] args){
//		String cand1 = "the the the the the the the";
		String cand1 = "It is a guide to action which ensures that the military always obeys the commands of the party";
//		String cand1 = "It is to insure the troops forever hearing the activity guidebook that party direct";
		String[] arr_cand1 = cand1.split(" ");
		List<String> candidate1 = Arrays.asList(arr_cand1);
//		String ref1 = "the cat is on the mat";
		String ref1 = "It is a guide to action that ensures that the military will forever heed Party commands";
		String ref2 = "It is the guiding priciple which guarantees the military forces always being under the command of the Party";
//		String ref2 = "It is";
		String ref3 = "It is the practical guide for the army always to heed the directions of the party";
		String[] arr_ref1 = ref1.split(" ");
		String[] arr_ref2 = ref2.split(" ");
		String[] arr_ref3 = ref3.split(" ");
		List<String> reference1 = Arrays.asList(arr_ref1);
		List<String> reference2 = Arrays.asList(arr_ref2);
		List<String> reference3 = Arrays.asList(arr_ref3);
		List<List<String>> candidates = new ArrayList<List<String>>();
		candidates.add(candidate1);
		List<List<String>> references = new ArrayList<List<String>>();
		references.add(reference1);
		references.add(reference2);
		references.add(reference3);
		double score = bleu(candidates, references, 3);
		System.out.println(score);
//		List<String> tempList = generateNgramList(candidate1, 8);
//		for (String str:tempList){
//			System.out.print(str + ", ");
//		}
	}
	
}
