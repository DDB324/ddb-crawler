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
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException, SQLException {
        String jdbcUrl = "jdbc:h2:file:/Users/jiangdaoran/IdeaProjects/ddb-crawler/news";
        Connection connection = DriverManager.getConnection(jdbcUrl, "root", "root");
        List<String> linkPool = new ArrayList<>();
        Set<String> processedPool = new HashSet<>();
        linkPool.add("https://sina.cn");
//        while(判断链接池的个数){
//            获取链接池的第一个链接；
//            判断链接有没有被处理；
//            没有被处理就访问链接获取a标签，并且查看它是否有article标签；
//            从链接池中删除这个链接；
//            将这个链接加入到处理过的链接池中；
//        }

        while (!linkPool.isEmpty()) {

            //从链接池中取出一个链接
            String link = linkPool.remove(linkPool.size() - 1);
            System.out.println(link);
            System.out.println(linkPool.size());
            //判断这个链接是否处理过，如果处理过就进入下一次循环，没有处理就将它放到处理池
            if (processedPool.contains(link)) {
                continue;
            }
            processedPool.add(link);

            //发送请求获得html
            Document doc = httpGetAndParseHtml(link);

            //将页面的内容，url存入数据库
            putNewsContentInToDatabase(doc, connection, link);

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

    private static void putNewsContentInToDatabase(Document doc, Connection connection, String link) throws SQLException {
        Elements articleTag = doc.select("article");
        if (!articleTag.isEmpty()) {
            String title = articleTag.select("h1").stream().map(Element::text).collect(Collectors.joining("\n"));
            String newsContent = articleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
            //将标题，内容，链接储存到数据库中
            //id不用传，会自动生成
            String sql = "insert into news (url,title,content,created_at,modified_at) values (?,?,?,now(),now())"
            try (PreparedStatement statement = connection.prepareStatement(sql)){
                statement.setString(1,link);
                statement.setString(2,title);
                statement.setString(3,newsContent);
                statement.executeUpdate();
            }
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
