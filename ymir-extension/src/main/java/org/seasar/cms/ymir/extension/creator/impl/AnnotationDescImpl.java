package org.seasar.cms.ymir.extension.creator.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.seasar.cms.ymir.extension.creator.AnnotationDesc;

public class AnnotationDescImpl implements AnnotationDesc {

    private String name_;

    private String body_;

    public AnnotationDescImpl(Annotation annotation) {
        analyze(annotation);
    }

    void analyze(Annotation annotation) {
        Class type = annotation.annotationType();
        name_ = type.getName();

        Method[] methods = type.getDeclaredMethods();
        if (methods.length == 0) {
            body_ = "";
        } else {
            boolean hasValueMethod = false;
            Set<Method> methodSet = new TreeSet<Method>(
                    new Comparator<Method>() {
                        public int compare(Method o1, Method o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
            for (int i = 0; i < methods.length; i++) {
                methodSet.add(methods[i]);
                if ("value".equals(methods[i].getName())) {
                    hasValueMethod = true;
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            String delim = "";
            for (Iterator<Method> itr = methodSet.iterator(); itr.hasNext();) {
                Method method = itr.next();
                sb.append(delim);
                delim = ",";
                try {
                    append(sb, method.getName(), method.getReturnType(), method
                            .invoke(annotation, new Object[0]), hasValueMethod
                            && methods.length == 1);
                } catch (IllegalArgumentException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            }
            sb.append(")");
            body_ = sb.toString();
        }
    }

    void append(StringBuilder sb, String name, Class<?> type, Object value,
            boolean isDefault) {
        if (!isDefault) {
            sb.append(name).append("=");
        }
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            int length = Array.getLength(value);
            if (length == 1) {
                append(sb, componentType, Array.get(value, 0));
            } else {
                sb.append("{");
                String delim = "";
                for (int i = 0; i < length; i++) {
                    sb.append(delim);
                    delim = ",";
                    append(sb, componentType, Array.get(value, i));
                }
                sb.append("}");
            }
        } else {
            append(sb, type, value);
        }
    }

    void append(StringBuilder sb, Class<?> type, Object value) {
        if (type == Class.class) {
            sb.append(((Class) value).getName()).append(".class");
        } else if (Annotation.class.isAssignableFrom(type)) {
            sb.append(new AnnotationDescImpl((Annotation) value).getString());
        } else if (type == String.class) {
            append(sb, (String) value);
        } else if (type == Character.TYPE) {
            append(sb, ((Character) value).charValue());
        } else if (Enum.class.isAssignableFrom(type)) {
            sb.append(type.getName()).append(".").append(value);
        } else {
            sb.append(value);
        }
    }

    StringBuilder append(StringBuilder sb, char ch) {
        appendQuoted(sb.append('\''), ch).append('\'');
        return sb;
    }

    StringBuilder appendQuoted(StringBuilder sb, char ch) {
        if (ch == '"' || ch == '\'' || ch == '\\') {
            sb.append("\\").append(ch);
        } else if (ch == '\b') {
            sb.append("\\b");
        } else if (ch == '\t') {
            sb.append("\\t");
        } else if (ch == '\n') {
            sb.append("\\n");
        } else if (ch == '\f') {
            sb.append("\\f");
        } else if (ch == '\r') {
            sb.append("\\r");
        } else if (ch == '\0') {
            sb.append("\\0");
        } else if (ch < 32) {
            addPadding(sb.append("\\u"), Integer.toString(ch, 16), 4);
        } else {
            sb.append(ch);
        }
        return sb;
    }

    StringBuilder append(StringBuilder sb, String string) {
        sb.append('"');
        for (int i = 0; i < string.length(); i++) {
            appendQuoted(sb, string.charAt(i));
        }
        sb.append('"');
        return sb;
    }

    StringBuilder addPadding(StringBuilder sb, String string, int length) {
        int n = length - string.length();
        for (int i = 0; i < n; i++) {
            sb.append('0');
        }
        sb.append(string);
        return sb;
    }

    public AnnotationDescImpl(String name) {
        name_ = name;
    }

    public Object clone() {
        try {
            return (AnnotationDescImpl) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString() {
        return getString();
    }

    public String getString() {
        return "@" + name_ + body_;
    }

    public String getName() {
        return name_;
    }

    public String getBody() {
        return body_;
    }

    public void setBody(String body) {
        body_ = body;
    }
}
