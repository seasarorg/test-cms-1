package org.seasar.cms.database.identity.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.seasar.cms.database.identity.Identity;
import org.seasar.cms.database.identity.IdentitySelector;

public class IdentitySelectorImpl implements IdentitySelector {

    private DataSource ds_;

    private Identity[] identities_;

    private Identity identity_;

    private boolean started_;

    public void start() {

        if (started_) {
            return;
        }

        String productName;
        String productVersion;
        Connection con = null;
        try {
            con = ds_.getConnection();
            DatabaseMetaData metaData = con.getMetaData();
            productName = metaData.getDatabaseProductName();
            productVersion = metaData.getDatabaseProductVersion();
        } catch (SQLException ex) {
            throw new RuntimeException("Can't get database metadata", ex);
        } finally {
            DbUtils.closeQuietly(con);
        }

        for (int i = 0; i < identities_.length; i++) {
            if (identities_[i].isMatched(productName, productVersion)) {
                identity_ = identities_[i];
                break;
            }
        }
        if (identity_ == null) {
            throw new RuntimeException("Unsupported databse product: name="
                + productName + ", version=" + productVersion);
        }

        identity_.startUsingDatabase();

        started_ = true;
    }

    public void stop() {

        if (!started_) {
            return;
        }

        if (identity_ != null) {
            identity_.stopUsingDatabase();
        }

        started_ = false;
    }

    public Identity getIdentity() {
        return identity_;
    }

    public void setDataSource(DataSource ds) {
        ds_ = ds;
    }

    public void setIdentities(Identity[] identities) {
        identities_ = identities;
    }
}
