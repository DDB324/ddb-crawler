package com.github.ddb;


import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    public static void main(String[] args) {
        Lock lock = new ReentrantLock();

        //private final CrawlerDao dao = new JdbcCrawlerDao();
        CrawlerDao dao = new MyBatisCrawlerDao();
        for (int i = 0; i < 8; i++) {
            new Crawler(dao, lock).start();
        }
    }
}
