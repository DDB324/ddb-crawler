package com.github.ddb;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

public class Main {
    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        String jdbcUrl = "jdbc:h2:file:/Users/jiangdaoran/IdeaProjects/ddb-crawler/news";
        Connection connection = DriverManager.getConnection(jdbcUrl, "root", "root");
        String link;
        //获取链接池的第一个链接，不为null就进入循环
        while ((link = getFirstLinkFromLinksPoolDatabase(connection)) != null) {
            System.out.println(link);

            //从链接池中删除这个链接
            removeLinkFromLinksPoolDatabase(link, connection);

            //判断链接有没有被处理，如果处理过了就进入下一次循环
            if (isLinkAlreadyProcessed(link, connection)) {
                continue;
            }
            //将这个链接加到处理过的链接池中
            addLinkIntoProcessedDatabase(link, connection);

            //对链接进行处理
            Document doc = httpGetAndParseHtml(link);
            //限制下加入的链接，不然没完没了
            if (getLinksNumberFromLinksPoolDatabase(connection) == 0) {
                addSatisfyConditionLinksIntoDatabase(doc, connection);
            }
            //访问链接获取a标签和article内容，将内容存到数据库
            putNewsContentInToDatabase(doc, connection, link);
        }
    }

    private static void removeLinkFromLinksPoolDatabase(String link, Connection connection) throws SQLException {
        insertLinkToDatabase("delete from LINKS_TO_BE_PROCESSED where link=?", connection, link);
    }

    private static boolean isLinkAlreadyProcessed(String link, Connection connection) throws SQLException {
        String sql = "select count(*) from LINKS_ALREADY_PROCESSED where link=? limit 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) != 0;
            }
        }
        return false;
    }

    private static String getFirstLinkFromLinksPoolDatabase(Connection connection) throws SQLException {
        String sql = "select link from LINKS_TO_BE_PROCESSED limit 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    private static int getLinksNumberFromLinksPoolDatabase(Connection connection) throws SQLException {
        String sql = "select count(*) from LINKS_TO_BE_PROCESSED";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }

    private static void addLinkIntoProcessedDatabase(String link, Connection connection) throws SQLException {
        insertLinkToDatabase("insert into LINKS_ALREADY_PROCESSED (link) values (?)", connection, link);
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
            String sql = "insert into news (url,title,content,created_at,modified_at) values (?,?,?,now(),now())";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, link);
                statement.setString(2, title);
                statement.setString(3, newsContent);
                statement.executeUpdate();
            }
        }
    }

    private static void addSatisfyConditionLinksIntoDatabase(Document doc, Connection connection) throws SQLException {
        Elements aTags = doc.select("a");
        for (Element aTag :
                aTags) {
            String href = aTag.attr("href");
            if (satisfyConditionLink(href)) {
                insertLinkToDatabase("insert into LINKS_TO_BE_PROCESSED (link) values (?)", connection, href);
            }
        }
    }

    private static void insertLinkToDatabase(String sql, Connection connection, String href) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, href);
            statement.executeUpdate();
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
