package org.seasar.cms.classbuilder.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.classbuilder.util.S2ContainerBuilderUtils;
import org.seasar.framework.container.factory.ClassPathResourceResolver;
import org.seasar.framework.exception.IORuntimeException;


public class RedefinableResourceResolver extends ClassPathResourceResolver
{
    public InputStream getInputStream(String path)
    {
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


    protected String[] constructRedefinedDiconPaths(String path)
    {
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
            .fromURLToResourcePath(body);
        if (resourceBody != null) {
            // パスがJarのURLの場合はURLをリソースパスに変換した上で作成したパスを候補に含める。
            pathList.add(resourceBody
                + RedefinableXmlS2ContainerBuilder.DELIMITER + suffix);
        }
        pathList
            .add(body + RedefinableXmlS2ContainerBuilder.DELIMITER + suffix);
        return pathList.toArray(new String[0]);
    }
}
