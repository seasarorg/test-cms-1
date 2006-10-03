package org.seasar.cms.ymir;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.seasar.cms.ymir.impl.DefaultRequestProcessor;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.VariableResolverUtils;
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
        Object value = super.getVariable(context, name);
        if (value != null) {
            return value;
        }
        if (parent_ != null) {
            value = parent_.getVariable(context, name);
            if (value != null) {
                return value;
            }
        }

        value = request_.getParameter(name);
        if (value == null) {
            Object component = request_
                    .getAttribute(DefaultRequestProcessor.ATTR_SELF);
            if (component != null) {
                try {
                    value = PropertyUtils.getProperty(component, name);
                } catch (Throwable ignore) {
                }
            }
        }
        return value;
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

    public Class getVariableType(TemplateContext context, String name) {
        return VariableResolverUtils.toType(getVariable(context, name));
    }
}
