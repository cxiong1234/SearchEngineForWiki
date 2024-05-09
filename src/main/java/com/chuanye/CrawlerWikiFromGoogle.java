package com.chuanye;

import org.json.simple.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;
import java.io.FileWriter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.select.Elements;
import java.io.FileReader;
import java.io.Reader;

import java.io.BufferedWriter;
import java.io.File;


public class CrawlerWikiFromGoogle {
    public static void main(String[] args) {



    }
    public void runEntry(Integer dataNum, List<String> keywords_list){

        //Integer dataNum = 30;
        //List<String> keywords_list = new ArrayList<>(Arrays.asList("dna", "rna", "protein", "cell", "gene"));
        String folderPath = "./generatedArticles";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // get the JsonArray allData
        JSONArray allData = getAllDataJsonArray(keywords_list, dataNum);

        System.out.println("The data amount we got:"+ String.valueOf(allData.size()));
        System.out.println("Writting to CSV");


        writeURLToCsV(allData);

        System.out.println("Saving articles text to txt");

        // get a long string with all articles save it to txt
        csvUrlCrawler(folderPath + "/keywordsAndLinksTitles.csv");

        //
    }


    public static JSONArray getAllDataJsonArray(List<String> keywords_list, Integer dataNum){
        JSONArray allData = new JSONArray();
        Integer iterNum = (dataNum % 10) == 0 ? (Integer)(dataNum/10) : (Integer)(dataNum/10) + 1;
        for (String keyword : keywords_list) {
            for (int i = 0; i < iterNum; i++) {
                Integer start = i * 10 + 1;
                String url;
                if ((dataNum % 10) != 0 && i == iterNum - 1) {
                    Integer num = dataNum % 10;
                    url = "https://www.googleapis.com/customsearch/v1?key=AIzaSyD8vnm6IY4jTZxZ7kMQF3sjm2JeQdbv13A&cx=0357dbfa7b7d34a9a" + "&q=" + keyword +  "&num=" + num.toString() + "&start=" + start.toString();
                } else {
                    url = "https://www.googleapis.com/customsearch/v1?key=AIzaSyD8vnm6IY4jTZxZ7kMQF3sjm2JeQdbv13A&cx=0357dbfa7b7d34a9a" + "&q=" + keyword + "&num=10&start=" + start.toString();
                }

                String responseJson = httpCallGoogle(url);

                try {
                    JSONObject obj = (JSONObject) new JSONParser().parse(responseJson);
                    JSONArray items = (JSONArray) obj.get("items");
                    for (int j = 0; j < items.size(); j++) {
                        JSONObject item = (JSONObject) items.get(j);
                        item.put("keyword", keyword);
                        allData.add(item);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return allData;
    }
    public static String httpCallGoogle(String url)  {
        try{
        return Jsoup.connect(url).ignoreContentType(true).execute().body();
    }
        catch (Exception e) {
            try {
                System.out.println("too many request, sleeping for 60s");
                Thread.sleep(60000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            e.printStackTrace();
            httpCallGoogle(url);
        }
        return null;
    }
    public static void writeURLToCsV(JSONArray allData){
        String csvFileLoc = "./generatedArticles/keywordsAndLinksTitles.csv";
        try (FileWriter writer = new FileWriter(csvFileLoc)) {
            // Write the header
            writer.append("Keyword,\tLink,\t Title\n");

            // Loop through each JSON object in the array
            for (int i = 0; i < allData.size(); i++) {
                JSONObject jsonObject = (JSONObject) allData.get(i);
                String keyword = (String) jsonObject.get("keyword");
                String link = (String) jsonObject.get("link");
                String title = (String) jsonObject.get("title");

                // Write the link and title to the CSV file
                writer.append(keyword).append(",\t").append(link).append(",").append(title).append("\n");
            }

            System.out.println("CSV file was created successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    public static void  csvUrlCrawler (String csvFilePath){
        // Specify the path to your CSV file
        //String csvFilePath = "path/to/your/file.csv";

        System.out.printf("Processing CSV file: %s\n", csvFilePath);
        try (Reader reader = new FileReader(csvFilePath)) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            String prevKeyword = "";
            for (CSVRecord csvRecord : csvParser) {
                StringBuilder sb = new StringBuilder();
                String curKeyword = csvRecord.get(0);
                String url = csvRecord.get(1); // Assuming URL is in the second column
                try {
                    Document doc = Jsoup.connect(url).get();
                    Elements paragraphs = doc.select(".mw-content-ltr p, .mw-content-ltr li");
                    paragraphs.forEach(element -> sb.append(element.text()).append("\n"));
                } catch (Exception e) {
                    System.err.println("Error processing URL: " + url);
                    e.printStackTrace();
                }
                String text = sb.toString();
                String title = csvRecord.get(2);
                String cleanedTitle = title.replaceAll("\\s", "");
                if (curKeyword.equals(prevKeyword)){
                    String filePath = "./generatedArticles/" + prevKeyword + "/" + cleanedTitle +".txt";
                    BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
                    writer.write(text);
                } else {

                    String folderPath = "./generatedArticles/" + curKeyword;
                    File folder = new File(folderPath);
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath + "/" + cleanedTitle + ".txt", true));
                    writer.write(text);
                }
                prevKeyword = curKeyword;
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

}


