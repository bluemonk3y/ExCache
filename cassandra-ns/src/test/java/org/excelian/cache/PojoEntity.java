package org.excelian.cache;

import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

/**
 * Created by neil.avery on 01/06/2015.
 */
@Table
public class PojoEntity {

    @PrimaryKey
    String username = "blah";

    String address = "I live here!";
}
