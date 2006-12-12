package org.seasar.cms.beantable.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.seasar.cms.beantable.Beantable;
import org.seasar.cms.beantable.Formula;
import org.seasar.cms.beantable.JDBCType;
import org.seasar.cms.beantable.Null;
import org.seasar.cms.beantable.annotation.ColumnDetail;
import org.seasar.cms.beantable.annotation.Constraint;
import org.seasar.cms.beantable.annotation.Index;
import org.seasar.cms.beantable.annotation.PrimaryKey;
import org.seasar.cms.beantable.annotation.Unique;
import org.seasar.cms.beantable.handler.BeantableHandler;
import org.seasar.cms.beantable.handler.BeantableListHandler;
import org.seasar.cms.database.identity.ColumnMetaData;
import org.seasar.cms.database.identity.ConstraintMetaData;
import org.seasar.cms.database.identity.Identity;
import org.seasar.cms.database.identity.IndexMetaData;
import org.seasar.cms.database.identity.TableMetaData;
import org.seasar.cms.database.identity.impl.HsqlIdentity;
import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;
import org.seasar.framework.container.annotation.tiger.Aspect;
import org.seasar.framework.log.Logger;

/**
 * <p>
 * <b>同期化：</b> このクラスはスレッドセーフではありません。
 * </p>
 *
 * @author YOKOTA Takehiko
 */
public class BeantableImpl<T> implements Beantable<T> {

    private DataSource ds_;

    private Identity identity_;

    private Class<T> beanClass_;

    private BeanInfo beanInfo_;

    private Set<String> actualJdbcTypeNameSet_ = new HashSet<String>();

    private Map<JDBCType, String> actualJdbcTypeNameByTypeMap_ = new HashMap<JDBCType, String>();

    private TableMetaData table_;

    private BeantableHandler<T> beantableHandler_;

    private BeantableListHandler<T> beantableListHandler_;

    private boolean activated_;

    private Logger logger_ = Logger.getLogger(getClass());

    private static final Map<String, JDBCType[]> JDBCTYPES_MAP = new HashMap<String, JDBCType[]>();

    public BeantableImpl() {
    }

