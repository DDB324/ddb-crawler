package com.github.ddb;

import java.util.Map;

public interface MyBatisMapper {
    void removeLinkFromLinksPool(String link);

    int getLinkNumber(Map<String, Object> param);

    String getFirstLinkFromLinksPool();

    void insertLink(Map<String, Object> param);

    void insertContentIntoNews(News news);
}
