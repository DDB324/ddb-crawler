package com.github.ddb;

import java.sql.SQLException;

public interface CrawlerDao {
    void removeLinkFromLinksPoolDatabase(String link) throws SQLException;

    int getSpecifiedLinkNumberFromProcessedDatabase(String link) throws SQLException;

    String getFirstLinkFromLinksPoolDatabase() throws SQLException;

    int getLinksNumberFromLinksPoolDatabase() throws SQLException;

    void insertLinkIntoLinksPoolDatabase(String link) throws SQLException;

    void insertContentIntoNewsDatabase(String link, String title, String content) throws SQLException;

    void insertLinkIntoProcessedDatabase(String link) throws SQLException;
}
