package ccls.pearson.teacher.evaluation;

import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by annapurna on 9/16/14.
 */
public class TeachEvalGetData {
  private int startYear, endYear;
  private static final String baseURI = "http://api.nytimes" +
      ".com/svc/search/v2/articlesearch";
  private static final String apiKey =
      "637e12e0c2f2dfbd50195d65def6ed40:18:69488624";
  private static final String queryParemeter = "?q=teacher+evaluation";
  private static final String beginDateParameter = "&begin_date=";
  private static final String endDateParameter = "&end_date=";
  private static final String responseFormatParameter = ".json";
  private static final String sortParameter = "&sort=oldest";
  private static final String fieldParameter = "&fl=web_url,snippet," +
      "lead_paragraph,abstract,headline,keywords,pub_date,document_type,byline";
  private static final String h1Parameter = "&hl=true";
  private static final String pageQueryParameter = "&page=";
  private static final String apiKeyParamater = "&api-key=";
  private static final String DEFAULT_OUTPUT_PATH = "/tmp/teachEvalOut.csv";
  private static final JsonParser parser = new JsonParser();
  private static final SimpleDateFormat fomatter = new SimpleDateFormat
      ("yyyyMMdd");
  private final List<NewsEntity> newsEntityList = new ArrayList<NewsEntity>();

  public TeachEvalGetData(int startYear, int endYear) {
    this.startYear = startYear;
    this.endYear = endYear;
  }

  public static void main(String[] args) {
    int startYear, endYear;
    String fileOutputPath;

    if (args.length < 2) {
      System.out.println("Insufficient number of arguments, exiting");
      System.exit(-1);
    }

    startYear = Integer.parseInt(args[0]);
    endYear = Integer.parseInt(args[1]);
    if (endYear < startYear) {
      endYear = startYear;
    }

    if (args.length < 3) {
      fileOutputPath = DEFAULT_OUTPUT_PATH;
    } else {
      fileOutputPath = args[2];
    }

    TeachEvalGetData teachEvalGetData = new TeachEvalGetData(startYear,
        endYear);
    try {
      teachEvalGetData.launchGETRequests();
    } catch (Exception e) {
      e.printStackTrace();
    }

    File file = new File(fileOutputPath);
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      FileWriter fw = new FileWriter(file.getAbsoluteFile());

      BufferedWriter bw = new BufferedWriter(fw);
      for (NewsEntity newsEntity : teachEvalGetData.getNewsEntityList()) {
        bw.write(newsEntity.toString());
        bw.newLine();
      }
      bw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("Done");
  }

  private void launchGETRequests() throws Exception {
    int currentYear = startYear;
    while (currentYear <= endYear) {
      int page = 0;
      String startDate = getDateOfYear(currentYear, true);
      String endDate = getDateOfYear(currentYear, false);
      while (launchGETRequestForPage(page, startDate, endDate)) {
        page++;
      }
      currentYear++;
    }
  }

  private String getDateOfYear(int year, boolean isStartOfYear) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, year);
    if (isStartOfYear) {
      calendar.set(Calendar.MONTH, Calendar.JANUARY);
      calendar.set(Calendar.DAY_OF_MONTH, 1);
    } else {
      calendar.set(Calendar.MONTH, Calendar.DECEMBER);
      calendar.set(Calendar.DAY_OF_MONTH, 31);
    }
    return fomatter.format(calendar.getTime());
  }

  private boolean launchGETRequestForPage(int pageNumber,
                                          String startDate,
                                          String endDate) throws Exception {
    String url = createURL(pageNumber, startDate, endDate);
    URL obj = new URL(url);
    System.out.println("Launching query for url:" + url);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    con.setRequestMethod("GET");

    int responseCode = con.getResponseCode();
    if (responseCode != 200) {
      System.out.println("Error response code["+ responseCode + " for url " +
          url);
      return false;
    }
    String responseJson;
    BufferedReader in = new BufferedReader(
        new InputStreamReader(con.getInputStream()));
    try {
      responseJson = in.readLine();
    } finally {
      in.close();
    }

    return parseResponseJson(responseJson);
  }

  private boolean parseResponseJson(String responseJson) {
    if (responseJson == null || responseJson.length() == 0) {
      return false;
    }
    JsonObject responseObject = (JsonObject) parser.parse(responseJson);
    JsonArray docsArray = responseObject.getAsJsonObject("response")
        .getAsJsonArray("docs");
    if (docsArray == null) {
      return false;
    }
    for (JsonElement docElement : docsArray) {
      parseDocObject(docElement);
    }
    if (docsArray.size() < 10) {
      return false;
    }
    return true;
  }

  private void parseDocObject(JsonElement docElement) {
    JsonObject docObject = docElement.getAsJsonObject();
    NewsEntity newsEntity = new NewsEntity();
    newsEntity.setWebUrl(docObject.get("web_url").getAsString());
    newsEntity.setHeadline(docObject.getAsJsonObject("headline").get("main")
        .getAsString());
    newsEntity.setPublishedDate(docObject.get("pub_date").getAsString());
    newsEntity.setSnippet(docObject.get("snippet").getAsString());
    JsonElement paragraphEl = docObject.get("lead_paragraph");
    if (paragraphEl.getClass() != JsonNull.class) {
      newsEntity.setLeadParagraph(paragraphEl.getAsString());
    }
    JsonElement abstractEl = docObject.get("abstract");
    if (abstractEl.getClass() != JsonNull.class) {
      newsEntity.setAbstractString(abstractEl.getAsString());
    }
    JsonElement docTypeEl = docObject.get("document_type");
    if (docTypeEl.getClass() != JsonNull.class) {
      newsEntity.setDocumentType(docTypeEl.getAsString());
    }
    newsEntity.setAuthor(getAuthor(docObject));
    newsEntity.setKeyWords(getKeyWords(docObject));
    newsEntityList.add(newsEntity);
  }

  private Map<String, List<String>> getKeyWords(JsonObject docObject) {
    Map<String, List<String>> keyWordsMap = new HashMap<String, List<String>>();
    JsonArray keyWordsArray = docObject.getAsJsonArray("keywords");
    for (JsonElement keyWordEle : keyWordsArray) {
      JsonObject keyWordObj = keyWordEle.getAsJsonObject();
      String key = keyWordObj.get("name").getAsString();
      List<String> valueList = keyWordsMap.get(key);
      if (valueList == null) {
        valueList = new ArrayList<String>();
      }
      valueList.add(keyWordObj.get("value").getAsString());
      keyWordsMap.put(key, valueList);
    }
    return keyWordsMap;
  }

  private String getAuthor(JsonObject docObject) {
    JsonElement bylineEl = docObject.get("byline");
    if (bylineEl.getClass() != JsonNull.class) {
      try {
        return bylineEl.getAsJsonObject().get("original").getAsString();
      } catch (Exception e) {
        System.out.println("Found incorrect json string at byline for a news " +
            "article" + docObject.toString());
      }
    }
    return "";
  }

  private String createURL(int pageNumber, String beginDate, String endDate) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(baseURI);
    buffer.append(responseFormatParameter);
    buffer.append(queryParemeter);
    buffer.append(beginDateParameter);
    buffer.append(beginDate);
    buffer.append(endDateParameter);
    buffer.append(endDate);
    buffer.append(sortParameter);
    buffer.append(fieldParameter);
    buffer.append(h1Parameter);
    buffer.append(pageQueryParameter);
    buffer.append(pageNumber);
    buffer.append(apiKeyParamater);
    buffer.append(apiKey);
    return buffer.toString();
  }

  public List<NewsEntity> getNewsEntityList() {
    return newsEntityList;
  }
}
