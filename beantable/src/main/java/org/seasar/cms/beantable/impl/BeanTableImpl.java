package org.seasar.cms.beantable.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.seasar.cms.beantable.BeanTable;
import org.seasar.cms.beantable.JDBCType;
import org.seasar.cms.beantable.annotation.ColumnDetail;
import org.seasar.cms.beantable.annotation.Constraint;
import org.seasar.cms.beantable.annotation.Index;
import org.seasar.cms.beantable.annotation.PrimaryKey;
import org.seasar.cms.beantable.annotation.Unique;
import org.seasar.cms.beantable.identity.Identity;
import org.seasar.cms.beantable.identity.impl.HsqlIdentity;
import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;
import org.seasar.framework.log.Logger;

/**
 * <p>
 * <b>同期化：</b> このクラスはスレッドセーフではありません。
 * </p>
 * 
 * @author YOKOTA Takehiko
 */
public class BeanTableImpl implements BeanTable {

    private Class<?> beanClass_;

    private BeanInfo beanInfo_;

    private Bean s2daoBean_;

    private DataSource ds_;

    private Identity identity_;

    private Set<String> actualJdbcTypeNameSet_ = new HashSet<String>();

    private Map<JDBCType, String> actualJdbcTypeNameByTypeMap_ = new HashMap<JDBCType, String>();

    private String tableName_;

    private ColumnMetaData[] columns_;

    private Map<String, ColumnMetaData> columnMap_ = new HashMap<String, ColumnMetaData>();

    private TableConstraint[] tableConstraints_;

    private Set<String> noPersistentPropertySet_ = new HashSet<String>();

    private boolean activated_;

    private Logger logger_ = Logger.getLogger(getClass());

    private static final Map<String, JDBCType[]> JDBCTYPES_MAP = new HashMap<String, JDBCType[]>();

    public BeanTableImpl() {
    }

    public BeanTableImpl(Class<?> beanClass) {
        this();
        setBeanClass(beanClass);
    }

    static {
        prepareJdbcTypesMap();
    }

    static void prepareJdbcTypesMap() {
        JDBCTYPES_MAP.put(String.class.getName(), new JDBCType[] {
            JDBCType.LONGVARCHAR, JDBCType.VARCHAR });
        JDBCTYPES_MAP.put(BigDecimal.class.getName(),
            new JDBCType[] { JDBCType.NUMERIC });
        JDBCTYPES_MAP.put(Boolean.TYPE.getName(),
            new JDBCType[] { JDBCType.BIT });
        JDBCTYPES_MAP.put(Boolean.class.getName(),
            new JDBCType[] { JDBCType.BIT });
        JDBCTYPES_MAP.put(Byte.TYPE.getName(), new JDBCType[] {
            JDBCType.TINYINT, JDBCType.SMALLINT, JDBCType.INTEGER });
        JDBCTYPES_MAP.put(Byte.class.getName(), new JDBCType[] {
            JDBCType.TINYINT, JDBCType.SMALLINT, JDBCType.INTEGER });
        JDBCTYPES_MAP.put(Short.TYPE.getName(), new JDBCType[] {
            JDBCType.SMALLINT, JDBCType.INTEGER });
        JDBCTYPES_MAP.put(Short.class.getName(), new JDBCType[] {
            JDBCType.SMALLINT, JDBCType.INTEGER });
        JDBCTYPES_MAP.put(Integer.TYPE.getName(),
            new JDBCType[] { JDBCType.INTEGER });
        JDBCTYPES_MAP.put(Integer.class.getName(),
            new JDBCType[] { JDBCType.INTEGER });
        JDBCTYPES_MAP.put(Long.TYPE.getName(), new JDBCType[] {
            JDBCType.BIGINT, JDBCType.INTEGER });
        JDBCTYPES_MAP.put(Long.class.getName(), new JDBCType[] {
            JDBCType.BIGINT, JDBCType.INTEGER });
        JDBCTYPES_MAP.put(Float.TYPE.getName(),
            new JDBCType[] { JDBCType.REAL });
        JDBCTYPES_MAP.put(Float.class.getName(),
            new JDBCType[] { JDBCType.REAL });
        JDBCTYPES_MAP.put(Double.TYPE.getName(),
            new JDBCType[] { JDBCType.DOUBLE });
        JDBCTYPES_MAP.put(Double.class.getName(),
            new JDBCType[] { JDBCType.DOUBLE });
        JDBCTYPES_MAP.put(byte[].class.getName(),
            new JDBCType[] { JDBCType.LONGVARBINARY });
        JDBCTYPES_MAP.put(Date.class.getName(),
            new JDBCType[] { JDBCType.DATE });
        JDBCTYPES_MAP.put(Time.class.getName(),
            new JDBCType[] { JDBCType.TIME });
        JDBCTYPES_MAP.put(Timestamp.class.getName(),
            new JDBCType[] { JDBCType.TIMESTAMP });
        JDBCTYPES_MAP.put(Clob.class.getName(),
            new JDBCType[] { JDBCType.CLOB });
        JDBCTYPES_MAP.put(Blob.class.getName(),
            new JDBCType[] { JDBCType.BLOB });
        JDBCTYPES_MAP.put(Array.class.getName(),
            new JDBCType[] { JDBCType.ARRAY });
        JDBCTYPES_MAP.put(Struct.class.getName(),
            new JDBCType[] { JDBCType.STRUCT });
        JDBCTYPES_MAP.put(Ref.class.getName(), new JDBCType[] { JDBCType.REF });
        JDBCTYPES_MAP.put(Object.class.getName(),
            new JDBCType[] { JDBCType.JAVA_OBJECT });

        JDBCTYPES_MAP.put(java.util.Date.class.getName(), new JDBCType[] {
            JDBCType.DATETIME, JDBCType.DATE });
    }

