package org.seasar.framework.util;

import java.io.IOException;
import java.util.jar.JarFile;

import org.seasar.framework.exception.IORuntimeException;

/**
 * Seasar2.4のJarFileUtilにあってSeasar2.3のJarFileUtilにないメソッドを定義するクラスです。
 */
public class JarFile24Util {
    /**
     * Jarファイルをクローズします。
     * 
     * @param jarFile
     *            Jarファイル
     * @throws IORuntimeException
     *             入出力エラーが発生した場合にスローされます
     */
    public static void close(final JarFile jarFile) {
        try {
            jarFile.close();
        } catch (final IOException e) {
            throw new IORuntimeException(e);
        }
    }
}
