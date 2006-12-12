package org.seasar.cms.beantable.impl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.seasar.cms.beantable.Beantable;
import org.seasar.cms.beantable.BeantableDao;
import org.seasar.cms.beantable.QueryNotFoundRuntimeException;
import org.seasar.cms.beantable.handler.BeantableHandler;
import org.seasar.cms.beantable.handler.BeantableListHandler;
import org.seasar.cms.beantable.handler.ScalarListHandler;
import org.seasar.cms.database.SQLRuntimeException;
import org.seasar.framework.container.annotation.tiger.Aspect;

/**
 * <p><b>同期化：</b>
 * このクラスはスレッドセーフです。</p>
 *
 * @author YOKOTA Takehiko
 */
abstract public class BeantableDaoBase<T> implements BeantableDao<T> {

    protected Beantable<T> beantable_;

    protected BeantableHandler<T> beantableHandler_;

    protected BeantableListHandler<T> beantableListHandler_;

    protected ScalarHandler scalarHandler_ = new ScalarHandler();

    protected ScalarListHandler scalarListHandler_ = new ScalarListHandler();

    private boolean started_ = false;

    private String queriesPath_;

    private Properties sql_;

    public final void start() {
        if (started_) {
            return;
        }

        beantable_.setBeanClass(getDtoClass());
        beantableHandler_ = new BeantableHandler<T>(beantable_);
        beantableListHandler_ = new BeantableListHandler<T>(beantable_);
        try {
            beantable_.activate();
        } catch (SQLException ex) {
            throw new SQLRuntimeException(ex);
        }

        Class[] classes = getInterfaceAndClasses(getClass());
        for (int i = 0; i < classes.length; i++) {
            String queriesPath = classes[i].getName().replace('.', '/').concat(
                    ".sql");
            if (getClass().getClassLoader().getResource(queriesPath) != null) {
                queriesPath_ = queriesPath;
                loadQueries();
                break;
            }
        }

        if (sqlForInitializingTableDefined()) {
            initializeTable();
        } else {
            try {
                beantable_.createTable();
            } catch (SQLException ex) {
                throw new SQLRuntimeException(ex);
            }
        }

        started_ = true;
    }

    boolean sqlForInitializingTableDefined() {
        return (getQueryIfExists("initialize") != null);
    }

    abstract protected Class<T> getDtoClass();

    Class[] getInterfaceAndClasses(Class clazz) {
        List<Class> list = new ArrayList<Class>();
        List<Class> classList = new ArrayList<Class>();
        while (clazz != Object.class) {
            classList.add(clazz);
            Class[] ifs = clazz.getInterfaces();
            for (int i = 0; i < ifs.length; i++) {
                list.add(ifs[i]);
            }
            clazz = clazz.getSuperclass();
        }
        list.addAll(classList);
        return list.toArray(new Class[0]);
    }

    /*
     * QueryHandler
     */

    public final String getQuery(String name) {
        String query = getQueryIfExists(name);
        if (query == null) {
            throw new QueryNotFoundRuntimeException(getClass().getName()
                    + ": Query not found; " + name);
        }
        return query;
    }

    public final String getQueryIfExists(String name) {
        if (sql_ != null) {
            return sql_.getProperty(name);
        } else {
            return null;
        }
    }

    public final Pair constructPair(String name, String[] blocks,
            Map<String, Object> namedParamMap, Object[] params) {
        String query = getQuery(name);
        StringBuffer sb = new StringBuffer();
        List<Object> list = new ArrayList<Object>();
        int blockCnt = 0;
        int paramCnt = 0;
        int pre = 0;
        int stat = 0;
        final int len = query.length();
        for (int i = 0; i < len; i++) {
            char ch = query.charAt(i);
            if (stat == 0) {
                if (ch == '?') {
                    // ?
                    sb.append(query.substring(pre, i));
                    stat = 1;
                } else if (ch == ':') {
                    // :
                    sb.append(query.substring(pre, i));
                    pre = i + 1;
                    stat = 2;
                }
            } else if (stat == 1) {
                // ?
                if (ch == '?') {
                    // ??
                    if (blocks != null) {
                        String block = blocks[blockCnt++];
                        sb.append(block);
                        final int l = block.length();
                        for (int j = 0; j < l; j++) {
                            if (block.charAt(j) == '?') {
                                if (params != null) {
                                    list.add(params[paramCnt++]);
                                }
                            }
                        }
                    }
                } else {
                    // ?
                    if (params != null) {
                        sb.append("?");
                        list.add(params[paramCnt++]);
                    }
                    i--;
                }
                pre = i + 1;
                stat = 0;
            } else if (stat == 2) {
                // :
                if (((ch >= 'a') && (ch <= 'z'))
                        || ((ch >= 'A') && (ch <= 'Z')) || (ch == '_')) {
                    ;
                } else {
                    if (pre == i) {
                        sb.append(':');
                    } else {
                        if (namedParamMap != null) {
                            Object param = namedParamMap.get(query.substring(
                                    pre, i));
                            if (param != null) {
                                sb.append('?');
                                list.add(param);
                            }
                        }
                        pre = i;
                    }
                    i--;
                    stat = 0;
                }
            } else {
                throw new RuntimeException("LOGIC ERROR");
            }
        }
        if (stat == 0) {
            sb.append(query.substring(pre));
        } else if (stat == 1) {
            if (params != null) {
                sb.append("?");
                list.add(params[paramCnt++]);
            }
            sb.append(query.substring(pre));
        } else if (stat == 2) {
            if (pre == len) {
                sb.append(':');
            } else {
                if (namedParamMap != null) {
                    Object param = namedParamMap.get(query.substring(pre));
                    if (param != null) {
                        sb.append('?');
                        list.add(param);
                    }
                }
            }
        }

        return new Pair(sb.toString(), list.toArray());
    }

