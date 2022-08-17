package com.github.ddb;

import java.util.List;

public interface MockDataMapper {
    List<News> getNewsFromNews();

    void insertNewsIntoNews(News news);

    int getNewsNumber();
}
