package com.github.ddb;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> linkPool = new ArrayList<>();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("https://sina.cn");
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            InputStream content = entity.getContent();
            String result = IOUtils.toString(content, StandardCharsets.UTF_8);
            Document doc = Jsoup.parse(result);
            Elements aTags = doc.select("a");
            for (Element aTag :
                    aTags) {
                String href = aTag.attr("href");
                if(href.contains("sina.cn")){
                    linkPool.add(href);
                }
            }
            EntityUtils.consume(entity);
        }

        for (String link :
                linkPool) {
            CloseableHttpClient httpclient1 = HttpClients.createDefault();
            HttpGet httpGet1 = new HttpGet(link);
            RequestConfig defaultConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
            httpGet1.setConfig(defaultConfig);
            try (CloseableHttpResponse response = httpclient1.execute(httpGet1)) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                String result = IOUtils.toString(content, StandardCharsets.UTF_8);
                Document doc = Jsoup.parse(result);
                Elements article = doc.select("article");
                Elements h1 = article.select("h1");
                if(!Objects.equals(String.valueOf(h1), "")) {
                    System.out.println(link);
                    System.out.println(h1.text());
                    System.out.println("----");
                };
                EntityUtils.consume(entity);
            }
        }
    }
}
