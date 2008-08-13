package org.seasar.framework.util;

/**
 * Seasar2.4のClassUtilにあってSeasar2.3のClassUtilにないメソッドを定義するクラスです。
 */
public class Class24Util {
    /**
     * FQCNからパッケージ名を除いた名前を返します。
     * 
     * @param clazz
     * @return FQCNからパッケージ名を除いた名前
     * @see #getShortClassName(String)
     */
    public static String getShortClassName(Class<?> clazz) {
        return getShortClassName(clazz.getName());
    }

    /**
     * FQCNからパッケージ名を除いた名前を返します。
     * 
     * @param className
     * @return FQCNからパッケージ名を除いた名前
     */
    public static String getShortClassName(String className) {
        int i = className.lastIndexOf('.');
        if (i > 0) {
            return className.substring(i + 1);
        }
        return className;
    }

    /**
     * クラス名をリソースパスとして表現します。
     * 
     * @param clazz
     * @return リソースパス
     * @see #getResourcePath(String)
     */
    public static String getResourcePath(Class<?> clazz) {
        return getResourcePath(clazz.getName());
    }

    /**
     * クラス名をリソースパスとして表現します。
     * 
     * @param className
     * @return リソースパス
     */
    public static String getResourcePath(String className) {
        return StringUtil.replace(className, ".", "/") + ".class";
    }
}
