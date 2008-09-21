package org.seasar.cms.classbuilder.impl;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.ComponentsTagHandler;
import org.seasar.framework.xml.TagHandlerContext;
import org.xml.sax.Attributes;

public class RedefinableComponentsTagHandler extends ComponentsTagHandler {
    private static final long serialVersionUID = 1L;

    @Override
    public void start(TagHandlerContext context, Attributes attributes) {
        super.start(context, attributes);

        RedefinableXmlS2ContainerBuilder builder = (RedefinableXmlS2ContainerBuilder) context
                .getParameter(RedefinableXmlS2ContainerBuilder.PARAMETER_BUILDER);
        S2Container container = (S2Container) context.peek();
        String path = (String) context.getParameter("path");

        builder.mergeContainers(container, path, false);
    }
}
