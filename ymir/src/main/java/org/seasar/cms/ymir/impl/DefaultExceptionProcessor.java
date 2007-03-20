package org.seasar.cms.ymir.impl;

import java.lang.reflect.InvocationTargetException;

import org.seasar.cms.ymir.ExceptionProcessor;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.Updater;
import org.seasar.cms.ymir.Ymir;
import org.seasar.cms.ymir.handler.ExceptionHandler;
import org.seasar.cms.ymir.response.ForwardResponse;
import org.seasar.cms.ymir.response.constructor.ResponseConstructor;
import org.seasar.cms.ymir.response.constructor.ResponseConstructorSelector;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.S2Container;

public class DefaultExceptionProcessor implements ExceptionProcessor {

    private static final String SUFFIX_HANDLER = "Handler";

    private Ymir ymir_;

    private ResponseConstructorSelector responseConstructorSelector_;

    private Updater[] updaters_ = new Updater[0];

    public void setYmir(Ymir ymir) {

        ymir_ = ymir;
    }

    public void setResponseConstructorSelector(
            ResponseConstructorSelector responseConstructorSelector) {

        responseConstructorSelector_ = responseConstructorSelector;
    }

    public void setUpdaters(Updater[] updaters) {

        updaters_ = updaters;
    }

    public Response process(Throwable t) {

        if (t instanceof InvocationTargetException) {
            Throwable cause = ((InvocationTargetException) t).getCause();
            if (cause != null) {
                t = cause;
            }
        }

        ExceptionHandler handler = null;
        Class exceptionClass = t.getClass();
        do {
            try {
                handler = (ExceptionHandler) getS2Container().getComponent(
                        getComponentName(exceptionClass));
                break;
            } catch (ComponentNotFoundRuntimeException ignore) {
            }
        } while ((exceptionClass = exceptionClass.getSuperclass()) != Object.class);

        if (handler != null) {
            Response response = constructResponse(handler.handle(t));
            if (response.getType() == Response.TYPE_PASSTHROUGH) {
                response = new ForwardResponse(PATH_EXCEPTION_TEMPLATE
                        + getClassShortName(exceptionClass)
                        + SUFFIX_EXCEPTION_TEMPLATE);
            }
            return response;
        } else {
            // ハンドラが見つからなかったので再スローする。
            if (t instanceof Error) {
                throw (Error) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t);
            }
        }
    }

    Response constructResponse(String returnValue) {

        ResponseConstructor constructor = responseConstructorSelector_
                .getResponseConstructor(String.class);
        if (constructor == null) {
            throw new ComponentNotFoundRuntimeException(
                    "Can't find ResponseConstructor for type '" + String.class
                            + "' in ResponseConstructorSelector");
        }

        return constructor.constructResponse(null, returnValue);
    }

    String getComponentName(Class clazz) {

        String name = getClassShortName(clazz);
        if (name.length() > 0) {
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        return name + SUFFIX_HANDLER;
    }

    String getClassShortName(Class clazz) {

        String name = clazz.getName();
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            return name.substring(dot + 1);
        } else {
            return name;
        }
    }

    S2Container getS2Container() {
        return ymir_.getApplication().getS2Container();
    }
}
