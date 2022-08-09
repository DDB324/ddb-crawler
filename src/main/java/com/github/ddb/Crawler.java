package com.github.ddb;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.stream.Collectors;

public class Crawler {
    private final CrawlerDao dao = new JdbcCrawlerDao();

    private void run() throws SQLException, IOException {
        String link;
        //获取链接池的第一个链接，不为null就进入循环
        while ((link = dao.getFirstLinkFromLinksPoolDatabase()) != null) {
            System.out.println(link);

            //从链接池中删除这个链接
            dao.removeLinkFromLinksPoolDatabase(link);

            //判断链接有没有被处理，如果处理过了就进入下一次循环
            if (isLinkAlreadyProcessed(link)) {
                continue;
            }
            //将这个链接加到处理过的链接池中
            dao.insertLinkIntoLinksPoolDatabase(link);

            //对链接进行处理
            Document doc = httpGetAndParseHtml(link);
            //限制下加入的链接，不然没完没了
            if (dao.getLinksNumberFromLinksPoolDatabase() == 0) {
                addSatisfyConditionLinksIntoDatabase(doc);
            }
            //访问链接获取a标签和article内容，将内容存到数据库
            putNewsContentInToDatabase(doc, link);
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        new Crawler().run();
    }

    public boolean isLinkAlreadyProcessed(String link) throws SQLException {
        return dao.getSpecifiedLinkNumberFromProcessedDatabase(link) != 0;
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

    private void putNewsContentInToDatabase(Document doc, String link) throws SQLException {
        Elements articleTag = doc.select("article");
        if (!articleTag.isEmpty()) {
            String title = articleTag.select("h1").stream().map(Element::text).collect(Collectors.joining("\n"));
            String newsContent = articleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
            //将标题，内容，链接储存到数据库中
            //id不用传，会自动生成
            dao.insertContentIntoNewsDatabase(link, title, newsContent);
        }
    }


    private void addSatisfyConditionLinksIntoDatabase(Document doc) throws SQLException {
        Elements aTags = doc.select("a");
        for (Element aTag :
                aTags) {
            String link = aTag.attr("href");
            if (satisfyConditionLink(link)) {
                dao.insertLinkToProcessedDatabase(link);
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
