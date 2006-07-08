package org.seasar.cms.beantable;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.seasar.cms.beantable.identity.Identity;
import org.seasar.cms.beantable.impl.ColumnMetaData;

/**
 * <p>
 * <b>同期化：</b> このインタフェースの実装クラスはスレッドセーフである必要があります。
 * </p>
 * 
 * @author YOKOTA Takehiko
 */
public interface BeanTable {

    void activate() throws SQLException;

    boolean update() throws SQLException;

    boolean update(boolean correctTableSchema) throws SQLException;

    boolean createTable() throws SQLException;

    boolean createTable(boolean force) throws SQLException;

    boolean dropTable() throws SQLException;

    boolean dropTable(boolean force) throws SQLException;

    boolean correctTableSchema() throws SQLException;

    Class getBeanClass();

    void setBeanClass(Class<?> beanClass);

    String getTableName();

    ColumnMetaData getColumnMetaData(String columnName);

    ColumnMetaData[] getColumnMetaData();

    /*
     * for framework
     */

    void setDataSource(DataSource ds);

    void setIdentity(Identity identity);
}