    public void activate() throws SQLException {

        if (activated_) {
            return;
        }

        gatherDatabaseMetaData(ds_);
        tableName_ = getTableName0();
        columns_ = getColumnMetaData0();
        tableConstraints_ = getTableConstraints();

        activated_ = true;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public void setBeanClass(Class<?> beanClass) {
        beanClass_ = beanClass;
        try {
            beanInfo_ = Introspector.getBeanInfo(beanClass);
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }

        s2daoBean_ = beanClass.getAnnotation(Bean.class);
        if (s2daoBean_ != null) {
            String[] noPersistentProperty = s2daoBean_.noPersistentProperty();
            for (int i = 0; i < noPersistentProperty.length; i++) {
                noPersistentPropertySet_.add(noPersistentProperty[i]);
            }
        }
        // java.lang.Objectのメソッドは抜いておく。
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(Object.class);
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            noPersistentPropertySet_.add(pds[i].getName().toUpperCase());
        }
    }

    public String getTableName() {
        return tableName_;
    }

    String getTableName0() {
        String tableName = null;

        if (s2daoBean_ != null) {
            tableName = s2daoBean_.table();
        }
        if (tableName == null) {
            tableName = beanClass_.getName();
            int dot = tableName.lastIndexOf('.');
            if (dot >= 0) {
                tableName = tableName.substring(dot + 1);
            }
            int dollar = tableName.lastIndexOf('$');
            if (dollar >= 0) {
                tableName = tableName.substring(dollar + 1);
            }
            tableName = tableName.toUpperCase();
        }

        return tableName;
    }

    public ColumnMetaData getColumnMetaData(String columnName) {
        return (ColumnMetaData) columnMap_.get(columnName.toUpperCase());
    }

    public ColumnMetaData[] getColumnMetaData() {
        return columns_;
    }

    ColumnMetaData[] getColumnMetaData0() {
        PropertyDescriptor[] descriptors = beanInfo_.getPropertyDescriptors();
        List<ColumnMetaData> columnMetaDataList = new ArrayList<ColumnMetaData>(
            descriptors.length);
        for (int i = 0; i < descriptors.length; i++) {
            String propertyName = descriptors[i].getName().toUpperCase();
            if (noPersistentPropertySet_.contains(propertyName)) {
                continue;
            }

            ColumnMetaData columnMetaData = new ColumnMetaData();
            columnMetaData.setIdentity(identity_);
            columnMetaData.setPropertyDescriptor(descriptors[i]);
            columnMetaData.setName(propertyName);
            String jdbcTypeName = getSuitableJDBCTypeName(descriptors[i]
                .getPropertyType().getName());
            if (jdbcTypeName != null) {
                columnMetaData.setJdbcTypeName(jdbcTypeName);
            }
            if (descriptors[i].getPropertyType().isPrimitive()) {
                columnMetaData.setNotNull(true);
            }

            applyAnnotations(columnMetaData, descriptors[i].getReadMethod());
            applyAnnotations(columnMetaData, descriptors[i].getWriteMethod());

            columnMetaDataList.add(columnMetaData);
            columnMap_.put(columnMetaData.getName().toUpperCase(),
                columnMetaData);
        }

        return columnMetaDataList.toArray(new ColumnMetaData[0]);
    }

