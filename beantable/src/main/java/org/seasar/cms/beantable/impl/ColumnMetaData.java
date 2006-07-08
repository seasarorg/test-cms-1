package org.seasar.cms.beantable.impl;

import java.beans.PropertyDescriptor;

import org.seasar.cms.beantable.identity.Identity;

/**
 * <p><b>同期化：</b>
 * このクラスはスレッドセーフではありません。
 * </p>
 * 
 * @author YOKOTA Takehiko
 */
public class ColumnMetaData {
    private Identity identity_;

    private String name_;

    private String jdbcTypeName_;

    private String default_;

    private boolean notNull_;

    private boolean primaryKey_;

    private boolean unique_;

    private boolean id_;

    private String sequenceName_;

    private boolean indexCreated_;

    private PropertyDescriptor propertyDescriptor_;

    public String getDefault() {
        return default_;
    }

    public void setDefault(String defaultValue) {
        default_ = defaultValue;
    }

    public String getJdbcTypeName() {
        return jdbcTypeName_;
    }

    public void setJdbcTypeName(String jdbcTypeName) {
        jdbcTypeName_ = jdbcTypeName;
    }

    public String getName() {
        return name_;
    }

    public void setName(String name) {
        name_ = name;
    }

    public boolean isNotNull() {
        return notNull_;
    }

    public void setNotNull(boolean notNull) {
        notNull_ = notNull;
    }

    public boolean isPrimaryKey() {
        return primaryKey_;
    }

    public void setPrimaryKey(boolean primaryKey) {
        primaryKey_ = primaryKey;
    }

    public boolean isUnique() {
        return unique_;
    }

    public void setUnique(boolean unique) {
        unique_ = unique;
    }

    public boolean isIndexCreated() {
        return indexCreated_;
    }

    public void setIndexCreated(boolean indexCreated) {
        indexCreated_ = indexCreated;
    }

    public boolean isId() {
        return id_;
    }

    public void setId(boolean id) {
        id_ = id;
    }

    public String getSequenceName() {
        return sequenceName_;
    }

    public void setSequenceName(String sequenceName) {
        sequenceName_ = sequenceName;
    }

    public PropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor_;
    }

    public void setPropertyDescriptor(PropertyDescriptor propertyDescriptor) {
        propertyDescriptor_ = propertyDescriptor;
    }

    public String getDefinitionSQL(String tableName) {
        if (id_) {
            return identity_.getIdColumnDefinitionSQL(tableName, name_,
                sequenceName_).getColumnDefinitionSQL();
        }

        StringBuffer sb = new StringBuffer();
        sb.append(jdbcTypeName_);
        if (default_ != null && default_.length() > 0) {
            sb.append(" DEFAULT ").append(default_);
        }
        if (notNull_) {
            sb.append(" NOT NULL");
        }
        if (primaryKey_) {
            sb.append(" PRIMARY KEY");
        } else if (unique_) {
            sb.append(" UNIQUE");
        }

        return sb.toString();
    }

    public String[] getAdditionalDefinitionSQLs(String tableName) {
        if (id_) {
            return identity_.getIdColumnDefinitionSQL(tableName, name_,
                sequenceName_).getAdditionalCreationSQLs();
        } else {
            return new String[0];
        }
    }

    public String[] getDeletionSQLs(String tableName) {
        if (id_) {
            return identity_.getIdColumnDeletionSQL(tableName, name_, sequenceName_)
                .getDeletionSQLs();
        } else {
            return new String[0];
        }
    }

    public String getIndexName(String tableName) {
        return "_IDX_" + tableName + "_" + name_;
    }

    public void setIdentity(Identity identity) {
        identity_ = identity;
    }
}
