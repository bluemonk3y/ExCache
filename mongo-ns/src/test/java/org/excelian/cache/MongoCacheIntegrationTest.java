package org.excelian.cache;

import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by neil.avery on 09/06/2015.
 */
public class MongoCacheIntegrationTest {

    private List<ServerAddress> serverAddresses = Arrays.asList(new ServerAddress("10.28.1.140", 27017));
    private String keySpace = "NoSQL_Nearside_Test_" + new Date().toString();
    private CacheThing<String, TestEntity> cacheThing;

    @Before
    public void setUp() throws Exception {
        cacheThing = new CacheThing<>(
                new MongoDBCacheLoader<String,TestEntity>(TestEntity.class, serverAddresses, true, keySpace));
    }

    @After
    public void tearDown() throws Exception {
        cacheThing.close();
    }

    @Test
    public void testGetDriver() throws Exception {
        cacheThing.get("test-1");
        MongoDBCacheLoader cacheLoader = (MongoDBCacheLoader) cacheThing.getCacheLoader();
        Mongo driver = cacheLoader.getDriverSession();
        assertNotNull(driver.getAddress());
    }

    @Test
    public void testPut() throws Exception {
        cacheThing.put("test-1", new TestEntity("value-yay"));
        TestEntity test = cacheThing.get("test-1");
        assertEquals("value-yay", test.pkString);
    }

    @Test
    public void testReadCache() throws Exception {
        cacheThing.put("test-2", new TestEntity("test-2"));
        TestEntity test = cacheThing.get("test-2");
        assertEquals("test-2", test.pkString);
    }

    @Test
    public void testRemove() throws Exception {
        String key = "rem-test-2";
        cacheThing.put(key, new TestEntity(key));
        cacheThing.remove(key);
        TestEntity testEntity = cacheThing.get(key);
        assertNull("Item wasnt removed", testEntity);
    }

    @Test
    public void testReadThrough() throws Exception {
        cacheThing.put("test-2", new TestEntity("test-2"));
        cacheThing.put("test-3", new TestEntity("test-3"));
        // replace the cache
        CacheThing<String, TestEntity> cacheThing1 = new CacheThing<String, TestEntity>(
                new MongoDBCacheLoader<String, TestEntity>(TestEntity.class, serverAddresses, true, keySpace));

        TestEntity test = cacheThing1.get("test-2");
        assertEquals("test-2", test.pkString);

        cacheThing1.close();
    }

    /**
     *  @see "http://docs.spring.io/spring-data/data-mongo/docs/1.8.0.M1/reference/html/#mapping-usage"
     */
    @Document
    public static class TestEntity {
        @Id
        String pkString = "yay";

        private int firstInt = 1;

        @Field(value = "differentName")
        private double aDouble = 1.0;

        @Indexed
        private String aString = "yay";


        public TestEntity(String pkString) {
            this.pkString = pkString;
        }
    }

}
