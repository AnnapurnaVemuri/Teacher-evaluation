package ccls.pearson.teacher.evaluation;

import java.util.List;
import java.util.Map;

/**
 * Created by annapurna on 9/16/14.
 */
public class NewsEntity {
  private String headline;
  private String webUrl;
  private String snippet;
  private String abstractString;
  private String leadParagraph;
  private String publishedDate;
  private String author;
  private String documentType;
  private Map<String, List<String>> keyWords;
  private static final String DELIMITER = "*";

  public void setWebUrl(String webUrl) {
    this.webUrl = webUrl;
  }

  public void setSnippet(String snippet) {
    this.snippet = snippet;
  }

  public void setPublishedDate(String publishedDate) {
    this.publishedDate = publishedDate;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public void setKeyWords(Map<String, List<String>> keyWords) {
    this.keyWords = keyWords;
  }

  public void setHeadline(String headline) {
    this.headline = headline;
  }

  public void setAbstractString(String abstractString) {
    this.abstractString = abstractString;
  }

  public void setLeadParagraph(String leadParagraph) {
    this.leadParagraph = leadParagraph;
  }

  public void setDocumentType(String documentType) {
    this.documentType = documentType;
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(headline);
    buffer.append(DELIMITER);
    buffer.append(webUrl);
    buffer.append(DELIMITER);
    buffer.append(author);
    buffer.append(DELIMITER);
    buffer.append(publishedDate);
    buffer.append(DELIMITER);
    buffer.append(snippet);
    buffer.append(DELIMITER);
    buffer.append(leadParagraph);
    buffer.append(DELIMITER);
    buffer.append(documentType);
    buffer.append(DELIMITER);
    buffer.append(abstractString);
    buffer.append(DELIMITER);
    int mapIndex = 0;
    for (Map.Entry<String, List<String>> entry : keyWords.entrySet()) {
      if (mapIndex > 0) {
        buffer.append("|");
      }
      buffer.append(entry.getKey());
      buffer.append(":");
      for (int i = 0; i < entry.getValue().size(); i++) {
        if (i > 0) {
          buffer.append(",");
        }
        buffer.append(entry.getValue().get(i));
      }
      mapIndex++;
    }
    return buffer.toString();
  }
}
