package com.github.ddb;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

public class JdbcCrawlerDao implements CrawlerDao {
    String jdbcUrl = "jdbc:h2:file:/Users/jiangdaoran/IdeaProjects/ddb-crawler/news";
    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        try {
            this.connection = DriverManager.getConnection(jdbcUrl, "root", "root");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeLinkFromLinksPoolDatabase(String link) throws SQLException {
        String sql = "delete from LINKS_TO_BE_PROCESSED where link=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }


    @Override
    public int getSpecifiedLinkNumberFromProcessedDatabase(String link) throws SQLException {
        String sql = "select count(*) from LINKS_ALREADY_PROCESSED where link=? limit 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }

    @Override
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

    @Override
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

    @Override
    public void insertLinkIntoLinksPoolDatabase(String link) throws SQLException {
        String sql = "insert into LINKS_TO_BE_PROCESSED (link) values (?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    @Override
    public void insertContentIntoNewsDatabase(String link, String title, String content) throws SQLException {
        String sql = "insert into news (url,title,content,created_at,modified_at) values (?,?,?,now(),now())";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.setString(2, title);
            statement.setString(3, content);
            statement.executeUpdate();
        }
    }

    @Override
    public void insertLinkIntoProcessedDatabase(String link) throws SQLException {
        String sql = "insert into LINKS_ALREADY_PROCESSED (link) values (?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }
}
