package com.analysis.dict.controller;

import com.analysis.dict.utils.CrawlingUtils;
import io.micrometer.core.instrument.util.StringUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class CrawlingController {

  @GetMapping("/peopledict")
  public HashMap<String,String> peopledict() {
    Connection connection = null;
    Statement stmt = null;
    ResultSet rs = null;
    String result = "error";
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      String sqlUrl = "jdbc:mysql://192.168.2.81:3306/dreamsearch?serverTimezone=UTC";
      connection = DriverManager.getConnection(sqlUrl,"jwhwang","Second142857!");
      log.info("Connection Success");

      ArrayList<String> wordList = new ArrayList<>();
      stmt = connection.createStatement();
      rs = stmt.executeQuery("SELECT WORD FROM NATIVE_MEDIA_DICT WHERE GUBUN='1'");

      File file = new File("C:\\Users\\enliple\\Documents\\userdict_ko.txt");
      BufferedReader br = new BufferedReader(new FileReader(file));
      String input1;

      while((input1 = br.readLine())!=null) {
        if (!wordList.contains(input1)) {
          wordList.add(input1);
        }
      }
      while(rs.next()) {
        String input2 = rs.getString(1);
        if (!wordList.contains(input2)) {
          wordList.add(input2);
        }
      }

      BufferedWriter bw = new BufferedWriter(new FileWriter(file));

      for (String word : wordList) {
        if (!StringUtils.isEmpty(word)) {
          Pattern pattern = Pattern.compile("[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]");
          Matcher matcher = pattern.matcher(word);
          if (!matcher.find()) {
            word = word.replaceAll(" ","");
            bw.write(word + "\n");
          }
        }
      }
      bw.close();
      result = "success";
      log.info(result);
    } catch (Exception e) {
      e.printStackTrace();
    }

    HashMap<String, String> map = new HashMap<>();
    map.put("result",result);

    return map;
  }

  @GetMapping("wikipedia-people-crawling")
  public HashMap<String,String> peoplecrawling() throws IOException, ParseException {
    ArrayList<String> wordList = new ArrayList<>();
    File file = new File("C:\\Users\\enliple\\Documents\\userdict_ko.txt");
    BufferedReader br = new BufferedReader(new FileReader(file));
    String input1;
    String result = "fail";

    while((input1 = br.readLine())!=null) {
      if (!wordList.contains(input1)) {
        wordList.add(input1);
      }
    }
    br.close();

    BufferedWriter bw = new BufferedWriter(new FileWriter(file));
    String name = "";
    String title = "";
    JSONArray jsonArray = CrawlingUtils.getCategoryList();
    HashMap<String, String> map = new HashMap<>();

    for (Object obj : jsonArray) {
      title = String.valueOf(obj);
      name = "";
      while (true) {
        Element element = CrawlingUtils.getElement(title,name);
        Elements nameElements = null;
        boolean isLast = false;
        int size = 0;

        Elements tmp = element.getElementsByTag("a");
        if (!ObjectUtils.isEmpty(tmp)) {
          size = tmp.size();
          if (!"다음 페이지".equals(tmp.get(size - 1).text())) {
            isLast = true;
          }
        }

        if (!ObjectUtils.isEmpty(element)) {
          nameElements = CrawlingUtils.getNameElements(element);
          for (Element nameElement : nameElements) {
            name = CrawlingUtils.parseName(nameElement);
            if (!wordList.contains(name)) {
              log.info(name);
              wordList.add(name);
            }
          }
        }
        if (isLast) {
          break;
        }
      }

      for (String word : wordList) {
        word = word.replaceAll(" ", "");
        bw.write(word + "\n");
      }
    }
    bw.close();
    result = "success";
    map.put("result", result);
    return map;
  }
}
