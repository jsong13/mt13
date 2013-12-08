package nlp.project;
import java.io.*;
import java.util.*;
import nlp.util.*;

public class IOUtils{

  public static List<SentencePair<Integer>> loadWA(String path) throws IOException {
    List<SentencePair<Integer>> ret = new ArrayList<SentencePair<Integer>>();
    BufferedReader reader = null;

    try{
      // 1 for lineno, 2 for source, 3 for target, 4 for wa-st 5 for wa-ts
      // and we assume initially that we just finish the last record
      int preStatus = 5;  

      SentencePair<Integer> currentSP = null;
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(path))); 
      String line=null;
 
      while ( (line = reader.readLine()) != null ) {
        String[] parts = line.trim().split("\\s+");

        if (parts.length == 1 && parts[0].startsWith("#") 
            && parts[0].endsWith("##") && preStatus == 5) {
          currentSP = new SentencePair<Integer>();
          preStatus = 1;
          continue;
        }

        if (preStatus == 1) {
          for (String a : parts) 
            currentSP.addSrcWord(Integer.parseInt(a)); 
          preStatus++;
          continue;
        }

        if (preStatus == 2) {
          for (String a : parts) 
            currentSP.addTrgWord(Integer.parseInt(a)); 
          preStatus++;
          continue;
        }

        if (preStatus == 3) {
          for (String a: parts)
            currentSP.addWAs2t(Integer.parseInt(a));
          preStatus++;
          continue;
        }

        if (preStatus == 4) {
          for (String a: parts)
            currentSP.addWAt2s(Integer.parseInt(a));
          
          currentSP.finishRead();
          ret.add(currentSP);
          currentSP = null;
          preStatus++;
          continue;
        }

        throw new IOException();
      }
    } catch (Exception e) {
      throw new IOException("Wrong wa format in " + path);
    } finally {
      reader.close();
    }

    return ret;
  }

    
  public static List<SentencePair<Integer>> loadSNT(String path) throws IOException {
    List<SentencePair<Integer>> ret = new ArrayList<SentencePair<Integer>>();
    BufferedReader reader = null;

    try{
      int preStatus = 1;  // 1 for separator, 2 for source, 3 for target
      SentencePair<Integer> currentSP = null;
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(path))); 
      String line=null;
 
      while ( (line = reader.readLine()) != null ) {
        String[] parts = line.split("\\s+");

        if (parts.length == 1 && parts[0].equals("1") && preStatus != 2) {
          preStatus = 1;
          continue;
        }

        if (parts.length == 0) continue;

        if (preStatus == 1) {
          currentSP = new SentencePair<Integer>();
          for (String a : parts) 
            currentSP.addSrcWord(Integer.parseInt(a)); 
          preStatus = 2;
          continue;
        }

        if (preStatus == 2) {
          for (String a : parts) 
            currentSP.addTrgWord(Integer.parseInt(a)); 
          ret.add(currentSP);
          preStatus = 3;
          continue;
        }
        throw new IOException("Wrong snt format in " + path);
      }
    } catch (Exception e) {
      throw new IOException("Wrong snt format in " + path);
    } finally {
      reader.close();
    }

    return ret;
  }

  public static List<SentencePair<String>> loadParallelText(String spath, String tpath) 
    throws IOException {

    List<SentencePair<String>> ret = new ArrayList<SentencePair<String>>();
    BufferedReader reader1 = null;
    BufferedReader reader2 = null;

    try {
      reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(spath),"UTF-8")); 
      reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(tpath),"UTF-8")); 
      while(true) {
        String line1 = reader1.readLine();
        String line2 = reader2.readLine();
        if ((line1 == null && line2 != null) || (line2 == null && line1 != null))
          throw new IOException();
        if (line1 == null && line2 == null) break;
        SentencePair<String> csp = new SentencePair<String>();
        for (String a : line1.split("\\s+")) csp.addSrcWord(a);
        for (String a : line2.split("\\s+")) csp.addTrgWord(a);
        ret.add(csp);
      }

    } catch( Exception e) {
      throw new IOException("Wrong parallel corpus: " + spath + ", " + tpath); 
    } finally {
      reader1.close();
      reader2.close();
    }
    return ret;
  }

  public static CounterMap<Integer, Integer> loadT3(String path) throws IOException {
    CounterMap<Integer, Integer> ret = new CounterMap();
    BufferedReader reader = null;
    try{
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(path))); 
      String line=null;

      while ((line = reader.readLine()) != null) {
        String[] parts = line.split("\\s+");
        int i = Integer.parseInt(parts[0]);
        int j = Integer.parseInt(parts[1]);
        double f = Double.parseDouble(parts[2]);
        ret.incrementCount(i,j,f);
      }
    } catch(Exception e){
      throw new IOException("Wrong t3 format: " + path);
    } finally {
      reader.close();
    }

    return ret;
  }

}
