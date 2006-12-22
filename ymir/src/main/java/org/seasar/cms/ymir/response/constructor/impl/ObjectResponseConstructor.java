package org.seasar.cms.ymir.response.constructor.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.response.VoidResponse;
import org.seasar.cms.ymir.response.constructor.ResponseConstructor;
import org.seasar.cms.ymir.response.constructor.ResponseConstructorSelector;

public class ObjectResponseConstructor implements ResponseConstructor {

    private ResponseConstructorSelector responseConstructorSelector_;

    public Class getTargetClass() {

        return Object.class;
    }

    public Response constructResponse(Object component, Object returnValue) {

        if (returnValue == null) {
            return VoidResponse.INSTANCE;
        }

        ResponseConstructor responseConstructor = findResponseConstructor(returnValue
                .getClass());
        if (responseConstructor == null) {
            if (responseConstructorSelector_
                    .hasResponseConstructor(String.class)) {
                responseConstructor = responseConstructorSelector_
                        .getResponseConstructor(String.class);
                returnValue = returnValue.toString();
            } else {
                throw new RuntimeException(
                        "Can't construct response for return value: class="
                                + returnValue.getClass() + ", value="
                                + returnValue);
            }
        }

        return responseConstructor.constructResponse(component, returnValue);
    }

    ResponseConstructor findResponseConstructor(Class clazz) {
        if (clazz == Object.class) {
            // 無限ループを避けるため。
            return null;
        }

        Set interfaceSet = new LinkedHashSet();
        if (clazz.isInterface()) {
            if (responseConstructorSelector_.hasResponseConstructor(clazz)) {
                return responseConstructorSelector_
                        .getResponseConstructor(clazz);
            }
            interfaceSet.addAll(Arrays.asList(clazz.getInterfaces()));
        } else {
            Class type = clazz;
            do {
                if (responseConstructorSelector_.hasResponseConstructor(type)) {
                    return responseConstructorSelector_
                            .getResponseConstructor(type);
                }
                interfaceSet.addAll(Arrays.asList(type.getInterfaces()));
            } while ((type = type.getSuperclass()) != Object.class);
        }

        for (Iterator itr = interfaceSet.iterator(); itr.hasNext();) {
            Class interfaze = (Class) itr.next();
            ResponseConstructor constructor = findResponseConstructor(interfaze);
            if (constructor != null) {
                return constructor;
            }
        }
        return null;
    }

    public void setResponseConstructorSelector(
            ResponseConstructorSelector responseConstructorSelector) {
        responseConstructorSelector_ = responseConstructorSelector;
    }
}
