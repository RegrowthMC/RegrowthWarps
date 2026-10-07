package org.lushplugins.regrowthwarps.storage;

import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.lushplugins.lushlib.item.DisplayItemStack;
import org.lushplugins.regrowthwarps.util.jooq.StringDisplayItemBinding;

public class WarpsTable {
    public static final Table<Record> TABLE = DSL.table("regrowthwarps_warps");

    private static final DataType<DisplayItemStack> DISPLAY_ITEM_TYPE = SQLDataType.VARCHAR.asConvertedDataType(new StringDisplayItemBinding());

    public static final Field<Integer> WARP_ID = DSL.field("warp_id", SQLDataType.INTEGER.autoIncrement());
    public static final Field<String> NAME = DSL.field("name", SQLDataType.VARCHAR.notNull());
    public static final Field<String> DISPLAY_NAME = DSL.field("display_name", SQLDataType.VARCHAR.notNull());
    public static final Field<String> ICON = DSL.field("icon", SQLDataType.VARCHAR);
    public static final Field<String> DESCRIPTION = DSL.field("description", SQLDataType.VARCHAR);
    public static final Field<String> CATEGORY = DSL.field("category", SQLDataType.VARCHAR);
    public static final Field<String> LOCATION = DSL.field("location", SQLDataType.VARCHAR.notNull());
    public static final Field<Integer> OWNER_ID = DSL.field("owner_id", SQLDataType.INTEGER.notNull());
    public static final Field<String> VISIBILITY = DSL.field("visibility", SQLDataType.VARCHAR.notNull());
    public static final Field<Long> NAME_RESERVED_SINCE = DSL.field("name_reserved_since", SQLDataType.BIGINT);
    public static final Field<Integer> VISITS = DSL.field("visits", SQLDataType.INTEGER.notNull());
    public static final Field<Long> LAST_VISITED_DAY = DSL.field("last_visited_day", SQLDataType.BIGINT);
}
