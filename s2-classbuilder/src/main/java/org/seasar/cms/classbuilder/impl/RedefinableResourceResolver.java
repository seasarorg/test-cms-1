package org.seasar.cms.classbuilder.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.classbuilder.util.S2ContainerBuilderUtils;
import org.seasar.framework.container.factory.ClassPathResourceResolver;
import org.seasar.framework.env.Env;
import org.seasar.framework.exception.IORuntimeException;
import org.seasar.framework.util.ResourceUtil;

public class RedefinableResourceResolver extends ClassPathResourceResolver {
    private static final char COLON = ':';

    public InputStream getInputStream(String path) {
        String[] paths = constructRedefinedDiconPaths(path);
        for (int i = 0; i < paths.length; i++) {
            try {
                InputStream is = super.getInputStream(paths[i]);
                if (is != null) {
                    return is;
                }
            } catch (IORuntimeException ignore) {
                // super.getInputStreamは、対応するリソースが見つからない場合は
                // nullを返すまたはIORuntimeExceptionをスローするので、
                // IORuntimeExceptionがスローされた場合は単にスキップする。
            }
        }
        return super.getInputStream(path);
    }

    /**
     * クラスパスから読み込み対象となるリソースを取得し、URLを構築します。 取得する際には、拡張子の手前に環境名をサフィックスを加えたパス(例
     * env.txt→env_ut.txt)を用います。 環境名を加えたパスのリソースが存在しない場合は、パスをそのまま用います。
     * 
     * @param path
     *            読み込み対象となるリソースのパス
     * @return 取得したリソースのURL
     * @see Env#adjustPath(String)
     */
    // 少なくともSeasar2.4.39の時点でパスに「:」を含む場合に挙動が仕様を満たさないため、ここで独自に対処を行なうようにしている。
    @Override
    protected URL getURL(String path) {
        String extPath = Env.adjustPath(path);
        URL url = toURL_corrected(extPath);
        if (url == null && !extPath.equals(path)) {
            url = toURL_corrected(path);
        }
        return url;
    }

    URL toURL_corrected(final String path) {
        if (path.indexOf(COLON) >= 0) {
            try {
                URL url = new URL(path);
                InputStream is = null;
                try {
                    is = url.openStream();
                    return url;
                } catch (IOException ex) {
                    // 開けなかった場合はそのリソースを使わないようにする。
                    return null;
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
            } catch (MalformedURLException ignore) {
                // URLの形式でない場合はクラスパス上のリソースとみなす。
            }
        }
        return ResourceUtil.getResourceNoException(path);
    }

    protected String[] constructRedefinedDiconPaths(String path) {
        int delimiter = path
                .lastIndexOf(RedefinableXmlS2ContainerBuilder.DELIMITER);
        int slash = path.lastIndexOf('/');
        if (delimiter >= 0 && delimiter > slash) {
            // リソース名に「+」が含まれていない場合だけ特別な処理を行なう。
            return new String[0];
        }

        List<String> pathList = new ArrayList<String>();
        String body;
        String suffix;
        int dot = path.lastIndexOf('.');
        if (dot < 0) {
            body = path;
            suffix = "";
        } else {
            body = path.substring(0, dot);
            suffix = path.substring(dot);
        }
        String resourceBody = S2ContainerBuilderUtils
                .fromURLToResourcePath(body
                        + RedefinableXmlS2ContainerBuilder.DELIMITER + suffix);
        if (resourceBody != null) {
            // パスがJarのURLの場合はURLをリソースパスに変換した上で作成したパスを候補に含める。
            pathList.add(resourceBody);
        }
        pathList
                .add(body + RedefinableXmlS2ContainerBuilder.DELIMITER + suffix);
        return pathList.toArray(new String[0]);
    }
}
