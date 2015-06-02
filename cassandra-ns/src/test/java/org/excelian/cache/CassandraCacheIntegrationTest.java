package org.excelian.cache;

import com.datastax.driver.core.Cluster;
import org.junit.Test;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CassandraCacheIntegrationTest {

    private Cluster cluster = CassandraCacheLoader.connect("10.28.1.140", "BluePrint", 9042);
    private String keySpace = "NoSQL-Nearside-Test";

    @Test
    public void testPut() throws Exception {

        CacheThing<String, TestEntity> cacheThing = new CacheThing<String, TestEntity>("test-cache",
                new CassandraCacheLoader<String,TestEntity>(TestEntity.class, cluster, true, keySpace));
        cacheThing.put("test-1", new TestEntity("value-yay"));
        TestEntity test = cacheThing.get("test-1");
        assertEquals("value-yay", test.pkString);
    }

    @Test
    public void testReadCache() throws Exception {

        CacheThing<String, TestEntity> cacheThing = new CacheThing<String, TestEntity>("test-cache", new CassandraCacheLoader<>(TestEntity.class, cluster, true, keySpace));
        cacheThing.put("test-2", new TestEntity("test-2"));
        TestEntity test = cacheThing.get("test-2");
        assertEquals("test-2", test.pkString);
    }

    @Test
    public void testRemove() throws Exception {
    }

    @Test
    public void testReadThrough() throws Exception {
        CacheThing<String, TestEntity> cacheThing = new CacheThing<String, TestEntity>("test-cache", new CassandraCacheLoader<>(TestEntity.class, cluster, true, keySpace));
        cacheThing.put("test-2", new TestEntity("test-2"));
        cacheThing.put("test-3", new TestEntity("test-3"));
        // replace the cache
        cacheThing = new CacheThing<String, TestEntity>("test-cache", new CassandraCacheLoader<>(TestEntity.class, cluster, true, keySpace));

        TestEntity test = cacheThing.get("test-2");
        assertEquals("test-2", test.pkString);
    }

    @Table
    public static class TestEntity {
        @Column
        private int firstInt = 1;
        @Column
        private double aDouble = 1.0;
        @Column(value="mappedColumn")
        private String aString = "yay";
        @PrimaryKey
        String pkString = "yay";

        public TestEntity(String pkString) {
            this.pkString = pkString;
        }
    }
}