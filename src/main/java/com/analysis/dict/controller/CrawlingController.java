package com.analysis.dict.controller;

import com.analysis.dict.utils.CrawlingUtils;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@Slf4j
public class CrawlingController {

//  @GetMapping("/peopledict")
//  public HashMap<String,String> peopledict() {
//    Connection connection = null;
//    Statement stmt = null;
//    ResultSet rs = null;
//    String result = "error";
//    try {
//      Class.forName("com.mysql.cj.jdbc.Driver");
//      String sqlUrl = "jdbc:mysql://192.168.2.81:3306/dreamsearch?serverTimezone=UTC";
//      connection = DriverManager.getConnection(sqlUrl,"jwhwang","Second142857!");
//      log.info("Connection Success");
//
//      ArrayList<String> wordList = new ArrayList<>();
//      stmt = connection.createStatement();
//      rs = stmt.executeQuery("SELECT WORD FROM NATIVE_MEDIA_DICT WHERE GUBUN='1'");
//
//      File file = new File("C:\\Users\\enliple\\Documents\\userdict_ko.txt");
//      BufferedReader br = new BufferedReader(new FileReader(file));
//      String input1;
//
//      while((input1 = br.readLine())!=null) {
//        if (!wordList.contains(input1)) {
//          wordList.add(input1);
//        }
//      }
//      while(rs.next()) {
//        String input2 = rs.getString(1);
//        if (!wordList.contains(input2)) {
//          wordList.add(input2);
//        }
//      }
//
//      BufferedWriter bw = new BufferedWriter(new FileWriter(file));
//
//      for (String word : wordList) {
//        if (StringUtils.isNotEmpty(word)) {
//          Pattern pattern = Pattern.compile("[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]");
//          Matcher matcher = pattern.matcher(word);
//          if (!matcher.find()) {
//            word = word.replaceAll(" ","");
//            bw.write(word + "\n");
//          }
//        }
//      }
//      bw.close();
//      result = "success";
//      log.info(result);
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//
//    HashMap<String, String> map = new HashMap<>();
//    map.put("result",result);
//
//    return map;
//  }

  @GetMapping("wikipedia-people-crawling")
  public HashMap<String,String> peoplecrawling() throws IOException {
    ArrayList<String> wordList = new ArrayList<>();
    File file = new File("/home/ubuntu/app/git/userdict.txt");
    BufferedReader br = new BufferedReader(new FileReader(file));
    HashMap<String, String> map = new HashMap<>();
    String input1;
    String result = "fail";

    try {
      while ((input1 = br.readLine()) != null) {
        if (!wordList.contains(input1)) {
          wordList.add(input1);
        }
      }
      br.close();

      BufferedWriter bw = new BufferedWriter(new FileWriter(file));
      String name = "";
      String title = "";
      JSONArray jsonArray = CrawlingUtils.getCategoryList();

      for (Object obj : jsonArray) {
        title = String.valueOf(obj);
        name = "";
        while (true) {
          Element element = CrawlingUtils.getPeopleListElement(title, name);
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
            nameElements = CrawlingUtils.getPeopleNameElements(element);
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
    } catch (Exception e) {
      e.printStackTrace();
    }
    map.put("result", result);
    return map;
  }

  @GetMapping("companycrawling")
  public HashMap<String, String> crawlingCompany() throws IOException {
    ArrayList<String> wordList = new ArrayList<>();
    File file = new File("/home/ubuntu/app/git/userdict.txt");
    log.info(file.getAbsolutePath());
    BufferedReader br = new BufferedReader(new FileReader(file));
    String input1;
    String result = "fail";
    HashMap<String, String> map = new HashMap<>();
    try {
      Elements companyElements = CrawlingUtils.getCompanyNameElements();
      while ((input1 = br.readLine()) != null) {
        if (!wordList.contains(input1)) {
          wordList.add(input1);
        }
      }
      br.close();

      log.info(String.valueOf(wordList.size()));

      for (Element companyElement : companyElements) {
        String companyName = CrawlingUtils.parseName(companyElement);
        if (StringUtils.isNotEmpty(companyName)) {
          if (!wordList.contains(companyName)) {
            //log.info(companyName);
            wordList.add(companyName);
          }
        }
      }

      BufferedWriter bw = new BufferedWriter(new FileWriter(file));

      for (String word : wordList) {
        if (StringUtils.isNotEmpty(word)) {
          //log.info(word);
          bw.write(word + "\n");
        }
      }
      bw.close();
      result = "success";
    } catch (Exception e) {
      e.printStackTrace();
    }
    log.info("github hook working!");
    map.put("result",result);
    return map;
  }
}
