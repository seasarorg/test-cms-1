package org.seasar.cms.ymir.zpt;

import java.text.MessageFormat;
import java.util.Locale;

import org.seasar.cms.ymir.Globals;
import org.seasar.cms.ymir.MessageNotFoundRuntimeException;
import org.seasar.cms.ymir.Messages;
import org.seasar.cms.ymir.MessagesNotFoundRuntimeException;
import org.seasar.cms.ymir.Note;
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
import net.skirnir.freyja.zpt.tales.PathResolver;

public class YmirPathResolver implements PathResolver {
    private static final String NAME_VALUE = "%value";

    private static final String PROPERTYPREFIX_LABEL = "label.";

    public boolean accept(TemplateContext context,
            VariableResolver varResolver, Object obj, String child) {
        return (obj instanceof Note
                || obj instanceof net.skirnir.freyja.render.Note
                || obj instanceof I18NPropertyReader
                || obj instanceof PropertyReader || obj instanceof AttributeReader);
    }

    public Object resolve(TemplateContext context,
            VariableResolver varResolver, Object obj, String child) {
        if (obj instanceof Note) {
            Note note = (Note) obj;
            if (child.equals(NAME_VALUE)) {
                return getMessageResourceValue(context, varResolver, note
                        .getValue(), note.getParameters());
            }
        } else if (obj instanceof net.skirnir.freyja.render.Note) {
            net.skirnir.freyja.render.Note note = (net.skirnir.freyja.render.Note) obj;
            if (child.equals(NAME_VALUE)) {
                return getMessageResourceValue(context, varResolver, note
                        .getValue(), note.getParameters());
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
            VariableResolver varResolver, String noteValue,
            Object[] noteParameters) {
        if (noteValue == null) {
            return null;
        }

        String messagesName;
        int slash = noteValue.indexOf('/');
        if (slash >= 0) {
            messagesName = noteValue.substring(0, slash);
            noteValue = noteValue.substring(slash + 1);
        } else {
            messagesName = null;
        }

        Messages messages;
        try {
            messages = findMessages(messagesName);
        } catch (ClassCastException ex) {
            throw new RuntimeException(
                    "Not Messages Object: messages' name may be incorrect: key="
                            + noteValue + ", messages' name=" + messagesName,
                    ex);
        } catch (ComponentNotFoundRuntimeException ex) {
            throw new MessagesNotFoundRuntimeException(
                    "Messages object not found: messages' name may be incorrect: key="
                            + noteValue + ", messages' name=" + messagesName,
                    ex).setMessagesName(messagesName);
        }

        if (messages != null) {
            Locale locale = findLocale(context, varResolver);
            String v = messages.getProperty(noteValue, locale);
            if (v != null) {
                for (int i = 0; i < noteParameters.length; i++) {
                    if (noteParameters[i] instanceof String) {
                        String localizedValue = messages.getProperty(
                                PROPERTYPREFIX_LABEL + noteParameters[i],
                                locale);
                        if (localizedValue != null) {
                            noteParameters[i] = localizedValue;
                        }
                    }
                }
                noteValue = MessageFormat.format(v, noteParameters);
            } else {
                StringBuffer sb = new StringBuffer();
                sb.append("Message corresponding key ('").append(noteValue)
                        .append("') does not exist in ");
                if (messagesName != null) {
                    sb.append("Messages ('").append(messagesName).append("')");
                } else {
                    sb.append("default Messages (").append(Globals.MESSAGES)
                            .append(")");
                }
                throw new MessageNotFoundRuntimeException(sb.toString())
                        .setMessagesName(messagesName).setMessageKey(noteValue)
                        .setLocale(locale);
            }
        }

        return noteValue;
    }

    Messages findMessages(String messagesName) {
        Ymir ymir = YmirContext.getYmir();
        if (ymir == null) {
            return null;
        }

        S2Container container = ymir.getApplication().getS2Container();
        if (messagesName != null) {
            return (Messages) container.getComponent(messagesName);
        } else {
            return (Messages) container.getComponent(Globals.NAME_MESSAGES);
        }
    }

    Locale findLocale(TemplateContext context, VariableResolver varResolver) {
        return ((Request) varResolver.getVariable(context,
                YmirVariableResolver.NAME_YMIRREQUEST)).getLocale();
    }
}