    void applyAnnotations(ColumnMetaData columnMetaData, Method method) {
        if (method == null) {
            return;
        }
        ColumnDetail column = getColumnAnnotation(method);
        if (column != null) {
            String columnName = column.name();
            if (columnName != null && columnName.length() > 0) {
                columnMetaData.setName(columnName);
            }

            String jdbcTypeName = null;
            JDBCType jdbcType = column.type();
            if (jdbcType != null) {
                jdbcTypeName = actualJdbcTypeNameByTypeMap_.get(jdbcType);
            }
            if (jdbcTypeName != null) {
                columnMetaData.setJdbcTypeName(jdbcTypeName);
            }

            String defaultValue = column.defaultValue();
            if (defaultValue.length() != 0) {
                columnMetaData.setDefault(defaultValue);
            }

            Constraint[] constraint = column.constraint();
            for (int i = 0; i < constraint.length; i++) {
                if (constraint[i] == Constraint.NOT_NULL) {
                    columnMetaData.setNotNull(true);
                } else if (constraint[i] == Constraint.PRIMARY_KEY) {
                    columnMetaData.setPrimaryKey(true);
                    columnMetaData.setNotNull(true);
                } else if (constraint[i] == Constraint.UNIQUE) {
                    columnMetaData.setUnique(true);
                    columnMetaData.setNotNull(true);
                } else {
                    throw new RuntimeException("Unsupported constraint: "
                        + constraint[i]);
                }
            }

            boolean index = column.index();
            columnMetaData.setIndexCreated(index);
        }

        Id id = method.getAnnotation(Id.class);
        if (id != null) {
            columnMetaData.setPrimaryKey(true);
            columnMetaData.setNotNull(true);
            switch (id.value()) {
            case IDENTITY:
                columnMetaData.setId(true);
                break;

            case ASSIGNED:
                break;

            case SEQUENCE:
                columnMetaData.setId(true);
                String sequenceName = id.sequenceName();
                if (sequenceName == null) {
                    throw new IllegalStateException(
                        "IdType is specified as SEQUENCE but sequenceName is not specified: method="
                            + method);
                }
                columnMetaData.setSequenceName(sequenceName);
                break;

            default:
                if (logger_.isInfoEnabled()) {
                    logger_.info("[SKIP] Unsupported Id annotation value: "
                        + id.value() + ": method=" + method);
                }
            }
        }
    }

    private ColumnDetail getColumnAnnotation(Method method) {
        ColumnDetail column = method.getAnnotation(ColumnDetail.class);
        if (column == null) {
            final org.seasar.dao.annotation.tiger.Column s2DaoColumn = method
                .getAnnotation(org.seasar.dao.annotation.tiger.Column.class);
            if (s2DaoColumn != null) {
                column = new ColumnDetail() {
                    public String name() {
                        return s2DaoColumn.value();
                    }

                    public JDBCType type() {
                        return null;
                    }

                    public String defaultValue() {
                        return "";
                    }

                    public Constraint[] constraint() {
                        return new Constraint[0];
                    }

                    public boolean index() {
                        return false;
                    }

                    public Class<? extends Annotation> annotationType() {
                        return null;
                    }
                };
            }
        } else if (column.name().length() == 0) {
            final org.seasar.dao.annotation.tiger.Column s2DaoColumn = method
                .getAnnotation(org.seasar.dao.annotation.tiger.Column.class);
            if (s2DaoColumn != null) {
                final ColumnDetail origColumn = column;
                column = new ColumnDetail() {
                    public String name() {
                        return s2DaoColumn.value();
                    }

                    public JDBCType type() {
                        return origColumn.type();
                    }

                    public String defaultValue() {
                        return origColumn.defaultValue();
                    }

                    public Constraint[] constraint() {
                        return origColumn.constraint();
                    }

                    public boolean index() {
                        return origColumn.index();
                    }

                    public Class<? extends Annotation> annotationType() {
                        return null;
                    }
                };
            }
        }
        return column;
    }

    TableConstraint[] getTableConstraints() {
        List<TableConstraint> tableConstraintList = new ArrayList<TableConstraint>();

        do {
            PrimaryKey primaryKey = beanClass_.getAnnotation(PrimaryKey.class);
            if (primaryKey == null) {
                break;
            }
            String value = primaryKey.value();
            if (value == null) {
                break;
            }
            TableConstraint tableConstraint = new TableConstraint();
            tableConstraint.setName(TableConstraint.PRIMARY_KEY);
            tableConstraint.setColumnNames(toStringArray(value));
            tableConstraintList.add(tableConstraint);

            Unique unique = beanClass_.getAnnotation(Unique.class);
            if (unique == null) {
                break;
            }
            String[] values = unique.value();
            if (values == null) {
                break;
            }
            for (int i = 0; i < values.length; i++) {
                tableConstraint = new TableConstraint();
                tableConstraint.setName(TableConstraint.UNIQUE);
                tableConstraint.setColumnNames(toStringArray(values[i]));
                tableConstraintList.add(tableConstraint);
            }
        } while (false);

        return tableConstraintList.toArray(new TableConstraint[0]);
    }

