package org.seasar.cms.ymir.extension.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.ymir.AnnotationHandler;
import org.seasar.cms.ymir.AttributeHandler;
import org.seasar.cms.ymir.extension.annotation.In;
import org.seasar.cms.ymir.extension.annotation.Out;
import org.seasar.cms.ymir.scope.Scope;
import org.seasar.cms.ymir.scope.impl.HttpSessionScope;
import org.seasar.framework.container.S2Container;

public class TigerAnnotationHandler implements AnnotationHandler {

    private S2Container container_;

    public AttributeHandler[] getInjectedScopeAttributes(Object component) {
        return getInjectedScopeAttributes(component.getClass());
    }

    public AttributeHandler[] getOutjectedScopeAttributes(Object component) {
        return getOutjectedScopeAttributes(component.getClass());
    }

    AttributeHandler[] getInjectedScopeAttributes(Class clazz) {
        Method[] methods = clazz.getMethods();
        List<AttributeHandler> handlerList = new ArrayList<AttributeHandler>();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            In in = method.getAnnotation(In.class);
            if (in == null) {
                continue;
            }

            int modifiers = method.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                throw new RuntimeException(
                        "Logic error: @In can't annotate static method: class="
                                + clazz.getName() + ", method=" + method);
            } else if (!Modifier.isPublic(modifiers)) {
                throw new RuntimeException(
                        "Logic error: @In can annotate only public method: class="
                                + clazz.getName() + ", method=" + method);
            } else if (method.getParameterTypes().length != 1) {
                throw new RuntimeException(
                        "Logic error: @In can't annotate this method: class="
                                + clazz.getName() + ", method=" + method);
            }

            handlerList.add(new AttributeHandler(toAttributeName(method
                    .getName(), in.name()), getScope(in), method, null));
        }

        return handlerList.toArray(new AttributeHandler[0]);
    }

    Scope getScope(In in) {
        Object key;
        if (in.scopeName().length() > 0) {
            key = in.scopeName();
        } else if (in.scopeClass() != Object.class) {
            key = in.scopeClass();
        } else if (in.value() != Object.class) {
            key = in.value();
        } else {
            key = HttpSessionScope.class;
        }
        return (Scope) container_.getComponent(key);
    }

    AttributeHandler[] getOutjectedScopeAttributes(Class clazz) {
        Method[] methods = clazz.getMethods();
        List<AttributeHandler> handlerList = new ArrayList<AttributeHandler>();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            Out out = method.getAnnotation(Out.class);
            if (out == null) {
                continue;
            }

            int modifiers = method.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                throw new RuntimeException(
                        "Logic error: @Out can't annotate static method: class="
                                + clazz.getName() + ", method=" + method);
            } else if (!Modifier.isPublic(modifiers)) {
                throw new RuntimeException(
                        "Logic error: @Out can annotate only public method: class="
                                + clazz.getName() + ", method=" + method);
            } else if (method.getParameterTypes().length != 0
                    || method.getReturnType() == Void.TYPE) {
                throw new RuntimeException(
                        "Logic error: @Out can't annotate this method: class="
                                + clazz.getName() + ", method=" + method);
            }

            handlerList.add(new AttributeHandler(toAttributeName(method
                    .getName(), out.name()), getScope(out), null, method));
        }

        return handlerList.toArray(new AttributeHandler[0]);
    }

    Scope getScope(Out out) {
        Object key;
        if (out.scopeName().length() > 0) {
            key = out.scopeName();
        } else if (out.scopeClass() != Object.class) {
            key = out.scopeClass();
        } else if (out.value() != Object.class) {
            key = out.value();
        } else {
            key = HttpSessionScope.class;
        }
        return (Scope) container_.getComponent(key);
    }

    String toAttributeName(String implicitName, String explicitName) {
        if (explicitName.length() > 0) {
            return explicitName;
        } else {
            for (int i = 0; i < implicitName.length(); i++) {
                char ch = implicitName.charAt(i);
                if (Character.isUpperCase(ch)) {
                    return String.valueOf(Character.toLowerCase(ch))
                            + implicitName.substring(i + 1);
                }
            }
            return implicitName;
        }
    }

    public void setS2Container(S2Container container) {
        container_ = container;
    }
}
