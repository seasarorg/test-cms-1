package org.seasar.cms.pluggable.dao;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.seasar.dao.AnnotationReaderFactory;
import org.seasar.dao.DaoMetaData;
import org.seasar.dao.DaoMetaDataFactory;
import org.seasar.dao.ValueTypeFactory;
import org.seasar.dao.impl.DaoMetaDataImpl;
import org.seasar.extension.jdbc.ResultSetFactory;
import org.seasar.extension.jdbc.StatementFactory;

public class OndemandDaoMetaDataFactory implements DaoMetaDataFactory {

    protected Map daoMetaDataCache_ = new HashMap();

    protected DataSource dataSource_;

    protected StatementFactory statementFactory_;

    protected ResultSetFactory resultSetFactory_;

    protected AnnotationReaderFactory readerFactory_;

    private ValueTypeFactory valueTypeFactory;

    private String sqlFileEncoding;

    private String[] daoSuffixes;

    private String[] insertPrefixes;

    private String[] deletePrefixes;

    private String[] updatePrefixes;

    public OndemandDaoMetaDataFactory(DataSource dataSource,
            StatementFactory statementFactory,
            ResultSetFactory resultSetFactory,
            AnnotationReaderFactory readerFactory) {

        dataSource_ = dataSource;
        statementFactory_ = statementFactory;
        resultSetFactory_ = resultSetFactory;
        readerFactory_ = readerFactory;
    }

    public void setSqlFileEncoding(String encoding) {
        this.sqlFileEncoding = encoding;
    }

    public void setDaoSuffixes(String[] suffixes) {
        this.daoSuffixes = suffixes;
    }

    public void setInsertPrefixes(String[] prefixes) {
        this.insertPrefixes = prefixes;
    }

    public void setDeletePrefixes(String[] prefixes) {
        this.deletePrefixes = prefixes;
    }

    public void setUpdatePrefixes(String[] prefixes) {
        this.updatePrefixes = prefixes;
    }

    public synchronized DaoMetaData getDaoMetaData(Class daoClass) {
        Object key = daoClass;
        DaoMetaData dmd = (DaoMetaData) daoMetaDataCache_.get(key);
        if (dmd != null) {
            return dmd;
        }
        DaoMetaData dmdi = createDaoMetaData(daoClass);
        daoMetaDataCache_.put(key, dmdi);
        return dmdi;
    }

    private DaoMetaData createDaoMetaData(Class daoClass) {
        DaoMetaDataImpl daoMetaData = new DaoMetaDataImpl();
        daoMetaData.setDaoClass(daoClass);
        daoMetaData.setDataSource(dataSource_);
        daoMetaData.setStatementFactory(statementFactory_);
        daoMetaData.setResultSetFactory(resultSetFactory_);
        daoMetaData.setAnnotationReaderFactory(readerFactory_);
        daoMetaData.setValueTypeFactory(valueTypeFactory);
        if (sqlFileEncoding != null) {
            daoMetaData.setSqlFileEncoding(sqlFileEncoding);
        }
        if (daoSuffixes != null) {
            daoMetaData.setDaoSuffixes(daoSuffixes);
        }
        if (insertPrefixes != null) {
            daoMetaData.setInsertPrefixes(insertPrefixes);
        }
        if (updatePrefixes != null) {
            daoMetaData.setUpdatePrefixes(updatePrefixes);
        }
        if (deletePrefixes != null) {
            daoMetaData.setDeletePrefixes(deletePrefixes);
        }
        daoMetaData.initialize();
        return daoMetaData;
    }

    public void setValueTypeFactory(ValueTypeFactory valueTypeFactory) {
        this.valueTypeFactory = valueTypeFactory;
    }
}
