package org.seasar.cms.classbuilder.impl;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.classbuilder.util.S2ContainerBuilderUtils;
import org.seasar.framework.container.factory.ResourceResolver;
import org.seasar.framework.exception.IORuntimeException;
import org.seasar.framework.util.ResourceUtil;
import org.seasar.framework.util.URLUtil;

public class RedefinableResourceResolver implements ResourceResolver {
    private static final char COLON = ':';

    public InputStream getInputStream(String path) {
        String[] paths = constructRedefinedDiconPaths(path);
        for (int i = 0; i < paths.length; i++) {
            try {
                InputStream is = getInputStream0(paths[i]);
                if (is != null) {
                    return is;
                }
            } catch (IORuntimeException ignore) {
                // super.getInputStreamは、対応するリソースが見つからない場合は
                // nullを返すまたはIORuntimeExceptionをスローするので、
                // IORuntimeExceptionがスローされた場合は単にスキップする。
            }
        }
        return getInputStream0(path);
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

    protected InputStream getInputStream0(final String path) {
        URL url = getURL(path);
        if (url == null) {
            return null;
        }
        return URLUtil.openStream(url);
    }

    protected URL getURL(final String path) {
        if (path.indexOf(COLON) >= 0) {
            try {
                return new URL(path);
            } catch (MalformedURLException ignore) {
            }
        }
        return ResourceUtil.getResourceNoException(path);
    }
}
