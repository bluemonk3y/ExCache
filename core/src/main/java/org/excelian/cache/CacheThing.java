package org.excelian.cache;

import com.google.common.cache.*;

import java.util.concurrent.ExecutionException;


public class CacheThing<K,V> implements ExCache<K,V>  {

    final private String name;

    private ForwardingCache<K, V> fwdCache;
    private AbstractCacheLoader cacheHook;

    private String spec = "maximumSize=10000,weakKeys,softValues,expireAfterWrite=1d,expireAfterAccess=1d,recordStats";

    private LoadingCache<K, V> cache;
    private volatile boolean created;

    public CacheThing(String name, final AbstractCacheLoader cacheHook, String ... optionalSpec) {


        this.name = name;
        this.cacheHook = cacheHook;
        if (optionalSpec != null && optionalSpec.length > 0) this.spec = optionalSpec[0];

        bindToGuavaCache(cacheHook);
    }

    private void bindToGuavaCache(AbstractCacheLoader cacheHook) {
        cache = CacheBuilder.from(spec).
                recordStats().removalListener(cacheHook).
                build(cacheHook);

        fwdCache = new ForwardingCache<K, V>() {
            @Override
            protected Cache<K, V> delegate() {
                return cache;
            }

            @Override
            public void put(K key, V value) {
                delegate().put(key, value);
            }

            @Override
            public void invalidate(Object key) {
                delegate().invalidate(key);
            }
        };
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public V get(final K k) throws ExecutionException {
        createMaybe(k);
        // a bit crappy - but the fwdrCache doesnt expose 'getOrLoad(K, Loader)'
        return cache.get(k);
    }

    @Override
    public void put(K k, V v) {
        createMaybe(k);
        fwdCache.put(k, v);
        cacheHook.put(k, v);
    }
    @Override
    public void remove(K k) {
        createMaybe(k);
        fwdCache.invalidate(k);
    }
    @Override
    public void invalidate() {
        fwdCache.invalidateAll();

    }
    private void createMaybe(K k) {
        if (!created) {
            synchronized (this) {
                if (!created) {
                    cacheHook.create(name, k);
                    created = true;
                }
            }
        }
    }

}
