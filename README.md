# ExCache
A extensible near-cache solution that combines eventing with pluggable NoSQL backends.

## Features:
- Nearside caching for common NoSQL platforms. 
- ORM Modelling for POJO's using Spring data
- Eventing and invalidation between various client-side caches (*) (Kafka, Rabbit etc)
- JSON support (*)
- eventual support for continuous query cache by leveraging cache-events and implicit query support (*)

\* coming soon ;)

## Leverages:
- Google Guava Cache for flexible caching implementation 
   https://code.google.com/p/guava-libraries/wiki/CachesExplained
- Spring Data wrappers to map POJO's to each data platform 
   http://projects.spring.io/spring-data/

## Supports
- Cassandra (done)
- Mongodb (done)
- Couchbase

## Getting started:
- Take a look at the integration tests for each data platform

```
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
  List<ServerAddress> serverAddresses = Arrays.asList(new ServerAddress("10.28.1.140", 27017));
  CacheThing cacheThing = new CacheThing<>(
                new MongoDBCacheLoader<String,TestEntity>(TestEntity.class, serverAddresses, true, keySpace));)
   cacheThing.put("test-1", new TestEntity("value-yay"));
   TestEntity test = cacheThing.get("test-1");
    assertEquals("value-yay", test.pkString);
```


## Future work
- Eventing mechanism
- JSON
