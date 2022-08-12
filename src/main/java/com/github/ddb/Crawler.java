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
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

public class Crawler extends Thread {
    private static final int MAX_NO_LINK_NUMBER = 20;
    private static final int SLEEP_TIME = 50;
    Lock lock;
    private final CrawlerDao dao;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public Crawler(CrawlerDao dao, Lock lock) {
        this.dao = dao;
        this.lock = lock;
    }

    @Override
    public void run() {
        try {
            int noLinkNumber = 0;
            String link;
            //获取链接池的第一个链接，不为null就进入循环
            while (true) {
                link = getFirstLinkThenDelete();

                //如果没有获取到的链接次数大于MAX，就跳出循环，否则继续循环
                if (link == null && noLinkNumber > MAX_NO_LINK_NUMBER) {
                    break;
                }
                if (link == null) {
                    noLinkNumber += 1;
                    sleep(SLEEP_TIME);
                    continue;
                }
                System.out.println(link);

                //判断链接有没有被处理，如果处理过了就进入下一次循环
                if (isLinkAlreadyProcessed(link)) {
                    continue;
                }

                //将这个链接加到处理过的链接池中
                dao.insertLinkIntoProcessedDatabase(link);

                //对链接进行处理
                Document doc = httpGetAndParseHtml(link);
                //限制下加入的链接，不然没完没了
                if (dao.getLinksNumberFromLinksPoolDatabase() == 0) {
                    addSatisfyConditionLinksIntoDatabase(doc);
                }
                //访问链接获取a标签和article内容，将内容存到数据库
                putNewsContentInToDatabase(doc, link);
            }
            printMsg();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getFirstLinkThenDelete() throws SQLException {
        lock.lock();
        try {
            String link = dao.getFirstLinkFromLinksPoolDatabase();
            if (link != null) {
                dao.removeLinkFromLinksPoolDatabase(link);
            }
            return link;
        } finally {
            lock.unlock();
        }
    }

    public void printMsg() {
        Thread t = Thread.currentThread();
        String name = t.getName();
        System.out.println("--------------------------------------------------------------------name=" + name + "结束");
    }

    public boolean isLinkAlreadyProcessed(String link) throws SQLException {
        return dao.getSpecifiedLinkNumberFromProcessedDatabase(link) != 0;
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        //构造请求并发送
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        //忽略cookie和开启循环重定向
        RequestConfig defaultConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).setCircularRedirectsAllowed(true).build();
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
        for (Element aTag : aTags) {
            String link = aTag.attr("href");
            if (satisfyConditionLink(link)) {
                dao.insertLinkIntoLinksPoolDatabase(link);
            }
        }
    }


    private static boolean satisfyConditionLink(String href) {
        return isSinaPage(href) && isNotBlogPage(href) && startWithHttps(href) && isNotReload(href);
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

    private static boolean isNotReload(String href) {
        return !href.contains("reload");
    }
}
