package nlp.project;
import java.io.*;
import java.util.*;

public class Vocabulary{
  Map<String, Integer> word2ind = new HashMap<String, Integer>();
  Map<Integer, String> ind2word = new HashMap<Integer, String>();

  public Vocabulary() {
    throw new RuntimeException("should not reach here");
  }
  public Vocabulary(String path) throws IOException {
    BufferedReader reader = null;
    try{ 
      reader = new BufferedReader(new InputStreamReader(
            new FileInputStream(path),"UTF-8")); 
      String line = null;

      while ( (line = reader.readLine()) != null ) {
        String[] parts = line.split("\\s+"); 
        if (parts.length != 3) throw new IOException();

        int ind = Integer.parseInt(parts[0]);
        String word = parts[1];
        ind2word.put(ind, word);
        word2ind.put(word,ind);
      }
    } catch (Exception e) {
      throw new IOException("wrong vcb file: " + path); 
    } finally{
      reader.close();
    }
  }

  public Integer getIndex(String word){
    return word2ind.get(word);
  }

  public String getWord(int ind) {
    return ind2word.get(ind);
  }

}