    public boolean update() throws SQLException {
        return update(true);
    }

    public boolean update(boolean correctTableSchema) throws SQLException {

        if (!activated_) {
            throw new IllegalStateException("Not activated");
        }

        if (!identity_.existsTable(tableName_)) {
            return createTable();
        } else if (correctTableSchema) {
            return correctTableSchema();
        }
        return false;
    }

    public boolean createTable() throws SQLException {
        return createTable(false);
    }

    public boolean createTable(boolean force) throws SQLException {
        if (!force && identity_.existsTable(tableName_)) {
            return false;
        }
        executeUpdate(constructCreateTableSQLs());
        executeUpdate(constructCreateIndexSQLs());
        return true;
    }

    public boolean correctTableSchema() throws SQLException {
        boolean modified = false;

        Set<String> currentColumnSet = new HashSet<String>();
        String[] columnNames = identity_.getColumns(tableName_);
        for (int i = 0; i < columnNames.length; i++) {
            currentColumnSet.add(columnNames[i].toUpperCase());
        }
        for (int i = 0; i < columns_.length; i++) {
            String columnName = columns_[i].getName().toUpperCase();
            if (currentColumnSet.contains(columnName)) {
                currentColumnSet.remove(columnName);
            } else {
                addColumn(columns_[i]);
                modified = true;
            }
        }
        for (Iterator itr = currentColumnSet.iterator(); itr.hasNext();) {
            dropColumn((String) itr.next());
            modified = true;
        }

        return modified;
    }

    public boolean dropTable() throws SQLException {
        return dropTable(false);
    }

    public boolean dropTable(boolean force) throws SQLException {
        if (!force && !identity_.existsTable(tableName_)) {
            return false;
        }
        executeUpdate(constructDropTableSQLs());
        executeUpdate(constructDropIndexSQLs());
        return true;
    }

