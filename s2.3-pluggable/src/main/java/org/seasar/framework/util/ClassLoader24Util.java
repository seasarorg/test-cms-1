package org.seasar.framework.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

import org.seasar.framework.exception.IORuntimeException;

/**
 * Seasar2.4のClassLoaderUtilにあってSeasar2.3のClassLoaderUtilにないメソッドを定義するクラスです。
 */
public class ClassLoader24Util {
    /**
     * {@link #getClassLoader(Class)}が返すクラスローダから指定された名前を持つすべてのリソースを探します。
     * 
     * @param targetClass
     *            ターゲット・クラス
     * @param name
     *            リソース名
     * @return リソースに対する URL
     *         オブジェクトの列挙。リソースが見つからなかった場合、列挙は空になる。クラスローダがアクセスを持たないリソースは列挙に入らない
     * @see java.lang.ClassLoader#getResources(String)
     */
    @SuppressWarnings("unchecked")
    public static Iterator getResources(final Class targetClass,
            final String name) {
        return getResources(ClassLoaderUtil.getClassLoader(targetClass), name);
    }

    /**
     * 指定のクラスローダから指定された名前を持つすべてのリソースを探します。
     * 
     * @param loader
     *            クラスローダ
     * @param name
     *            リソース名
     * @return リソースに対する URL
     *         オブジェクトの列挙。リソースが見つからなかった場合、列挙は空になる。クラスローダがアクセスを持たないリソースは列挙に入らない
     * @see java.lang.ClassLoader#getResources(String)
     */
    @SuppressWarnings("unchecked")
    public static Iterator getResources(final ClassLoader loader,
            final String name) {
        try {
            final Enumeration e = loader.getResources(name);
            return new EnumerationIterator(e);
        } catch (final IOException e) {
            throw new IORuntimeException(e);
        }
    }
}
