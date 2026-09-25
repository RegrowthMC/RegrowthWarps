package org.lushplugins.regrowthwarps.util.jooq;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.UUID;

public class BinaryUUIDBinding implements Binding<byte[], UUID> {

    @Override
    public @NonNull Converter<byte[], UUID> converter() {
        return new Converter<>() {
            @Override
            public UUID from(byte[] bytes) {
                if (bytes == null) return null;
                ByteBuffer buf = ByteBuffer.wrap(bytes);
                return new UUID(buf.getLong(), buf.getLong());
            }

            @Override
            public byte[] to(UUID uuid) {
                if (uuid == null) return null;
                ByteBuffer buf = ByteBuffer.allocate(16);
                buf.putLong(uuid.getMostSignificantBits());
                buf.putLong(uuid.getLeastSignificantBits());
                return buf.array();
            }

            @Override
            public @NonNull Class<byte[]> fromType() {
                return byte[].class;
            }

            @Override
            public @NonNull Class<UUID> toType() {
                return UUID.class;
            }
        };
    }

    @Override
    public void sql(BindingSQLContext<UUID> context) {
        context.render().visit(DSL.val(context.convert(converter()).value())).sql("");
    }

    @Override
    public void register(BindingRegisterContext<UUID> context) throws SQLException {
        context.statement().registerOutParameter(context.index(), Types.BINARY);
    }

    @Override
    public void set(BindingSetStatementContext<UUID> context) throws SQLException {
        context.statement().setBytes(context.index(), converter().to(context.value()));
    }

    @Override
    public void get(BindingGetResultSetContext<UUID> context) throws SQLException {
        context.value(converter().from(context.resultSet().getBytes(context.index())));
    }

    @Override
    public void get(BindingGetStatementContext<UUID> context) throws SQLException {
        context.value(converter().from(context.statement().getBytes(context.index())));
    }

    @Override
    public void set(BindingSetSQLOutputContext<UUID> bindingSetSQLOutputContext) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void get(BindingGetSQLInputContext<UUID> bindingGetSQLInputContext) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    // TODO: Remove when bindings work
    public static UUID from(byte[] bytes) {
        if (bytes == null) return null;
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        return new UUID(buf.getLong(), buf.getLong());
    }

    public static byte[] to(UUID uuid) {
        if (uuid == null) return null;
        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.putLong(uuid.getMostSignificantBits());
        buf.putLong(uuid.getLeastSignificantBits());
        return buf.array();
    }
}
