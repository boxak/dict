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

  @GetMapping("wikipedia-people-crawling")
  public HashMap<String,String> peoplecrawling() throws IOException {
    ArrayList<String> wordList = new ArrayList<>();
    ArrayList<String> wordList2 = new ArrayList<>();
    File file = new File("C:/Users/Administrator/Documents/userdict_ko.txt");
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
      }
      for (String word : wordList) {
        if (!wordList2.contains(word)) {
          wordList2.add(word);
        }
      }

      for (String word : wordList2) {
        word = word.trim();
        bw.write(word + "\n");
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
    ArrayList<String> wordList2 = new ArrayList<>();
    File file = new File("C:/Users/Administrator/Documents/userdict_ko.txt");
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
            log.info(companyName);
            wordList.add(companyName);
          }
        }
      }

      for (String word : wordList) {
        if (!wordList2.contains(word)) {
          wordList2.add(word);
        }
      }

      BufferedWriter bw = new BufferedWriter(new FileWriter(file));

      for (String word : wordList2) {
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
    map.put("result",result);
    return map;
  }
}
