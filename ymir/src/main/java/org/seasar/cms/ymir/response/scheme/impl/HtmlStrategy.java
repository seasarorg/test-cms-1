package org.seasar.cms.ymir.response.scheme.impl;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.response.SelfContainedResponse;
import org.seasar.cms.ymir.response.scheme.Strategy;

/**
 * このクラスは後方互換性のために存在します。通常は代わりにContentStrategyを利用すべきです。
 * 具体的には、「<code>html:XXX</code>」と書く代わりに「<code>content:XXX</code>」
 * もしくは「<code>content:text/html; charset=CCC:XXX</code>」と書くべきです。
 * 
 * @author YOKOTA Takehiko
 */
public class HtmlStrategy implements Strategy {

    public static final String SCHEME = "html";

    public String getScheme() {

        return SCHEME;
    }

    public Response constructResponse(String path, Object component) {

        return new SelfContainedResponse(path);
    }
}
