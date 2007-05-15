package org.seasar.cms.database.identity.impl;

/**
 * <p>
 * <b>同期化：</b> このクラスはスレッドセーフです。
 * </p>
 *
 * @author YOKOTA Takehiko
 */
public class VoidIdentity extends AbstractIdentity {

    public String getDatabaseProductId() {
        return "void";
    }

    public boolean isMatched(String productName, String productVersion) {
        if ("void".equals(productName)) {
            return true;
        }

        return false;
    }
}
