package org.seasar.cms.classbuilder.impl;

import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.classbuilder.util.S2ContainerBuilderUtils;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.factory.XmlS2ContainerBuilder;
import org.seasar.framework.xml.SaxHandlerParser;


public class RedefinableXmlS2ContainerBuilder extends XmlS2ContainerBuilder
{
    public static final String DELIMITER = "+";

    private static final String NAME_ADDITIONAL = "+";


    public RedefinableXmlS2ContainerBuilder()
    {
        getRule().addTagHandler("component",
            new RedefinableComponentTagHandler());
    }


    @Override
    protected SaxHandlerParser createSaxHandlerParser(S2Container parent,
        String path)
    {
        SaxHandlerParser parser = super.createSaxHandlerParser(parent, path);
        parser.getSaxHandler().getTagHandlerContext().addParameter("builder",
            this);
        return parser;
    }


    @Override
    protected S2Container parse(S2Container parent, String path)
    {
        S2Container container = super.parse(parent, path);

        String[] additionalDiconPaths = constructAdditionalDiconPaths(path);
        for (int i = 0; i < additionalDiconPaths.length; i++) {
            if (S2ContainerBuilderUtils.resourceExists(additionalDiconPaths[i],
                this)) {
                S2ContainerBuilderUtils.mergeContainer(container,
                    S2ContainerFactory.create(additionalDiconPaths[i]));
                break;
            }
        }

        return container;
    }


    protected String[] constructAdditionalDiconPaths(String path)
    {
        int delimiter = path.lastIndexOf(DELIMITER);
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
            .fromJarURLToResourcePath(body);
        if (resourceBody != null) {
            // パスがJarのURLの場合はURLをリソースパスに変換した上で作成したパスを候補に含める。
            pathList.add(resourceBody + DELIMITER + NAME_ADDITIONAL + suffix);
        }
        pathList.add(body + DELIMITER + NAME_ADDITIONAL + suffix);
        return pathList.toArray(new String[0]);
    }
}
