package org.seasar.cms.database.identity;

import java.beans.PropertyDescriptor;

/**
 * <p><b>同期化：</b>
 * このクラスはスレッドセーフではありません。
 * </p>
 *
 * @author YOKOTA Takehiko
 */
public class ColumnMetaData {
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

    private boolean versionNo_;

    private String detail_;

    private String check_;

    private ReferencesMetaData references_;

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

    public boolean isVersionNo() {
        return versionNo_;
    }

    public void setVersionNo(boolean versionNo) {
        versionNo_ = versionNo;
    }

    public String getDetail() {
        return detail_;
    }

    public void setDetail(String detail) {
        detail_ = detail;
    }

    public String getCheck() {
        return check_;
    }

    public void setCheck(String check) {
        check_ = check;
    }

    public ReferencesMetaData getReferences() {
        return references_;
    }

    public void setReferences(ReferencesMetaData references) {
        references_ = references;
    }
}
