package org.lushplugins.regrowthwarps.storage;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class WarpsTable {
    public static final Table<Record> TABLE = DSL.table("regrowthwarps_warps");

    public static final Field<Integer> WARP_ID = DSL.field("warp_id", SQLDataType.INTEGER.autoIncrement());
    public static final Field<String> NAME = DSL.field("name", SQLDataType.VARCHAR.notNull());
    public static final Field<String> DISPLAY_NAME = DSL.field("display_name", SQLDataType.VARCHAR.notNull());
    public static final Field<String> DESCRIPTION = DSL.field("description", SQLDataType.VARCHAR);
    public static final Field<String> LOCATION = DSL.field("location", SQLDataType.VARCHAR.notNull());
    public static final Field<Integer> OWNER_ID = DSL.field("owner_id", SQLDataType.INTEGER.notNull());
    public static final Field<String> VISIBILITY = DSL.field("visibility", SQLDataType.VARCHAR.notNull());
    public static final Field<Long> NAME_RESERVED_UNTIL = DSL.field("name_reserved_until", SQLDataType.BIGINT);
    public static final Field<Long> LAST_VISITED_DAY = DSL.field("last_visited_day", SQLDataType.BIGINT);
}
