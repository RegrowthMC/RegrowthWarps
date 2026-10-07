package org.lushplugins.regrowthwarps.util.jooq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jspecify.annotations.NonNull;
import org.lushplugins.lushlib.item.DisplayItemStack;
import org.lushplugins.lushlib.jackson.JacksonHelper;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;

public class StringDisplayItemBinding implements Binding<String, DisplayItemStack> {
    private static final ObjectMapper MAPPER = JacksonHelper.addCustomSerializers(new ObjectMapper());

    @Override
    public @NotNull Converter<String, DisplayItemStack> converter() {
        return new Converter<>() {
            @Override
            public DisplayItemStack from(String string) {
                if (string == null) return null;

                try {
                    return MAPPER.readValue(string, DisplayItemStack.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String to(DisplayItemStack icon) {
                if (icon == null) return null;

                try {
                    return MAPPER.writeValueAsString(icon);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public @NonNull Class<String> fromType() {
                return String.class;
            }

            @Override
            public @NonNull Class<DisplayItemStack> toType() {
                return DisplayItemStack.class;
            }
        };
    }

    @Override
    public void sql(BindingSQLContext<DisplayItemStack> context) {
        context.render().visit(DSL.val(context.convert(converter()).value())).sql("");
    }

    @Override
    public void register(BindingRegisterContext<DisplayItemStack> context) throws SQLException {
        context.statement().registerOutParameter(context.index(), Types.VARCHAR);
    }

    @Override
    public void set(BindingSetStatementContext<DisplayItemStack> context) throws SQLException {
        context.statement().setString(context.index(), converter().to(context.value()));
    }

    @Override
    public void get(BindingGetResultSetContext<DisplayItemStack> context) throws SQLException {
        context.value(converter().from(context.resultSet().getString(context.index())));
    }

    @Override
    public void get(BindingGetStatementContext<DisplayItemStack> context) throws SQLException {
        context.value(converter().from(context.statement().getString(context.index())));
    }

    @Override
    public void set(BindingSetSQLOutputContext<DisplayItemStack> bindingSetSQLOutputContext) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void get(BindingGetSQLInputContext<DisplayItemStack> bindingGetSQLInputContext) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    // TODO: Remove when bindings work
    public static DisplayItemStack from(String string) {
        if (string == null) return null;

        try {
            return MAPPER.readValue(string, DisplayItemStack.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String to(DisplayItemStack icon) {
        if (icon == null) return null;

        try {
            return MAPPER.writeValueAsString(icon);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
