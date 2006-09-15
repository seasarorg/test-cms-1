/**
 * 
 */
package org.seasar.cms.ymir.response.scheme;

import org.seasar.cms.ymir.Response;

public interface Strategy {

    /**
     * このStrategyが処理する対象のスキーム文字列を返します。
     * 
     * @return 処理対象のスキーム文字列。
     */
    String getScheme();

    /**
     * 指定されたパス文字列とコンポーネントからResponseを構築して返します。
     * 
     * @param path パス文字列。パス文字列とは、通常アクションメソッドの返り値からスキーム文字列を除去したものです。
     * @param component コンポーネント。nullであることがあります。
     * @return 構築したResponseオブジェクト。
     */
    Response constructResponse(String path, Object component);
}