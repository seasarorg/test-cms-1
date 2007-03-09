package org.seasar.cms.ymir.zpt;

import java.text.MessageFormat;
import java.util.Locale;

import org.seasar.cms.ymir.Globals;
import org.seasar.cms.ymir.MessageResourceNotFoundRuntimeException;
import org.seasar.cms.ymir.MessageResources;
import org.seasar.cms.ymir.MessageResourcesNotFoundRuntimeException;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Ymir;
import org.seasar.cms.ymir.YmirContext;
import org.seasar.cms.ymir.YmirVariableResolver;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.kvasir.util.collection.AttributeReader;
import org.seasar.kvasir.util.collection.I18NPropertyReader;
import org.seasar.kvasir.util.collection.PropertyReader;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.render.Note;
import net.skirnir.freyja.zpt.tales.PathResolver;

public class YmirPathResolver implements PathResolver {
    private static final String NAME_VALUE = "%value";

    private static final String PROPERTYPREFIX_LABEL = "label.";

    public boolean accept(TemplateContext context,
            VariableResolver varResolver, Object obj, String child) {
        return (obj instanceof Note || obj instanceof I18NPropertyReader
                || obj instanceof PropertyReader || obj instanceof AttributeReader);
    }

    public Object resolve(TemplateContext context,
            VariableResolver varResolver, Object obj, String child) {
        if (obj instanceof Note) {
            Note note = (Note) obj;
            if (child.equals(NAME_VALUE)) {
                return getMessageResourceValue(context, varResolver, note);
            }
        } else if (obj instanceof I18NPropertyReader) {
            I18NPropertyReader reader = (I18NPropertyReader) obj;
            if (child.startsWith("%")) {
                return reader.getProperty(child.substring(1), findLocale(
                        context, varResolver));
            } else {
                return reader.getProperty(child);
            }
        } else if (obj instanceof PropertyReader) {
            return ((PropertyReader) obj).getProperty(child);
        } else if (obj instanceof AttributeReader) {
            return ((AttributeReader) obj).getAttribute(child);
        }
        return null;
    }

    String getMessageResourceValue(TemplateContext context,
            VariableResolver varResolver, Note note) {
        String value = note.getValue();
        if (value == null) {
            return null;
        }

        String resourcesName;
        int slash = value.indexOf('/');
        if (slash >= 0) {
            resourcesName = value.substring(0, slash);
            value = value.substring(slash + 1);
        } else {
            resourcesName = null;
        }

        MessageResources resources;
        try {
            resources = findMessageResources(resourcesName);
        } catch (ClassCastException ex) {
            throw new RuntimeException(
                    "Not MessageResources Object: message resources' name may be incorrect: key="
                            + value + ", message resources' name="
                            + resourcesName, ex);
        } catch (ComponentNotFoundRuntimeException ex) {
            throw new MessageResourcesNotFoundRuntimeException(
                    "MessageResources object not found: message resources' name may be incorrect: key="
                            + value
                            + ", message resources' name="
                            + resourcesName, ex)
                    .setMessageResourcesName(resourcesName);
        }

        if (resources != null) {
            Locale locale = findLocale(context, varResolver);
            String v = resources.getProperty(value, locale);
            if (v != null) {
                Object[] parameters = note.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i] instanceof String) {
                        String localizedValue = resources.getProperty(
                                PROPERTYPREFIX_LABEL + parameters[i], locale);
                        if (localizedValue != null) {
                            parameters[i] = localizedValue;
                        }
                    }
                }
                value = MessageFormat.format(v, note.getParameters());
            } else {
                StringBuffer sb = new StringBuffer();
                sb.append("Message corresponding key ('").append(value).append(
                        "') does not exist in ");
                if (resourcesName != null) {
                    sb.append("MessageResources ('").append(resourcesName)
                            .append("')");
                } else {
                    sb.append("default MessageResources (").append(
                            Globals.MESSAGE_RESOURCES).append(")");
                }
                throw new MessageResourceNotFoundRuntimeException(sb.toString())
                        .setMessageResourcesName(resourcesName)
                        .setMessageResourceKey(value).setLocale(locale);
            }
        }

        return value;
    }

    MessageResources findMessageResources(String resourcesName) {
        Ymir ymir = YmirContext.getYmir();
        if (ymir == null) {
            return null;
        }

        S2Container container = ymir.getApplication().getS2Container();
        if (resourcesName != null) {
            return (MessageResources) container.getComponent(resourcesName);
        } else {
            return (MessageResources) container
                    .getComponent(MessageResources.class);
        }
    }

    Locale findLocale(TemplateContext context, VariableResolver varResolver) {
        return ((Request) varResolver.getVariable(context,
                YmirVariableResolver.NAME_YMIRREQUEST)).getLocale();
    }
}
