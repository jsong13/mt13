package nlp.project;
import java.util.List;

public interface Translator<T> {
  public List<T> translate(List<T> srcSentence);
}
