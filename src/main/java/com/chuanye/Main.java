package com.chuanye;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        //Using the CrawlerWikiFromGoogle class to run the program
        CrawlerWikiFromGoogle crawler = new CrawlerWikiFromGoogle();
        // dataNum is the number of articles per keyword
        crawler.runEntry(30, Arrays.asList("dna", "rna", "protein", "cell", "gene"));
        /// this is demo

    }

}
