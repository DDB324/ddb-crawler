package com.github.ddb;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class MyBatisCrawlerDao implements CrawlerDao {
    private SqlSessionFactory sqlSessionFactory;

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
    public void removeLinkFromLinksPoolDatabase(String link) throws SQLException {

    }

    @Override
    public boolean isLinkAlreadyProcessed(String link) throws SQLException {
        return false;
    }

    @Override
    public String getFirstLinkFromLinksPoolDatabase() throws SQLException {
        return null;
    }

    @Override
    public int getLinksNumberFromLinksPoolDatabase() throws SQLException {
        return 0;
    }

    @Override
    public void insertLinkIntoProcessedDatabase(String link) throws SQLException {

    }

    @Override
    public void insertContentIntoNewsDatabase(String link, String title, String newsContent) throws SQLException {

    }

    @Override
    public void insertLinkToDatabase(String sql, String link) throws SQLException {

    }

    @Override
    public void insertLinkToAlreadyDatabase(String link) throws SQLException {

    }
}
