package org.excelian.cache;

/**
 * Created by neil.avery on 27/05/2015.
 * Glues itself onto Guava by way of the AbstractCacheLoafer
 * - create - optionally create back end store and hook in with associated credentials
 * - put - listen to put events to drive distributed invalidation
 *
 * TODO: Remove invalidate, publish invalidate
 */
public interface CacheListener<K,V> {

    // Creds?
    public void create(String name, K k);

    V load(K key) throws Exception;

    public void put(K k, V v);

    public void remove(K k);
}
