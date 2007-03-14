package org.seasar.cms.ymir.extension.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.seasar.cms.ymir.AnnotationHandler;
import org.seasar.cms.ymir.ScopeHandler;
import org.seasar.cms.ymir.Constraint;
import org.seasar.cms.ymir.extension.ConstraintType;
import org.seasar.cms.ymir.extension.annotation.ConstraintAnnotation;
import org.seasar.cms.ymir.extension.annotation.In;
import org.seasar.cms.ymir.extension.annotation.Out;
import org.seasar.cms.ymir.extension.annotation.SuppressConstraints;
import org.seasar.cms.ymir.extension.constraint.ConstraintFactory;
import org.seasar.cms.ymir.scope.Scope;
import org.seasar.cms.ymir.scope.impl.RequestScope;
import org.seasar.framework.container.S2Container;

public class TigerAnnotationHandler implements AnnotationHandler {

    static final Set<ConstraintType> EMPTY_SUPPRESSTYPESET = EnumSet
            .noneOf(ConstraintType.class);

    private S2Container container_;

    public ScopeHandler[] getInjectedScopeAttributes(Object component) {
        return getInjectedScopeAttributes(component.getClass());
    }

    public ScopeHandler[] getOutjectedScopeAttributes(Object component) {
        return getOutjectedScopeAttributes(component.getClass());
    }

    ScopeHandler[] getInjectedScopeAttributes(Class clazz) {
        Method[] methods = clazz.getMethods();
        List<ScopeHandler> handlerList = new ArrayList<ScopeHandler>();
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

            handlerList.add(new ScopeHandler(toAttributeName(method
                    .getName(), in.name()), getScope(in), method, null));
        }

        return handlerList.toArray(new ScopeHandler[0]);
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
            key = RequestScope.class;
        }
        return (Scope) container_.getComponent(key);
    }

    ScopeHandler[] getOutjectedScopeAttributes(Class clazz) {
        Method[] methods = clazz.getMethods();
        List<ScopeHandler> handlerList = new ArrayList<ScopeHandler>();
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

            handlerList.add(new ScopeHandler(toAttributeName(method
                    .getName(), out.name()), getScope(out), null, method));
        }

        return handlerList.toArray(new ScopeHandler[0]);
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
            key = RequestScope.class;
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

    public Constraint[] getConstraints(Object component, Method action,
            boolean includeCommonConstraints) {
        return getConstraints(component.getClass(), action,
                includeCommonConstraints);
    }

    // PropertyDescriptorのreadMethodは対象外。fieldも対象外。
    Constraint[] getConstraints(Class<?> clazz, Method action,
            boolean includeCommonConstraints) {
        List<Constraint> list = new ArrayList<Constraint>();

        if (includeCommonConstraints) {
            Set<ConstraintType> suppressTypeSet = EnumSet
                    .noneOf(ConstraintType.class);
            if (action != null) {
                SuppressConstraints suppress = action
                        .getAnnotation(SuppressConstraints.class);
                if (suppress != null) {
                    ConstraintType[] types = suppress.value();
                    for (int i = 0; i < types.length; i++) {
                        suppressTypeSet.add(types[i]);
                    }
                }
            }
            getConstraint(clazz, list, suppressTypeSet);
            BeanInfo beanInfo;
            try {
                beanInfo = Introspector.getBeanInfo(clazz);
            } catch (IntrospectionException ex) {
                throw new RuntimeException(ex);
            }
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < pds.length; i++) {
                getConstraint(pds[i].getWriteMethod(), list, suppressTypeSet);
            }
        }
        getConstraint(action, list, EMPTY_SUPPRESSTYPESET);

        return list.toArray(new Constraint[0]);
    }

    @SuppressWarnings("unchecked")
    void getConstraint(AnnotatedElement element, List<Constraint> list,
            Set<ConstraintType> suppressTypeSet) {
        if (element == null) {
            return;
        }
        Annotation[] annotations = element.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            ConstraintAnnotation constraintAnnotation = annotations[i]
                    .annotationType().getAnnotation(ConstraintAnnotation.class);
            if (constraintAnnotation == null
                    || suppressTypeSet.contains(constraintAnnotation.type())) {
                continue;
            }
            ConstraintFactory factory;
            try {
                factory = (ConstraintFactory) constraintAnnotation.factory()
                        .newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
            list.add(factory.getConstraint(annotations[i], element));
        }
    }

    Method getMethod(Class clazz, String name) {
        try {
            return clazz.getMethod(name, new Class[0]);
        } catch (SecurityException ex) {
            return null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }
}
