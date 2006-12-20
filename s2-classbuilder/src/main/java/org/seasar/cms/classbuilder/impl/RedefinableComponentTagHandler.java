package org.seasar.cms.classbuilder.impl;

import java.io.IOException;
import java.io.InputStream;

import org.seasar.framework.container.ArgDef;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.deployer.InstanceDefFactory;
import org.seasar.framework.container.factory.AnnotationHandler;
import org.seasar.framework.container.factory.AnnotationHandlerFactory;
import org.seasar.framework.container.factory.ComponentTagHandler;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.factory.TagAttributeNotDefinedRuntimeException;
import org.seasar.framework.util.StringUtil;
import org.seasar.framework.xml.TagHandlerContext;


public class RedefinableComponentTagHandler extends ComponentTagHandler
{
    private static final long serialVersionUID = 2513809305883784501L;

    private static final String DELIMITER = "+";


    public void end(TagHandlerContext context, String body)
    {
        ComponentDef componentDef = (ComponentDef)context.pop();
        AnnotationHandler annoHandler = AnnotationHandlerFactory
            .getAnnotationHandler();
        annoHandler.appendInitMethod(componentDef);
        annoHandler.appendDestroyMethod(componentDef);
        String expression = null;
        if (body != null) {
            expression = body.trim();
            if (!StringUtil.isEmpty(expression)) {
                componentDef
                    .setExpression(createExpression(context, expression));
            } else {
                expression = null;
            }
        }
        if (componentDef.getComponentClass() == null
            && !InstanceDefFactory.OUTER.equals(componentDef.getInstanceDef())
            && expression == null) {
            throw new TagAttributeNotDefinedRuntimeException("component",
                "class");
        }
        if (context.peek() instanceof S2Container) {
            if (componentDef.getComponentName() != null) {
                componentDef = redefine(componentDef, (String)context
                    .getParameter("path"),
                    (RedefinableXmlS2ContainerBuilder)context
                        .getParameter("builder"));
            }
            S2Container container = (S2Container)context.peek();
            container.register(componentDef);
        } else {
            ArgDef argDef = (ArgDef)context.peek();
            argDef.setChildComponentDef(componentDef);
        }
    }


    ComponentDef redefine(ComponentDef componentDef, String path,
        RedefinableXmlS2ContainerBuilder builder)
    {
        if (path.indexOf(DELIMITER) >= 0) {
            // 高速化のため。
            return componentDef;
        }

        String name = componentDef.getComponentName();
        String diconPath = constructRedifinitionDiconPath(path, name);
        if (!resourceExists(diconPath, builder)) {
            return componentDef;
        }

        S2Container container = S2ContainerFactory.create(diconPath);
        if (!container.hasComponentDef(name)) {
            throw new RuntimeException(
                "Can't find component definition named '" + name + "' in "
                    + diconPath);
        }
        ComponentDef redefinition = container.getComponentDef(name);
        redefinition.setContainer(componentDef.getContainer());

        return redefinition;
    }


    boolean resourceExists(String path, RedefinableXmlS2ContainerBuilder builder)
    {
        InputStream is = builder.getResourceResolver().getInputStream(path);
        if (is == null) {
            return false;
        } else {
            try {
                is.close();
            } catch (IOException ignore) {
            }
            return true;
        }
    }


    protected String constructRedifinitionDiconPath(String path, String name)
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
        return body + DELIMITER + name + suffix;
    }
}
