package org.seasar.cms.database.identity.impl;

/**
 * <p>
 * <b>同期化：</b> このクラスはスレッドセーフです。
 * </p>
 *
 * @author YOKOTA Takehiko
 */
public class PostgreIdentity extends AbstractIdentity {

    public String getDatabaseProductId() {

        return "postgre";
    }

    public boolean isMatched(String productName, String productVersion) {
        // XXX 実装しよう。
        return false;
    }

    public String toNumericExpression(String expr) {
        return "(to_number(" + expr + ",'9999999999')";
    }
}
