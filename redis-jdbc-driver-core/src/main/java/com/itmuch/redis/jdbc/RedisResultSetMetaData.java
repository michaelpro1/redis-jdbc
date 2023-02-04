package com.itmuch.redis.jdbc;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.function.Function;

public class RedisResultSetMetaData implements ResultSetMetaData {
    private final static Logger LOGGER = new Logger(RedisResultSetMetaData.class);

    public static final int MAX_SIZE = 1024;

    private final Map<Integer, ColumnConverter> columnConverterMap;


    public RedisResultSetMetaData(Map<Integer, ColumnConverter> columnConverterMap) {
        this.columnConverterMap = columnConverterMap;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        try {
            return iface.cast(this);
        } catch (ClassCastException cce) {
            LOGGER.log("Unable to unwrap to %s", iface);
            throw new SQLException("Unable to unwrap to " + iface);
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }

    @Override
    public int getColumnCount() throws SQLException {
        return columnConverterMap.size();
    }

    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        return true;
    }

    @Override
    public boolean isSearchable(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isCurrency(int column) throws SQLException {
        return false;
    }

    @Override
    public int isNullable(int column) throws SQLException {
        return ResultSetMetaData.columnNoNulls;
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        return false;
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        return MAX_SIZE;
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        return getColumnName(column);
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        return getFromColumnConverter(column, "RESULTS", ColumnConverter::getColumnName);
    }

    @Override
    public String getSchemaName(int column) throws SQLException {
        LOGGER.log("getSchemaName(%s)", column);
        return "9";
    }

    @Override
    public int getPrecision(int column) throws SQLException {
        return MAX_SIZE;
    }

    @Override
    public int getScale(int column) throws SQLException {
        return 0;
    }

    @Override
    public String getTableName(int column) throws SQLException {
        return "";
    }

    @Override
    public String getCatalogName(int column) throws SQLException {
        return "";
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        return getFromColumnConverter(column, Types.NVARCHAR, c -> c.getColumnTypeName());
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        return getFromColumnConverter(column, "String", c -> c.getTargetType().getSimpleName());
    }

    @Override
    public boolean isReadOnly(int column) throws SQLException {
        return true;
    }

    @Override
    public boolean isWritable(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        return false;
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        return getFromColumnConverter(column, "java.lang.String", c -> c.getTargetType().getName());
    }

    private <T> T getFromColumnConverter(int column, T nullDefault, Function<ColumnConverter, T> getter) {
        if (column > columnConverterMap.size() + 1) {
            throw new IllegalArgumentException("Invalid column index " + column);
        }
        ColumnConverter cc = columnConverterMap.get(column);
        return cc == null ? nullDefault : getter.apply(cc);
    }

}
