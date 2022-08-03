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
        Set<String> processedPool = new HashSet<>();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        linkPool.add("https://sina.cn");
        while (!linkPool.isEmpty()) {

            //从链接池中取出一个链接
            String link = linkPool.remove(linkPool.size() - 1);

            //判断这个链接是否处理过，如果处理过就进入下一次循环，没有处理就将它放到处理池
            if (processedPool.contains(link)) continue;
            processedPool.add(link);

            //构造请求并发送
            HttpGet httpGet = new HttpGet(link);
            //忽略cookie
            RequestConfig defaultConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
            httpGet.setConfig(defaultConfig);
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                String result = IOUtils.toString(content, StandardCharsets.UTF_8);
                Document doc = Jsoup.parse(result);
                //获取页面中的所有a标签，将符合要求的加入链接池。要做个限制，不然没完没了了。
                if(linkPool.size()==0){
                    Elements aTags = doc.select("a");
                    for (Element aTag :
                            aTags) {
                        String href = aTag.attr("href");
                        if (href.contains("sina.cn") && !href.contains("blog") && href.startsWith("https")) {
                            linkPool.add(href);
                        }
                    }
                }
                //获取页面的article标签的h1标签的内容
                Elements articleTag = doc.select("article");
                if(!articleTag.isEmpty()){
                    Elements h1 = articleTag.select("h1");
                    System.out.println(link);
                    System.out.println(h1.text());
                    System.out.println("----");
                };
                EntityUtils.consume(entity);
            }
        }
    }
}
