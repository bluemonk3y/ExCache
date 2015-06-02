package org.excelian.cache;

import org.junit.Test;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.util.List;

import static org.junit.Assert.*;

public class CassandraCacheLoaderTest {

    @Test
    public void testColumnExtraction() throws Exception {
        List<String> columns = new CassandraCacheLoader<>().getColumns(TestEntity.class);
        System.out.println("Columns:" + columns);
        assertEquals(3, columns.size());
        assertTrue(columns.toString().contains("firstInt"));
        assertTrue(columns.toString().contains("mappedColumn"));
    }

    @Test
    public void testPKExtraction() throws Exception {
        String pk = new CassandraCacheLoader<>().getPrimaryKey(TestEntity.class);
        System.out.println("Columns:" + pk);
        assertTrue(pk.contains("pkString"));
    }


    @Test
    public void testPut() throws Exception {

    }

    @Test
    public void testRemove() throws Exception {

    }

    @Test
    public void testLoad() throws Exception {

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
        private String pkString = "yay";
    }
}