    public String[] constructCreateTableSQLs() {
        List<String> sqlList = new ArrayList<String>();

        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE ").append(tableName_).append(" (");
        String delim = "";
        for (int i = 0; i < columns_.length; i++) {
            sb.append(delim);
            delim = ", ";
            String columnName = columns_[i].getName();
            sb.append(columnName).append(" ").append(
                columns_[i].getDefinitionSQL(tableName_));
            sqlList.addAll(Arrays.asList(columns_[i]
                .getAdditionalDefinitionSQLs(tableName_)));
        }
        for (int i = 0; i < tableConstraints_.length; i++) {
            String[] names = tableConstraints_[i].getColumnNames();
            if (names.length == 0) {
                continue;
            }
            sb.append(delim);
            delim = ", ";
            sb.append(tableConstraints_[i].getName()).append(" (");
            String delim2 = "";
            for (int j = 0; j < names.length; j++) {
                sb.append(delim2);
                delim2 = ", ";
                sb.append(names[j]);
            }
            sb.append(")");
        }
        sb.append(")");
        sqlList.add(sb.toString());

        return sqlList.toArray(new String[0]);
    }

    public String[] constructCreateIndexSQLs() {
        List<String> indexList = new ArrayList<String>();

        for (int i = 0; i < columns_.length; i++) {
            if (!columns_[i].isIndexCreated()) {
                continue;
            }
            indexList.add("CREATE INDEX "
                + columns_[i].getIndexName(tableName_) + " ("
                + columns_[i].getName() + ")");
        }

        Index index = beanClass_.getAnnotation(Index.class);
        if (index != null) {
            String[] value = index.value();
            for (int i = 0; i < value.length; i++) {
                String[] columns = toStringArray(value[i]);
                if (columns.length == 0) {
                    continue;
                }
                StringBuffer sb = new StringBuffer();
                sb.append("CREATE INDEX ");
                appendIndexName(sb, columns).append(" (");
                String delim = "";
                for (int j = 0; j < columns.length; j++) {
                    sb.append(delim);
                    delim = ", ";
                    sb.append(columns[j]);
                }
                sb.append(")");
                indexList.add(sb.toString());
            }
        }
        return indexList.toArray(new String[0]);
    }

    StringBuffer appendIndexName(StringBuffer sb, String[] columns) {
        sb.append("_IDX_").append(tableName_);
        for (int i = 0; i < columns.length; i++) {
            sb.append("_").append(columns[i]);
        }
        return sb;
    }

    public String[] constructDropTableSQLs() {
        List<String> sqlList = new ArrayList<String>();

        sqlList.add("DROP TABLE " + tableName_);
        for (int i = 0; i < columns_.length; i++) {
            sqlList.addAll(Arrays.asList(columns_[i]
                .getDeletionSQLs(tableName_)));
        }

        return sqlList.toArray(new String[0]);
    }

    public String[] constructDropIndexSQLs() {
        List<String> indexList = new ArrayList<String>();

        for (int i = 0; i < columns_.length; i++) {
            if (!columns_[i].isIndexCreated()) {
                continue;
            }
            indexList.add("DROP INDEX " + columns_[i].getIndexName(tableName_));
        }

        Index index = beanClass_.getAnnotation(Index.class);
        if (index != null) {
            String[] value = index.value();
            for (int i = 0; i < value.length; i++) {
                String[] columns = toStringArray(value[i]);
                if (columns.length == 0) {
                    continue;
                }
                StringBuffer sb = new StringBuffer();
                sb.append("DROP INDEX ");
                appendIndexName(sb, columns).append(")");
                indexList.add(sb.toString());
            }
        }
        return indexList.toArray(new String[0]);
    }

    void addColumn(ColumnMetaData column) throws SQLException {
        executeUpdate(identity_.getAlterTableAddColumnSQL(tableName_, column));
    }

    void dropColumn(String columnName) throws SQLException {
        executeUpdate(identity_.getAlterTableDropColumnSQL(tableName_,
            columnName));
    }

    String[] toStringArray(String value) {
        if (value == null) {
            return new String[0];
        }
        List<String> list = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(value, ",");
        while (st.hasMoreTokens()) {
            String tkn = st.nextToken().trim();
            if (tkn.length() > 0) {
                list.add(tkn);
            }
        }
        return list.toArray(new String[0]);
    }

    String getSuitableJDBCTypeName(String javaTypeName) {
        JDBCType[] jdbcTypes = JDBCTYPES_MAP.get(javaTypeName);
        if (jdbcTypes == null) {
            return null;
        }
        for (int i = 0; i < jdbcTypes.length; i++) {
            String typeName = jdbcTypes[i].getName();
            if (actualJdbcTypeNameSet_.contains(typeName)) {
                return typeName;
            } else {
                typeName = (String) actualJdbcTypeNameByTypeMap_
                    .get(new Integer(jdbcTypes[i].getType()));
                if (typeName != null) {
                    return typeName;
                }
            }
        }
        return "VARCHAR";
    }

    void gatherDatabaseMetaData(DataSource ds) throws SQLException {
        actualJdbcTypeNameSet_.clear();
        actualJdbcTypeNameByTypeMap_.clear();

        Connection con = null;
        ResultSet rs = null;
        try {
            con = ds.getConnection();
            DatabaseMetaData metaData = con.getMetaData();
            rs = metaData.getTypeInfo();
            while (rs.next()) {
                String typeName = rs.getString("TYPE_NAME");
                JDBCType jdbcType = JDBCType
                    .getInstance(rs.getInt("DATA_TYPE"));
                actualJdbcTypeNameSet_.add(typeName);
                if (!actualJdbcTypeNameByTypeMap_.containsKey(jdbcType)) {
                    actualJdbcTypeNameByTypeMap_.put(jdbcType, typeName);
                }
            }
        } finally {
            DbUtils.closeQuietly(con, null, rs);
        }

        if (identity_ instanceof HsqlIdentity) {
            if (!actualJdbcTypeNameSet_.contains("DATETIME")) {
                actualJdbcTypeNameSet_.add("DATETIME");
            }
        }
    }

    void executeUpdate(String sql) throws SQLException {
        executeUpdate(new String[] { sql });
    }

    void executeUpdate(String[] sqls) throws SQLException {
        Connection con = null;
        try {
            con = ds_.getConnection();
            for (int i = 0; i < sqls.length; i++) {
                Statement st = null;
                try {
                    st = con.createStatement();
                    System.out.println("EXECUTE: " + sqls[i]);
                    st.executeUpdate(sqls[i]);
                } catch (SQLException ex) {
                    if (logger_.isDebugEnabled()) {
                        logger_.debug("SQL execution has been failed: SQL="
                            + sqls[i], ex);
                    }
                } finally {
                    DbUtils.closeQuietly(st);
                }
            }
        } finally {
            DbUtils.closeQuietly(con);
        }
    }

    public void setDataSource(DataSource ds) {
        ds_ = ds;
    }

    public void setIdentity(Identity identity) {
        identity_ = identity;
    }
}
