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
import org.seasar.cms.ymir.util.TokenUtils;
import org.seasar.framework.container.S2Container;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.impl.VariableResolverImpl;

public class YmirVariableResolver extends VariableResolverImpl {
    public static final String NAME_YMIRREQUEST = "ymirRequest";

    public static final String NAME_CONTAINER = "container";

    public static final String NAME_MESSAGES = "messages";

    public static final String NAME_TOKEN = "token";

    private Request ymirRequest_;

    private HttpServletRequest request_;

    private S2Container container_;

    private VariableResolver parent_;

    private Messages messages_;

    private Token token_;

    public YmirVariableResolver(Request ymirRequest,
            HttpServletRequest request, S2Container container) {
        this(ymirRequest, request, container, null);
    }

    public YmirVariableResolver(Request ymirRequest,
            HttpServletRequest request, S2Container container,
            VariableResolver parent) {
        ymirRequest_ = ymirRequest;
        request_ = request;
        container_ = container;
        messages_ = (Messages) container.getComponent(Globals.NAME_MESSAGES);
        parent_ = parent;
    }

    public Object getVariable(TemplateContext context, String name) {
        if (NAME_YMIRREQUEST.equals(name)) {
            return ymirRequest_;
        } else if (NAME_CONTAINER.equals(name)) {
            return container_;
        } else if (NAME_MESSAGES.equals(name)) {
            return messages_;
        } else if (NAME_TOKEN.equals(name)) {
            return getToken();
        } else if (super.containsVariable(name)) {
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
        nameSet.add(NAME_YMIRREQUEST);
        nameSet.add(NAME_CONTAINER);
        nameSet.add(NAME_MESSAGES);
        nameSet.add(NAME_TOKEN);
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
        if (NAME_YMIRREQUEST.equals(name) || NAME_CONTAINER.equals(name)
                || NAME_MESSAGES.equals(name) || NAME_TOKEN.equals(name)) {
            return true;
        } else if (super.containsVariable(name)) {
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
        if (NAME_YMIRREQUEST.equals(name)) {
            return new EntryImpl(name, Request.class, ymirRequest_);
        } else if (NAME_CONTAINER.equals(name)) {
            return new EntryImpl(name, S2Container.class, container_);
        } else if (NAME_MESSAGES.equals(name)) {
            return new EntryImpl(name, Messages.class, messages_);
        } else if (NAME_TOKEN.equals(name)) {
            return new TokenEntry(name);
        }

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

    Token getToken() {
        if (token_ == null) {
            TokenUtils.saveToken(request_, false);
            token_ = new Token(TokenUtils.getToken(request_));
        }
        return token_;
    }

    class TokenEntry implements Entry {
        private String name_;

        public TokenEntry(String name) {
            name_ = name;
        }

        public String getName() {
            return name_;
        }

        public Class getType() {
            return Token.class;
        }

        public Object getValue() {
            return getToken();
        }
    }
}