    /*
     * protected scope methods
     */

    protected String getTableName() {
        return beantable_.getTableMetaData().getName();
    }

    protected String getQueriesPath() {
        return queriesPath_;
    }

    protected final Connection getConnection() throws SQLException {
        return beantable_.getDataSource().getConnection();
    }

    protected final Object runQuery(Connection con, String name,
            Map<String, Object> paramMap, ResultSetHandler h)
            throws SQLException {
        Pair pair = constructPair(name, null, paramMap, null);
        return new QueryRunner().query(con, pair.getTemplate(), pair
                .getParameters(), h);
    }

    protected final void runUpdate(Connection con, String name,
            Map<String, Object> paramMap) throws SQLException {
        Pair pair = constructPair(name, null, paramMap, null);
        new QueryRunner().update(con, pair.getTemplate(), pair.getParameters());
    }

    @Aspect("j2ee.requiredTx")
    protected final void initializeTable() {
        String tableName = getTableName();
        try {
            if (beantable_.getIdentity().existsTable(tableName)) {
                return;
            }
        } catch (SQLException ex) {
            throw new SQLRuntimeException(
                    "Can't detect if table exists: table=" + tableName, ex);
        }

        Statement stmt = null;
        try {
            Connection con = getConnection();
            stmt = con.createStatement();

            String pre = getQueryIfExists("initialize.specific.pre");
            if ((pre != null) && (pre.trim().length() > 0)) {
                StringTokenizer st = new StringTokenizer(pre, ",");
                while (st.hasMoreTokens()) {
                    String tkn = st.nextToken().trim();
                    String sql = getQueryIfExists("initialize.specific." + tkn);
                    if (sql != null) {
                        sql = sql.trim();
                        if (sql.length() > 0) {
                            stmt.executeUpdate(sql);
                        }
                    }
                }
            }

            StringTokenizer str = new StringTokenizer(getQuery("initialize"),
                    ",");
            while (str.hasMoreTokens()) {
                String tkn = str.nextToken().trim();
                String sql = getQueryIfExists("initialize." + tkn);
                if (sql != null) {
                    sql = sql.trim();
                    if (sql.length() > 0) {
                        stmt.executeUpdate(sql);
                    }
                }
            }

            String post = getQueryIfExists("initialize.specific.post");
            if ((post != null) && (post.trim().length() > 0)) {
                str = new StringTokenizer(post, ",");
                while (str.hasMoreTokens()) {
                    String tkn = str.nextToken().trim();
                    String sql = getQueryIfExists("initialize.specific." + tkn);
                    if (sql != null) {
                        sql = sql.trim();
                        if (sql.length() > 0) {
                            stmt.executeUpdate(sql);
                        }
                    }
                }
            }
            stmt.close();
            stmt = null;
        } catch (SQLException ex) {
            throw new SQLRuntimeException("Can't initialize database", ex);
        } finally {
            DbUtils.closeQuietly(null, stmt, null);
        }
    }

    void loadQueries() {
        String queriesPath = getQueriesPath();
        if (queriesPath == null) {
            return;
        }

        ClassLoader cl = getClass().getClassLoader();
        InputStream in = cl.getResourceAsStream(queriesPath);
        if (in == null) {
            throw new SQLRuntimeException("Can't find query resource: "
                    + queriesPath);
        }

        Properties prop = new Properties();
        try {
            prop.load(in);
        } catch (IOException ex) {
            throw new SQLRuntimeException("Can't load queries: " + queriesPath);
        }

        String productId = beantable_.getIdentity().getDatabaseProductId();
        String path;
        int dot = queriesPath.lastIndexOf('.');
        if (dot >= 0) {
            path = queriesPath.substring(0, dot) + "_" + productId
                    + queriesPath.substring(dot);
        } else {
            path = queriesPath + "_" + productId;
        }
        in = cl.getResourceAsStream(path);
        if (in != null) {
            prop = new Properties(prop);
            try {
                prop.load(in);
            } catch (IOException ex) {
                throw new SQLRuntimeException("Can't load queries: " + path);
            }
        }

        sql_ = prop;
    }

    public Beantable<T> getBeantable() {
        return beantable_;
    }

    public ResultSetHandler getBeantableHandler() {
        return beantableHandler_;
    }

    public ResultSetHandler getBeantableListHandler() {
        return beantableListHandler_;
    }
}
