package com.github.ddb;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MyBatisCrawlerDao implements CrawlerDao {
    private final SqlSessionFactory sqlSessionFactory;

    public MyBatisCrawlerDao() {
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeLinkFromLinksPoolDatabase(String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            MyBatisMapper mapper = session.getMapper(MyBatisMapper.class);
            mapper.removeLinkFromLinksPool(link);
        }
    }

    @Override
    public int getSpecifiedLinkNumberFromProcessedDatabase(String link) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBatisMapper mapper = session.getMapper(MyBatisMapper.class);
            Map<String, Object> param = new HashMap<>();
            param.put("tableName", "LINKS_ALREADY_PROCESSED");
            param.put("link", link);
            return mapper.getLinkNumber(param);
        }
    }

    @Override
    public String getFirstLinkFromLinksPoolDatabase() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBatisMapper mapper = session.getMapper(MyBatisMapper.class);
            return mapper.getFirstLinkFromLinksPool();
        }
    }

    @Override
    public int getLinksNumberFromLinksPoolDatabase() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBatisMapper mapper = session.getMapper(MyBatisMapper.class);
            Map<String, Object> param = new HashMap<>();
            param.put("tableName", "LINKS_TO_BE_PROCESSED");
            return mapper.getLinkNumber(param);
        }
    }

    @Override
    public void insertLinkIntoLinksPoolDatabase(String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            MyBatisMapper mapper = session.getMapper(MyBatisMapper.class);
            Map<String, Object> param = new HashMap<>();
            param.put("tableName", "LINKS_TO_BE_PROCESSED");
            param.put("link", link);
            mapper.insertLink(param);
        }
    }

    @Override
    public void insertContentIntoNewsDatabase(String link, String title, String content) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            MyBatisMapper mapper = session.getMapper(MyBatisMapper.class);
            mapper.insertContentIntoNews(new News(link, title, content));
        }
    }

    @Override
    public void insertLinkIntoProcessedDatabase(String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            MyBatisMapper mapper = session.getMapper(MyBatisMapper.class);
            Map<String, Object> param = new HashMap<>();
            param.put("tableName", "LINKS_ALREADY_PROCESSED");
            param.put("link", link);
            mapper.insertLink(param);
        }
    }
}
