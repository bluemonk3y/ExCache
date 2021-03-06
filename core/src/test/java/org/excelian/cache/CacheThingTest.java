package org.excelian.cache;

import com.google.common.cache.RemovalNotification;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class CacheThingTest {

    private AbstractCacheLoader<String, String, String> fixture;
    private int created;
    private int read;
    private int removed;
    private RemovalNotification receivedNotification;
    private int put;
    private CacheThing<String, String> cacheThing;

    @Test
    public void canCreate() throws ExecutionException {

        String test = cacheThing.get("TEST1");
        test = cacheThing.get("TEST2");
        assertEquals(1, created);
    }


    @Test
    public void canReadThrough() throws ExecutionException {
        String test = cacheThing.get("TEST");
        assertEquals(1, read);
        assertEquals("FIXTURE:loaded_TEST", test);
    }

    @Test
    public void canWriteThrough() throws ExecutionException {
        cacheThing.put("TEST","VALUE");
        assertEquals(1, put);

    }
    @Test
    public void canRemove() throws ExecutionException {
        cacheThing.put("TEST", "VALUE");
        cacheThing.remove("TEST");
        assertEquals(1, removed);

    }
    @Before
    public void setUp() throws Exception {

        fixture = new AbstractCacheLoader<String, String, String>() {
            public String load(String key) throws Exception {
                read++;
                return "FIXTURE:loaded_" + key;
            }

            public void close() {}
            public void create(String name, String s) {
                created++;
            }
            public void put(String s, String s2) {
                put++;
            }
            public void remove(String s) {
                removed++;
            }
            public String getName() { return "myCache"; }
            public String getDriverSession() {  return "yay"; }
            public void invalidateAll() {}
        };
        cacheThing = new CacheThing<String, String>(fixture);
    }
}
