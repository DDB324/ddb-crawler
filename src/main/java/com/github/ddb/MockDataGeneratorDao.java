package com.github.ddb;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockDataGeneratorDao {
    private static final int TARGET_ROW_COUNT = 100_0000;

    @SuppressFBWarnings("DMI_RANDOM_USED_ONLY_ONCE")
    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            MockDataMapper mapper = session.getMapper(MockDataMapper.class);
            List<News> trueNews = mapper.getNewsFromNews();
            int number = mapper.getNewsNumber();
            Random random = new Random();
            int count = TARGET_ROW_COUNT - number;
            while (count-- > 0) {
                int index = random.nextInt(100);
                News news = trueNews.get(index);
                Instant trueTime = news.getCreatedAt();
                Instant fakeTime = trueTime.plusSeconds(random.nextInt(3600 * 24 * 36));
                News newNews = new News(news.getUrl(), news.getTitle(), news.getContent());
                newNews.setCreatedAt(fakeTime);
                newNews.setModifiedAt(fakeTime);
                mapper.insertNewsIntoNews(newNews);
                System.out.println("Left: " + count);
            }
        }
    }
}
