package com.analysis.dict.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Objects;

@Slf4j
public class CrawlingUtils {
  public static JSONArray getCategoryList() {
    JSONArray jsonArray = null;
    try(InputStream in = CrawlingUtils.class.getResourceAsStream("/static/resources/categoryList.json")) {
      byte[] data = IOUtils.toByteArray(in);
      String jsonStr = new String(data);
      JSONParser parser = new JSONParser();
      JSONObject jsonObject = (JSONObject) parser.parse(jsonStr);
      jsonArray = (JSONArray) parser.parse(String.valueOf(jsonObject.get("category")));
    } catch (IOException | ParseException e) {
      e.printStackTrace();
    }

    if (CollectionUtils.isEmpty(jsonArray)) jsonArray = new JSONArray();

    return jsonArray;
  }

  public static Element getPeopleListElement(String title, String fromName) {
    String url = "";
    Element element = null;
    try {
      url = "https://ko.wikipedia.org/w/index.php?title=" + URLEncoder.encode(title, "UTF-8")
          + "&pagefrom=" + URLEncoder.encode(fromName, "UTF-8");
      Document document = Jsoup.connect(url).get();
      Elements elements = document.select("div#mw-pages");
      if (!ObjectUtils.isEmpty(elements)) {
        element = elements.get(0);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return element;
  }

  public static Elements getPeopleNameElements(Element element) {
    Elements elements = element.select("div.mw-category");
    Element element2 = null;
    Elements nameElements = null;
    String name = "";
    if (!ObjectUtils.isEmpty(elements)) {
      element2 = elements.get(0);
    }
    if (!ObjectUtils.isEmpty(element2)) {
      nameElements = elements.first().getElementsByTag("a");
    }
    if (Objects.isNull(nameElements)) {
      nameElements = new Elements();
    }
    return nameElements;
  }

  public static Elements getCompanyNameElements() {
    Elements elements = new Elements();
    try {
      String url = "https://ko.wikipedia.org/wiki/"+URLEncoder.encode("대한민국의_기업_목록","UTF-8");
      Document document = Jsoup.connect(url).get();
      Element divElement = document.getElementById("mw-content-text");
      Element ulElement = divElement.select("ul").get(0);
      elements = ulElement.select("li");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return elements;
  }

  public static String parseName(Element nameElement) {
    String name = nameElement.text();
    if (name.contains("(")) {
      int index = name.indexOf("(");
      name = name.substring(0,index);
    }
    name = name.replaceAll("[^\uAC00-\uD7A3xfe0-9a-zA-Z]","");
    return name.trim();
  }
}
