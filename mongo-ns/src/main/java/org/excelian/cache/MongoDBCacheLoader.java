package org.excelian.cache;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.*;

/**
 * CacheLoader to bind Cassandra API onto the GuavaCache
 *
 * Created by neil.avery on 29/05/2015.
 * TODO: Replication class and factor need to be configurable.
 */
public class MongoDBCacheLoader<K,V> extends AbstractCacheLoader<Object,Object,Mongo> {


    private Mongo mongoClient;
    private List<ServerAddress> hosts;
    private boolean isSchemaCreate;
    private String keySpace;

    private boolean isTableCreated = false;

    private Class<V> clazz;

    public MongoDBCacheLoader(Class<V> clazz, List<ServerAddress> hosts, boolean isSchemaCreate, String keySpace) {
        this.hosts = hosts;
        this.isSchemaCreate = isSchemaCreate;
        this.keySpace = keySpace;
        this.keySpace = keySpace.replace("-","_").replace(" ","_").replace(":","_");
        this.clazz = clazz;
    }

    @Override
    public String getName() {
        return clazz.getSimpleName();
    }

    public void create(String name, Object k) {
        if (isSchemaCreate && mongoClient == null) {
            synchronized (this) {
                if (isSchemaCreate && mongoClient == null) {
                    isSchemaCreate = false;
                    try {
                        this.mongoClient = connect(hosts);

                        createKeySpace();
                        createTable();
                    } catch (Throwable t) {
                        t.printStackTrace();
                        System.err.println("Failed to create:" + t.getMessage());

                    }

                }
            }
        } else {
            this.mongoClient = connect(hosts);
        }
    }

    private void createKeySpace() {
        // implicit in connect?
//        session.execute(String.format("CREATE KEYSPACE IF NOT EXISTS %s  WITH REPLICATION = {'class':'%s', 'replication_factor':%d}; ", keySpace, REPLICATION_CLASS, REPLICATION_FACTOR));
//        session.execute(String.format("USE %s ", keySpace));
    }

    void createTable() {
        if (!isTableCreated) {
            isTableCreated = true;
            if (!ops().collectionExists(clazz)) {
                ops().createCollection(clazz);//, new CollectionOptions());
            }
        }
    }

    public void put(Object k, Object v) {
        ops().insert(v);
    }

    public void remove(Object k) {
        // crappy API - cant i delete by id?
        V byId = ops().findById(k, clazz);
        ops().remove(byId);
    }

    @Override
    public Object load(Object key) throws Exception {
        Object o = ops().findById(key, clazz);
        return (V) o;
    }

    @Override
    public void close() {
        if (mongoClient != null){
            mongoClient.close();
        }
    }

    @Override
    public Mongo getDriverSession() {
        if (mongoClient == null) throw  new IllegalStateException("Session has not been created - read/write to cache first");
        return mongoClient;
    }

    private MongoOperations ops() {
        return new MongoTemplate(mongoClient, keySpace);
    }

    public static Mongo connect(List<ServerAddress> hosts) {
        return new MongoClient(hosts);
    }
}
