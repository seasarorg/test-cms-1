package org.seasar.cms.beantable;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.seasar.cms.database.identity.ColumnMetaData;
import org.seasar.cms.database.identity.Identity;
import org.seasar.cms.database.identity.TableMetaData;

/**
 * <p>
 * <b>同期化：</b> このインタフェースの実装クラスはスレッドセーフである必要があります。
 * </p>
 *
 * @author YOKOTA Takehiko
 */
public interface Beantable<T> {

    void activate() throws SQLException;

    boolean update() throws SQLException;

    boolean update(boolean correctTableSchema) throws SQLException;

    boolean createTable() throws SQLException;

    boolean createTable(boolean force) throws SQLException;

    boolean dropTable() throws SQLException;

    boolean dropTable(boolean force) throws SQLException;

    boolean correctTableSchema() throws SQLException;

    Class<T> getBeanClass();

    void setBeanClass(Class<T> beanClass);

    TableMetaData getTableMetaData();

    T selectColumn(Formula formula) throws SQLException;

    T[] selectColumns(Formula formula) throws SQLException;

    void insertColumn(T bean) throws SQLException;

    int updateColumns(T bean, Formula formula) throws SQLException;

    int deleteColumns(Formula formula) throws SQLException;

    T newBeanInstance();

    Object getValue(T bean, String columnName);

    Object getValue(T bean, ColumnMetaData column);

    void setValue(T bean, String columnName, Object value);

    /**
     * Beanの持つ、指定されたカラムに対応するプロパティに値を設定します。
     * <p>値がnullの場合や型が合わない場合は何もしません。</p>
     *
     * @param bean 値を設定する対象であるBean。
     * @param column カラム。
     * @param value 値。
     */
    void setValue(T bean, ColumnMetaData column, Object value);

    DataSource getDataSource();

    Identity getIdentity();

    Object adjust(Object value, Class type);

    /*
     * for framework
     */

    void setDataSource(DataSource ds);

    void setIdentity(Identity identity);
}
