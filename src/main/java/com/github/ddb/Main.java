package com.github.ddb;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> linkPool = new ArrayList<>();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("https://sina.cn");
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            System.out.println(response.getStatusLine());
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
                    System.out.println(href);
                    System.out.println("-------");
                }
            }

            EntityUtils.consume(entity);
        }
    }
}
