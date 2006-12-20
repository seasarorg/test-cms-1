package org.seasar.cms.classbuilder.impl;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.XmlS2ContainerBuilder;
import org.seasar.framework.xml.SaxHandlerParser;


public class RedefinableXmlS2ContainerBuilder extends XmlS2ContainerBuilder
{
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
}
