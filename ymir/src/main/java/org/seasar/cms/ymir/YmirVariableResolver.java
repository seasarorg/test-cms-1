package org.seasar.cms.ymir;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.seasar.cms.ymir.impl.DefaultRequestProcessor;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.impl.VariableResolverImpl;

public class YmirVariableResolver extends VariableResolverImpl {
    private HttpServletRequest request_;

    private VariableResolver parent_;

    public YmirVariableResolver(HttpServletRequest request) {
        this(request, null);
    }

    public YmirVariableResolver(HttpServletRequest request,
            VariableResolver parent) {
        request_ = request;
        parent_ = parent;
    }

    public Object getVariable(TemplateContext context, String name) {
        if (super.containsVariable(name)) {
            return super.getVariable(context, name);
        } else if (parent_ != null) {
            Entry entry = parent_.getVariableEntry(context, name);
            if (entry != null) {
                return entry.getValue();
            }
        }

        Object value = request_.getParameter(name);
        if (value != null) {
            return value;
        }

        Object component = request_
                .getAttribute(DefaultRequestProcessor.ATTR_SELF);
        if (component != null) {
            try {
                return PropertyUtils.getProperty(component, name);
            } catch (Throwable ignore) {
            }
        }

        return null;
    }

    public String[] getVariableNames() {
        Set nameSet = new HashSet();
        nameSet.addAll(Arrays.asList(super.getVariableNames()));
        if (parent_ != null) {
            nameSet.addAll(Arrays.asList(parent_.getVariableNames()));
        }
        for (Enumeration enm = request_.getParameterNames(); enm
                .hasMoreElements();) {
            nameSet.add((String) enm.nextElement());
        }
        Object component = request_
                .getAttribute(DefaultRequestProcessor.ATTR_SELF);
        if (component != null) {
            PropertyDescriptor[] pds = PropertyUtils
                    .getPropertyDescriptors(component);
            for (int i = 0; i < pds.length; i++) {
                String name = pds[i].getName();
                if (pds[i].getReadMethod() != null) {
                    nameSet.add(name);
                }
            }
        }
        return (String[]) nameSet.toArray(new String[0]);
    }

    public boolean containsVariable(String name) {
        if (super.containsVariable(name)) {
            return true;
        } else if (parent_ != null && parent_.containsVariable(name)) {
            return true;
        } else if (request_.getParameter(name) != null) {
            return true;
        }

        Object component = request_
                .getAttribute(DefaultRequestProcessor.ATTR_SELF);
        if (component != null) {
            try {
                if (PropertyUtils.getPropertyDescriptor(component, name) != null) {
                    return true;
                }
            } catch (Throwable ignore) {
            }
        }

        return false;
    }

    public Entry getVariableEntry(TemplateContext context, String name) {
        Entry entry = super.getVariableEntry(context, name);
        if (entry != null) {
            return entry;
        } else if (parent_ != null) {
            entry = parent_.getVariableEntry(context, name);
            if (entry != null) {
                return entry;
            }
        }

        Object value = request_.getParameter(name);
        if (value != null) {
            return new EntryImpl(name, String.class, value);
        }

        Object component = request_
                .getAttribute(DefaultRequestProcessor.ATTR_SELF);
        if (component != null) {
            try {
                PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(
                        component, name);
                if (pd != null) {
                    Method readMethod = pd.getReadMethod();
                    if (readMethod != null) {
                        return new EntryImpl(name, readMethod.getReturnType(),
                                readMethod.invoke(component, new Object[0]));
                    }
                }
            } catch (Throwable ignore) {
            }
        }

        return null;
    }
}
