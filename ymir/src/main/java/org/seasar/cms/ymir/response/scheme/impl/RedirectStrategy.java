package org.seasar.cms.ymir.response.scheme.impl;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;

import org.apache.commons.beanutils.BeanUtils;
import org.seasar.cms.ymir.Path;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.response.RedirectResponse;
import org.seasar.cms.ymir.response.scheme.Strategy;
import org.seasar.kvasir.util.el.EvaluationException;
import org.seasar.kvasir.util.el.TextTemplateEvaluator;
import org.seasar.kvasir.util.el.VariableResolver;
import org.seasar.kvasir.util.el.impl.SimpleTextTemplateEvaluator;

public class RedirectStrategy implements Strategy {

    private static final String SCHEME = "redirect";

    private static final String ENCODING = "UTF-8";

    private static final TextTemplateEvaluator evaluator_ = new SimpleTextTemplateEvaluator();

    public String getScheme() {

        return SCHEME;
    }

    public Response constructResponse(String path, Object component) {

        return new RedirectResponse(constructPath(path, component));
    }

    String constructPath(String path, Object component) {

        Path p = null;
        int lparen = path.lastIndexOf('(');
        if (lparen >= 0 && path.endsWith(")")) {
            // /path(a,b)形式。
            p = new Path(path.substring(0, lparen));
            parseFunctionTypeParameters(path.substring(lparen + 1, path
                    .length() - 1), component, p);
        } else {
            int question = path.indexOf('?');
            if (question >= 0) {
                p = new Path(path.substring(0, question));
                if (path.indexOf('=', question + 1) >= 0) {
                    // path?a=${a}&b=${b}形式。
                    parseExpressionTypeParameters(path.substring(question + 1),
                            component, p);
                } else {
                    // path?a&b形式。
                    parseSimpleTypeParameters(path.substring(question + 1),
                            component, p);
                }
            }
        }
        if (p != null) {
            return p.asString();
        } else {
            return path;
        }
    }

    void parseFunctionTypeParameters(String parameter, Object component,
            Path path) {

        String[] params = parameter.split(",");
        for (int i = 0; i < params.length; i++) {
            String name = decodeURL(params[i].trim());
            path.addParameter(name, getProperty(component, name));
        }
    }

    String getProperty(Object component, String name) {
        if (component != null) {
            try {
                return BeanUtils.getProperty(component, name);
            } catch (IllegalAccessException ex) {
                ;
            } catch (InvocationTargetException ex) {
                ;
            } catch (NoSuchMethodException ex) {
                ;
            }
        }
        return null;
    }

    void parseExpressionTypeParameters(String parameter, Object component,
            Path path) {

        VariableResolver resolver = new ComponentVariableResolver(component);

        String[] params = parameter.split("&");
        for (int i = 0; i < params.length; i++) {
            int equal = params[i].indexOf('=');
            if (equal >= 0) {
                try {
                    path.addParameter(
                            evaluator_.evaluateAsString(decodeURL(params[i]
                                    .substring(0, equal)), resolver),
                            evaluator_.evaluateAsString(decodeURL(params[i]
                                    .substring(equal + 1)), resolver));
                } catch (EvaluationException ex) {
                    throw new RuntimeException(
                            "Expression type parameter's format is wrong: "
                                    + parameter, ex);
                }
            }
        }
    }

    void parseSimpleTypeParameters(String parameter, Object component, Path path) {

        String[] params = parameter.split("&");
        for (int i = 0; i < params.length; i++) {
            String name = decodeURL(params[i]);
            path.addParameter(name, getProperty(component, name));
        }
    }

    String decodeURL(String string) {

        try {
            return URLDecoder.decode(string, ENCODING);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private class ComponentVariableResolver implements VariableResolver {
        private Object component_;

        public ComponentVariableResolver(Object component) {

            component_ = component;
        }

        public Object getValue(Object key) {

            if (key instanceof String) {
                return getProperty(component_, (String) key);
            } else {
                return null;
            }
        }
    }
}
