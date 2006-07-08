package org.seasar.cms.beantable;

import java.sql.Types;

public enum JDBCType {

    ARRAY(Types.ARRAY, "ARRAY"),

    BIGINT(Types.BIGINT, "BIGINT"),

    BINARY(Types.BINARY, "BINARY"),

    BIT(Types.BIT, "BIT"),

    BLOB(Types.BLOB, "BLOB"),

    BOOLEAN(Types.BOOLEAN, "BOOLEAN"),

    CHAR(Types.CHAR, "CHAR"),

    CLOB(Types.CLOB, "CLOB"),

    DATALINK(Types.DATALINK, "DATALINK"),

    DATE(Types.DATE, "DATE"),

    DECIMAL(Types.DECIMAL, "DECIMAL"),

    DISTINCT(Types.DISTINCT, "DISTINCT"),

    DOUBLE(Types.DOUBLE, "DOUBLE"),

    FLOAT(Types.FLOAT, "FLOAT"),

    INTEGER(Types.INTEGER, "INTEGER"),

    JAVA_OBJECT(Types.JAVA_OBJECT, "JAVA_OBJECT"),

    LONGVARBINARY(Types.LONGVARBINARY, "LONGVARBINARY"),

    LONGVARCHAR(Types.LONGVARCHAR, "LONGVARCHAR"),

    NULL(Types.NULL, "NULL"),

    NUMERIC(Types.NUMERIC, "NUMERIC"),

    OTHER(Types.OTHER, "OTHER"),

    REAL(Types.REAL, "REAL"),

    REF(Types.REF, "REF"),

    SMALLINT(Types.SMALLINT, "SMALLINT"),

    STRUCT(Types.STRUCT, "STRUCT"),

    TIME(Types.TIME, "TIME"),

    TIMESTAMP(Types.TIMESTAMP, "TIMESTAMP"),

    TINYINT(Types.TINYINT, "TINYINT"),

    VARBINARY(Types.VARBINARY, "VARBINARY"),

    VARCHAR(Types.VARCHAR, "VARCHAR"),

    DATETIME(Types.DATE, "DATETIME");

    private int type_;

    private String name_;

    private JDBCType(int type, String name) {
        type_ = type;
        name_ = name;
    }

    public static JDBCType getInstance(int type) {
        for (int i = 0; i < values().length; i++) {
            if (type == values()[i].getType()) {
                return values()[i];
            }
        }
        return null;
    }

    public int getType() {
        return type_;
    }

    public String getName() {
        return name_;
    }
}
