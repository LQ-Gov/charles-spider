package com.bh.spider.client;

import com.bh.spider.fetch.Extractor;
import com.bh.spider.fetch.FetchContext;

public class CrawlerExtractor implements Extractor {
    @Override
    public void run(FetchContext ctx) throws Exception {
        System.out.println("测试抽取");
    }
}
