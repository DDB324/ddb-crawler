package com.github.ddb;

import java.sql.SQLException;

public interface CrawlerDao {
    void removeLinkFromLinksPoolDatabase(String link) throws SQLException;

    boolean isLinkAlreadyProcessed(String link) throws SQLException;

    String getFirstLinkFromLinksPoolDatabase() throws SQLException;

    int getLinksNumberFromLinksPoolDatabase() throws SQLException;

    void insertLinkIntoProcessedDatabase(String link) throws SQLException;

    void insertContentIntoNewsDatabase(String link, String title, String newsContent) throws SQLException;

    void insertLinkToDatabase(String sql, String link) throws SQLException;

    void insertLinkToAlreadyDatabase(String link) throws SQLException;
}
