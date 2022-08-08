package com.github.ddb;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

public class DatabaseAccessObject {
    String jdbcUrl = "jdbc:h2:file:/Users/jiangdaoran/IdeaProjects/ddb-crawler/news";
    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public DatabaseAccessObject() {
        try {
            this.connection = DriverManager.getConnection(jdbcUrl, "root", "root");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeLinkFromLinksPoolDatabase(String link) throws SQLException {
        insertLinkToDatabase("delete from LINKS_TO_BE_PROCESSED where link=?", link);
    }

    public boolean isLinkAlreadyProcessed(String link) throws SQLException {
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

    public String getFirstLinkFromLinksPoolDatabase() throws SQLException {
        String sql = "select link from LINKS_TO_BE_PROCESSED limit 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    public int getLinksNumberFromLinksPoolDatabase() throws SQLException {
        String sql = "select count(*) from LINKS_TO_BE_PROCESSED";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }

    public void insertLinkIntoProcessedDatabase(String link) throws SQLException {
        insertLinkToDatabase("insert into LINKS_ALREADY_PROCESSED (link) values (?)", link);
    }

    public void insertContentIntoNewsDatabase(String link, String title, String newsContent) throws SQLException {
        String sql = "insert into news (url,title,content,created_at,modified_at) values (?,?,?,now(),now())";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.setString(2, title);
            statement.setString(3, newsContent);
            statement.executeUpdate();
        }
    }

    public void insertLinkToDatabase(String sql, String href) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, href);
            statement.executeUpdate();
        }
    }

    public void insertLinkToAlreadyDatabase(String link) throws SQLException {
        insertLinkToDatabase("insert into LINKS_TO_BE_PROCESSED (link) values (?)", link);
    }
}
