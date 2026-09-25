package org.lushplugins.regrowthwarps.storage;

import org.jetbrains.annotations.Blocking;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.lushplugins.regrowthwarps.util.jooq.BinaryUUIDBinding;

import java.util.UUID;

public class UsersTable {
    public static final Table<Record> TABLE = DSL.table("regrowthwarps_users");

    private static final DataType<UUID> UUID_TYPE = SQLDataType.BINARY(16).asConvertedDataType(new BinaryUUIDBinding());

    public static final Field<Integer> USER_ID = DSL.field("user_id", SQLDataType.INTEGER.autoIncrement());
    public static final Field<byte[]> UUID = DSL.field("uuid", SQLDataType.BINARY(16).notNull());
//    public static final Field<UUID> UUID = DSL.field("uuid", SQLDataType.UUID);
    public static final Field<String> DEFAULT_WARP = DSL.field("default_warp", SQLDataType.VARCHAR);

    @Blocking
    public static int getOrCreateUserId(DSLContext context, UUID uuid) {
        byte[] uuidAsBytes = BinaryUUIDBinding.to(uuid);
        Integer userId = context
            .select(UsersTable.USER_ID)
            .from(UsersTable.TABLE)
            .where(UsersTable.UUID.eq(uuidAsBytes))
            .fetchOneInto(Integer.class);

        if (userId != null) {
            return userId;
        }

        return context
            .insertInto(UsersTable.TABLE)
            .set(UsersTable.UUID, uuidAsBytes)
            .returning(UsersTable.USER_ID)
            .fetchOne()
            .get(UsersTable.USER_ID);
    }
}
