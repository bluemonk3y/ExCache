package org.excelian.cache;

import com.datastax.driver.core.*;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.mapping.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * CacheLoader to bind Cassandra API onto the GuavaCache
 *
 * Created by neil.avery on 29/05/2015.
 * @TODO: Replication class and factor needs to be configurable.
 */
public class CassandraCacheLoader<K,V> extends AbstractCacheLoader<K,V> {


    private static final int REPLICATION_FACTOR = 1;
    private static final String REPLICATION_CLASS = "SimpleStrategy";

    private Cluster cluster;
    private Session session;
    private boolean isSchemaCreate;
    private String keySpace;

    private boolean isTableCreated = false;

    private Class<V> clazz;


    public CassandraCacheLoader(Class<V> clazz, Cluster cluster, boolean isSchemaCreate, String keySpace) {
        this.cluster = cluster;
        this.cluster.getConfiguration().getQueryOptions().setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.isSchemaCreate = isSchemaCreate;
        this.keySpace = keySpace.replace("-","_");
        this.clazz = clazz;
    }

    public CassandraCacheLoader() {

    }

    public void create(String name, K k) {
        if (isSchemaCreate && session == null) {
            try {
                session = cluster.connect();
                createKeySpace();
                createTable();
            } catch (Throwable t) {
                t.printStackTrace();
                System.err.println("Failed to create:" + t.getMessage());
                isSchemaCreate = false;
            }
        } else {
            isTableCreated = true;
            session = cluster.connect(keySpace);
        }
    }

    private void createKeySpace() {
        String query = String.format("CREATE KEYSPACE IF NOT EXISTS %s  WITH REPLICATION = {'class':'%s', 'replication_factor':%d}; ", keySpace, REPLICATION_CLASS, REPLICATION_FACTOR);
        ResultSet execute = session.execute(query);
        session.execute(String.format(" USE %s ", keySpace));
    }

    void createTable() {
        if (!isTableCreated) {
            synchronized (this) {
                final StringBuilder create_CMD = new StringBuilder("CREATE TABLE IF NOT EXISTS " + getTableName() + " ( ");
                List<String> fields = getColumns(clazz);
                String primaryKey = getPrimaryKey(clazz);
                fields.forEach(field -> create_CMD.append(field).append(",\n"));
                create_CMD.append(primaryKey);
                System.out.println("CMD:" + create_CMD);
                ResultSet execute = session.execute(create_CMD.toString());
                isTableCreated = true;
            }
        }
    }

    private String getTableName() {
        String value = clazz.getAnnotation(Table.class).value();
        if (value.length() == 0) value = clazz.getSimpleName();
        return value;
    }

    String getPrimaryKey(Class<?> aClass) {
        Field field = Arrays.asList(aClass.getDeclaredFields()).stream().filter(new Predicate<Field>() {
            @Override
            public boolean test(Field field) {
                return field.isAnnotationPresent(PrimaryKey.class);
            }
        }).findFirst().get();
        PrimaryKey annotation = field.getAnnotation(PrimaryKey.class);
        String column = annotation.value().length() > 0 ? annotation.value() : field.getName();

        return column + " " + classTypeMap.get(field.getType())+ ",\n PRIMARY KEY (" + column + ")" +
//                ")\n WITH CLUSTERING ORDER BY ( " + column + " ASC);;";
        ");";
    }

    final private Map<Class, String> classTypeMap = new HashMap<Class, String>() {{
        put(Integer.class, "int");
        put(int.class, "int");
        put(String.class, "varchar");
        put(double.class, "double");
        put(Double.class, "double");

    }};

    public List<String> getColumns(Class<?> aClass) {
        return Arrays.asList(aClass.getDeclaredFields()).stream().filter(new Predicate<Field>() {
            @Override
            public boolean test(Field field) {
                return field.isAnnotationPresent(Column.class);
            }
        })
            .map(field -> {
                String value = field.getAnnotation(Column.class).value();
                String type = classTypeMap.get(field.getType());
                if (type == null) type = "varchar";
                return (value.length() > 0 ? value : field.getName()) + " " + type;
            })
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public void put(K k, V v) {
        ops().insert(v);
    }

    public void remove(K k) {
        ops().deleteById(clazz, k);
    }

    @Override
    public V load(K key) throws Exception {
        Object o = ops().selectOneById(clazz, key);
        return (V) o;
    }

    private CassandraOperations ops() {
        return new CassandraTemplate(session);
    }

    public static Cluster connect(String contactPoint, String clusterName, int port) {
        // Connect to the cluster and keySpace "testKube"
        Cluster cluster = Cluster.builder().addContactPoint(contactPoint).withPort(port).withClusterName(clusterName).build();
        Metadata metadata = cluster.getMetadata();
        return cluster;
    }
}
