package org.excelian.cache;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * Created by neil.avery on 27/05/2015.
 */
abstract public class AbstractCacheLoader<K,V,D> extends CacheLoader<K,V> implements CacheListener<K,V>, RemovalListener<K,V> {

    public void create(String name, K k) {
    }

    public void put(K k, V v) {
    }

    public void remove(K k) {
    }

    @Override
    public V load(K key) throws Exception {
        return null;
    }

    public void onRemoval(RemovalNotification<K, V> notification) {
        if (notification.getCause().equals(RemovalCause.EXPLICIT)) {
            this.remove(notification.getKey());
        }
    }
    abstract public void close();

    abstract public String getName();

    abstract public D getDriverSession();
}
