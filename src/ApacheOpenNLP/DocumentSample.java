package ApacheOpenNLP;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class which holds a classified document and its category.
 */
public class DocumentSample implements Serializable {

  private final String category;
  private final List<String> text;
  private final Map<String, Object> extraInformation;

  public DocumentSample(String category, String[] text) {
    this(category, text, null);
  }

  public DocumentSample(String category, String[] text, Map<String, Object> extraInformation) {
    Objects.requireNonNull(text, "text must not be null");

    this.category = Objects.requireNonNull(category, "category must not be null");
    this.text = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(text)));

    if (extraInformation == null) {
      this.extraInformation = Collections.emptyMap();
    } else {
      this.extraInformation = extraInformation;
    }
  }

  public String getCategory() {
    return category;
  }

  public String[] getText() {
    return text.toArray(new String[text.size()]);
  }

  public Map<String, Object> getExtraInformation() {
    return extraInformation;
  }

  @Override
  public String toString() {

    StringBuilder sampleString = new StringBuilder();

    sampleString.append(category).append('\t');

    for (String s : text) {
      sampleString.append(s).append(' ');
    }

    if (sampleString.length() > 0) {
      // remove last space
      sampleString.setLength(sampleString.length() - 1);
    }

    return sampleString.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getCategory(), Arrays.hashCode(getText()));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof DocumentSample) {
      DocumentSample a = (DocumentSample) obj;

      return getCategory().equals(a.getCategory())
          && Arrays.equals(getText(), a.getText());
    }

    return false;
  }
}