package org.seasar.framework.util;

/**
 * Seasar2.4のStringUtilにあってSeasar2.3のStringUtilにないメソッドを定義するクラスです。
 */
public class String24Util {
    /**
     * サフィックスを削ります。
     * 
     * @param text
     *            テキスト
     * @param suffix
     *            サフィックス
     * @return 結果の文字列
     */
    public static final String trimSuffix(final String text, String suffix) {
        if (text == null) {
            return null;
        }
        if (suffix == null) {
            return text;
        }
        if (text.endsWith(suffix)) {
            return text.substring(0, text.length() - suffix.length());
        }
        return text;
    }
}
