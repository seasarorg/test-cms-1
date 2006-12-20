package org.seasar.cms.classbuilder.impl;

import org.seasar.cms.classbuilder.util.S2ContainerBuilderUtils;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.factory.XmlS2ContainerBuilder;
import org.seasar.framework.xml.SaxHandlerParser;


public class RedefinableXmlS2ContainerBuilder extends XmlS2ContainerBuilder
{
    public static final String DELIMITER = "+";


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

        String additionalDiconPath = constructAdditionalDiconPath(path);
        if (S2ContainerBuilderUtils.resourceExists(additionalDiconPath, this)) {
            S2ContainerBuilderUtils.mergeContainer(container,
                S2ContainerFactory.create(additionalDiconPath));
        }

        return container;
    }


    protected String constructAdditionalDiconPath(String path)
    {
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
        return body + DELIMITER + suffix;
    }
}
