package org.seasar.framework.util;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.seasar.framework.exception.SRuntimeException;

/**
 * Seasar2.4のURLUtilにあってSeasar2.3のURLUtilにないメソッドを定義するクラスです。
 */
@SuppressWarnings("unchecked")
public class URL24Util {
    /** プロトコルを正規化するためのマップ */
    protected static final Map CANONICAL_PROTOCOLS = new HashMap();
    static {
        CANONICAL_PROTOCOLS.put("wsjar", "jar"); // WebSphereがJarファイルのために使用する固有のプロトコル
    }

    /**
     * プロトコルを正規化して返します。
     * 
     * @param protocol
     *            プロトコル
     * @return 正規化されたプロトコル
     */
    public static String toCanonicalProtocol(final String protocol) {
        final String canonicalProtocol = (String) CANONICAL_PROTOCOLS
                .get(protocol);
        if (canonicalProtocol != null) {
            return canonicalProtocol;
        }
        return protocol;
    }

    /**
     * URLが示すJarファイルの{@link File}オブジェクトを返します。
     * 
     * @param fileUrl
     *            JarファイルのURL
     * @return Jarファイルの{@link File}
     */
    public static File toFile(final URL fileUrl) {
        try {
            final String path = URLDecoder.decode(fileUrl.getPath(), "UTF-8");
            return new File(path).getAbsoluteFile();
        } catch (final Exception e) {
            throw new SRuntimeException("ESSR0091", new Object[] { fileUrl }, e);
        }
    }
}
