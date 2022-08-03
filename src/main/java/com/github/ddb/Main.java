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
        linkPool.add("https://sina.cn");
        while (!linkPool.isEmpty()) {

            //从链接池中取出一个链接
            String link = linkPool.remove(linkPool.size() - 1);
            System.out.println(link);
            System.out.println(linkPool.size());
            //判断这个链接是否处理过，如果处理过就进入下一次循环，没有处理就将它放到处理池
            if (processedPool.contains(link)) continue;
            processedPool.add(link);

            //发送请求获得html
            Document doc = httpGetAndParseHtml(link);

            //获取页面的article标签的h1标签的内容
            getArticleText(link, doc);

            //获取页面中的所有a标签，将符合要求的加入链接池。要做个限制，不然没完没了了。
            if (linkPool.size() == 0) {
                addSatisfyConditionLinks(linkPool, doc);
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        //构造请求并发送
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        //忽略cookie
        RequestConfig defaultConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
        httpGet.setConfig(defaultConfig);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            InputStream content = entity.getContent();
            String result = IOUtils.toString(content, StandardCharsets.UTF_8);
            return Jsoup.parse(result);
        }
    }

    private static void getArticleText(String link, Document doc) {
        Elements articleTag = doc.select("article") ;
        if (!articleTag.isEmpty()) {
            Elements h1 = articleTag.select("h1");
//            System.out.println(link);
            System.out.println(h1.text());
            System.out.println("----");
        }
    }

    private static void addSatisfyConditionLinks(List<String> linkPool, Document doc) {
        Elements aTags = doc.select("a");
        for (Element aTag :
                aTags) {
            String href = aTag.attr("href");
            if (satisfyConditionLink(href)) {
                linkPool.add(href);
            }
        }
    }

    private static boolean satisfyConditionLink(String href) {
        return isSinaPage(href) && isNotBlogPage(href) && startWithHttps(href);
    }

    private static boolean startWithHttps(String href) {
        return href.startsWith("https");
    }

    private static boolean isNotBlogPage(String href) {
        return !href.contains("blog");
    }

    private static boolean isSinaPage(String href) {
        return href.contains("sina.cn");
    }

}