    public BeantableImpl(Class<T> beanClass) {
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

    public void setDataSource(DataSource ds) {
        ds_ = ds;
    }

    public void setIdentity(Identity identity) {
        identity_ = identity;
    }

    public void activate() throws SQLException {

        if (activated_) {
            return;
        }

        gatherDatabaseMetaData();
        table_ = createTableMetaData();
        beantableHandler_ = new BeantableHandler<T>(this);
        beantableListHandler_ = new BeantableListHandler<T>(this);

        activated_ = true;
    }

    public Class<T> getBeanClass() {

        return beanClass_;
    }

    public void setBeanClass(Class<T> beanClass) {

        beanClass_ = beanClass;
        try {
            beanInfo_ = Introspector.getBeanInfo(beanClass_);
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }
    }

    void gatherDatabaseMetaData() throws SQLException {

        actualJdbcTypeNameSet_.clear();
        actualJdbcTypeNameByTypeMap_.clear();

        Connection con = null;
        ResultSet rs = null;
        try {
            con = ds_.getConnection();
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

    TableMetaData createTableMetaData() {

        TableMetaData table = new TableMetaData(gatherTableName());
        table.setColumns(gatherColumnMetaData(gatherNoPersistentProperties()));
        table.setConstraints(gatherConstraintMetaData());
        table.setIndexes(gatherIndexMetaData());
        return table;
    }

    String gatherTableName() {

        String tableName = null;

        Bean bean = beanClass_.getAnnotation(Bean.class);
        if (bean != null) {
            tableName = bean.table();
        }
        if (tableName == null || tableName.length() == 0) {
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

    Set<String> gatherNoPersistentProperties() {

        Set<String> noPersistentPropertySet = new HashSet<String>();
        Bean bean = beanClass_.getAnnotation(Bean.class);
        if (bean != null) {
            String[] noPersistentProperty = bean.noPersistentProperty();
            for (int i = 0; i < noPersistentProperty.length; i++) {
                noPersistentPropertySet.add(noPersistentProperty[i]);
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
            noPersistentPropertySet.add(pds[i].getName());
        }

        return noPersistentPropertySet;
    }

    ColumnMetaData[] gatherColumnMetaData(Set<String> noPersistentPropertySet) {

        PropertyDescriptor[] descriptors = beanInfo_.getPropertyDescriptors();
        Arrays.sort(descriptors, new Comparator<PropertyDescriptor>() {
            public int compare(PropertyDescriptor o1, PropertyDescriptor o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        List<ColumnMetaData> columnMetaDataList = new ArrayList<ColumnMetaData>(
                descriptors.length);
        for (int i = 0; i < descriptors.length; i++) {
            String propertyName = descriptors[i].getName();
            if (noPersistentPropertySet.contains(propertyName)) {
                continue;
            }

            ColumnMetaData columnMetaData = new ColumnMetaData();
            columnMetaData.setPropertyDescriptor(descriptors[i]);
            columnMetaData.setName(propertyName.toUpperCase());
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
            if (columnName != null) {
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
            if (defaultValue != null) {
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

    ColumnDetail getColumnAnnotation(Method method) {

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
                        return null;
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
        } else if (column.name() == null) {
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

    ConstraintMetaData[] gatherConstraintMetaData() {

        List<ConstraintMetaData> constraintList = new ArrayList<ConstraintMetaData>();

        do {
            PrimaryKey primaryKey = beanClass_.getAnnotation(PrimaryKey.class);
            if (primaryKey == null) {
                break;
            }
            String value = primaryKey.value();
            if (value == null) {
                break;
            }
            ConstraintMetaData constraint = new ConstraintMetaData();
            constraint.setName(ConstraintMetaData.PRIMARY_KEY);
            constraint.setColumnNames(toStringArray(value));
            constraintList.add(constraint);

            Unique unique = beanClass_.getAnnotation(Unique.class);
            if (unique == null) {
                break;
            }
            String[] values = unique.value();
            if (values.length == 0) {
                break;
            }
            for (int i = 0; i < values.length; i++) {
                constraint = new ConstraintMetaData();
                constraint.setName(ConstraintMetaData.UNIQUE);
                constraint.setColumnNames(toStringArray(values[i]));
                constraintList.add(constraint);
            }
        } while (false);

        return constraintList.toArray(new ConstraintMetaData[0]);
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

    IndexMetaData[] gatherIndexMetaData() {

        List<IndexMetaData> indexList = new ArrayList<IndexMetaData>();

        do {
            Index index = beanClass_.getAnnotation(Index.class);
            if (index == null) {
                break;
            }
            String[] values = index.value();
            if (values.length == 0) {
                break;
            }
            for (int i = 0; i < values.length; i++) {
                IndexMetaData metaData = new IndexMetaData();
                metaData.setColumnNames(toStringArray(values[i]));
                indexList.add(metaData);
            }
        } while (false);

        return indexList.toArray(new IndexMetaData[0]);
    }

    public TableMetaData getTableMetaData() {

        return table_;
    }

    public boolean update() throws SQLException {

        return update(true);
    }

    public boolean update(boolean correctTableSchema) throws SQLException {

        if (!activated_) {
            throw new IllegalStateException("Not activated");
        }

        if (!identity_.existsTable(table_.getName())) {
            return createTable();
        } else if (correctTableSchema) {
            return correctTableSchema();
        }
        return false;
    }

    public boolean createTable() throws SQLException {

        return identity_.createTable(table_);
    }

    public boolean createTable(boolean force) throws SQLException {

        return identity_.createTable(table_, force);
    }

    public boolean correctTableSchema() throws SQLException {

        return identity_.correctTableSchema(table_);
    }

    public boolean correctTableSchema(boolean force) throws SQLException {

        return identity_.correctTableSchema(table_, force);
    }

    public boolean dropTable() throws SQLException {

        return identity_.dropTable(table_);
    }

    public boolean dropTable(boolean force) throws SQLException {

        return identity_.dropTable(table_, force);
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

    @Aspect("j2ee.requiredTx")
    public void insertColumn(T bean) throws SQLException {

        Connection con = null;
        PreparedStatement pst = null;
        try {
            con = getConnection();

            pst = prepareInsertStatement(con, bean);
            pst.executeUpdate();

            ColumnMetaData idColumn = table_.getIdColumn();
            if (idColumn != null) {
                setValue(bean, idColumn, identity_.getGeneratedId(table_));
            }
        } finally {
            DbUtils.closeQuietly(con, pst, null);
        }
    }

    Connection getConnection() throws SQLException {
        return ds_.getConnection();
    }

    PreparedStatement prepareInsertStatement(Connection con, T bean)
            throws SQLException {
        StringBuffer columnSb = new StringBuffer("(");
        List<Object> list = new ArrayList<Object>();
        StringBuffer sb = new StringBuffer("(");
        String delim = "";
        ColumnMetaData[] columns = table_.getColumns();
        for (int i = 0; i < columns.length; i++) {
            Object value = getValue(bean, columns[i]);
            if (columns[i].isId() && value == null) {
                String sql = identity_.getSQLToGenerateNextId(table_,
                        columns[i]);
                if (sql != null) {
                    columnSb.append(delim).append(columns[i].getName());
                    sb.append(delim).append(sql);
                    delim = ",";
                }
            } else {
                if (value == null) {
                    continue;
                } else if (value == Null.INSTANCE) {
                    // Null.INSTANCEがセットされている場合はnullが指定されたものとみなす。
                    value = null;
                }

                columnSb.append(delim).append(columns[i].getName());
                sb.append(delim).append("?");
                delim = ",";
                list.add(value);
            }
        }
        if (delim == null) {
            return null;
        }
        columnSb.append(")");
        sb.append(")");
        PreparedStatement pst = con.prepareStatement("INSERT INTO "
                + table_.getName() + columnSb.toString() + " VALUES "
                + sb.toString());
        int size = list.size();
        int cnt = 1;
        for (int i = 0; i < size; i++) {
            setObject(pst, cnt++, list.get(i));
        }
        return pst;
    }

    void setObject(PreparedStatement pst, int idx, Object obj)
            throws SQLException {
        if (obj instanceof java.util.Date) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            obj = df.format((java.util.Date) obj);
        }
        pst.setObject(idx, obj);
    }

    public Object getValue(T bean, String columnName) {

        ColumnMetaData column = table_.getColumn(columnName);
        if (column == null) {
            throw new IllegalArgumentException("Unknown column: " + columnName);
        }
        return getValue(bean, column);
    }

    public Object getValue(T bean, ColumnMetaData column) {
        return invoke(bean, column.getPropertyDescriptor().getReadMethod(),
                new Object[0]);
    }

    public void setValue(T bean, String columnName, Object value) {
        ColumnMetaData column = table_.getColumn(columnName);
        if (column == null) {
            throw new IllegalArgumentException("Unknown column: " + columnName);
        }
        setValue(bean, column, value);
    }

    public void setValue(T bean, ColumnMetaData column, Object value) {
        if (value == null) {
            return;
        }
        Method method = column.getPropertyDescriptor().getWriteMethod();
        value = adjust(value, method.getParameterTypes()[0]);
        if (value == null) {
            return;
        }
        invoke(bean, method, new Object[] { value });
    }

    Object adjust(Object value, Class type) {
        if (type.isInstance(value)) {
            return value;
        } else if (type == Boolean.TYPE || type == Boolean.class) {
            if (value instanceof Boolean) {
                return value;
            } else if (value instanceof Number) {
                return Boolean.valueOf((((Number) value).intValue() != 0));
            } else if (value == null) {
                return Boolean.FALSE;
            } else {
                return null;
            }
        } else if (type == Character.TYPE || type == Character.class) {
            if (value instanceof Character) {
                return value;
            } else if (value instanceof Number) {
                return (char) ((Number) value).intValue();
            } else if (value == null) {
                return '\0';
            } else {
                return null;
            }
        } else if (type == Short.TYPE || type == Short.class) {
            if (value instanceof Short) {
                return value;
            } else if (value instanceof Number) {
                return ((Number) value).shortValue();
            } else if (value == null) {
                return (short) 0;
            } else {
                return null;
            }
        } else if (type == Integer.TYPE || type == Integer.class) {
            if (value instanceof Integer) {
                return value;
            } else if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value == null) {
                return 0;
            } else {
                return null;
            }
        } else if (type == Long.TYPE || type == Long.class) {
            if (value instanceof Long) {
                return value;
            } else if (value instanceof Number) {
                return ((Number) value).longValue();
            } else if (value == null) {
                return 0L;
            } else {
                return null;
            }
        } else if (type == Float.TYPE || type == Float.class) {
            if (value instanceof Float) {
                return value;
            } else if (value instanceof Number) {
                return ((Number) value).floatValue();
            } else if (value == null) {
                return 0F;
            } else {
                return null;
            }
        } else if (type == Double.TYPE || type == Double.class) {
            if (value instanceof Double) {
                return value;
            } else if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value == null) {
                return 0D;
            } else {
                return null;
            }
        }
        return null;
    }

    Object invoke(Object obj, Method method, Object[] params) {
        try {
            return method.invoke(obj, params);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Aspect("j2ee.requiredTx")
    public int updateColumns(T bean, Formula formula) throws SQLException {
        Connection con = null;
        PreparedStatement pst = null;
        try {
            con = getConnection();

            pst = prepareUpdateStatement(con, bean, formula);
            return pst.executeUpdate();
        } finally {
            DbUtils.closeQuietly(con, pst, null);
        }
    }

    PreparedStatement prepareUpdateStatement(Connection con, T bean,
            Formula formula) throws SQLException {
        List<Object> list = new ArrayList<Object>();
        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE ").append(table_.getName()).append(" SET ");
        String delim = null;
        ColumnMetaData[] columns = table_.getColumns();
        ColumnMetaData versionNoColumn = null;
        Object versionNoValue = null;
        for (int i = 0; i < columns.length; i++) {
            Object value = getValue(bean, columns[i]);
            if (columns[i].isVersionNo()) {
                versionNoColumn = columns[i];
                versionNoValue = value;
            } else {
                if (value == null) {
                    continue;
                } else if (value == Null.INSTANCE) {
                    // Null.INSTANCEがセットされている場合はnullが指定されたものとみなす。
                    value = null;
                }
            }

            if (delim != null) {
                sb.append(delim);
            } else {
                delim = ",";
            }
            sb.append(columns[i].getName()).append("=");
            if (columns[i].isVersionNo()) {
                sb.append(columns[i].getName()).append("+1");
            } else {
                sb.append("?");
                list.add(value);
            }
        }
        if (delim == null) {
            return null;
        }
        if (formula != null) {
            sb.append(" WHERE ").append(formula.getBase());
        }
        if (versionNoValue != null) {
            if (formula != null) {
                sb.append(" AND ");
            } else {
                sb.append(" WHERE ");
            }
            sb.append(versionNoColumn.getName()).append("=?");
            list.add(versionNoValue);
        }

        PreparedStatement pst = con.prepareStatement(sb.toString());
        int size = list.size();
        int cnt = 1;
        for (int i = 0; i < size; i++) {
            setObject(pst, cnt++, list.get(i));
        }
        if (formula != null) {
            Object[] params = formula.getParameters();
            for (int i = 0; i < params.length; i++) {
                setObject(pst, cnt++, params[i]);
            }
        }
        return pst;
    }

    public T newBeanInstance() {
        try {
            return beanClass_.newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Aspect("j2ee.requiredTx")
    @SuppressWarnings("unchecked")
    public T selectColumn(Formula formula) throws SQLException {
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            pst = prepareSelectStatement(con, formula);
            rs = pst.executeQuery();
            return (T) beantableHandler_.handle(rs);
        } finally {
            DbUtils.closeQuietly(con, pst, rs);
        }
    }

    @Aspect("j2ee.requiredTx")
    @SuppressWarnings("unchecked")
    public T[] selectColumns(Formula formula) throws SQLException {
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            pst = prepareSelectStatement(con, formula);
            rs = pst.executeQuery();
            return ((List<T>) beantableListHandler_.handle(rs))
                    .toArray((T[]) java.lang.reflect.Array.newInstance(
                            beanClass_, 0));
        } finally {
            DbUtils.closeQuietly(con, pst, rs);
        }
    }

    PreparedStatement prepareSelectStatement(Connection con, Formula formula)
            throws SQLException {
        List<Object> list = new ArrayList<Object>();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM ").append(table_.getName());
        if (formula != null) {
            sb.append(" WHERE ").append(formula.getBase());
        }
        PreparedStatement pst = con.prepareStatement(sb.toString());
        int size = list.size();
        int cnt = 1;
        for (int i = 0; i < size; i++) {
            setObject(pst, cnt++, list.get(i));
        }
        if (formula != null) {
            Object[] params = formula.getParameters();
            for (int i = 0; i < params.length; i++) {
                setObject(pst, cnt++, params[i]);
            }
        }
        return pst;
    }

    public int deleteColumns(Formula formula) throws SQLException {
        Connection con = null;
        PreparedStatement pst = null;
        try {
            con = getConnection();

            pst = prepareDeleteStatement(con, formula);
            return pst.executeUpdate();
        } finally {
            DbUtils.closeQuietly(con, pst, null);
        }
    }

    PreparedStatement prepareDeleteStatement(Connection con, Formula formula)
            throws SQLException {
        List<Object> list = new ArrayList<Object>();
        StringBuffer sb = new StringBuffer();
        sb.append("DELETE FROM ").append(table_.getName());
        if (formula != null) {
            sb.append(" WHERE ").append(formula.getBase());
        }
        PreparedStatement pst = con.prepareStatement(sb.toString());
        int size = list.size();
        int cnt = 1;
        for (int i = 0; i < size; i++) {
            setObject(pst, cnt++, list.get(i));
        }
        if (formula != null) {
            Object[] params = formula.getParameters();
            for (int i = 0; i < params.length; i++) {
                setObject(pst, cnt++, params[i]);
            }
        }
        return pst;
    }

    public DataSource getDataSource() {
        return ds_;
    }

    public Identity getIdentity() {
        return identity_;
    }
}
