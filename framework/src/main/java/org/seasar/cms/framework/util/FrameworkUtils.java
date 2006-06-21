package org.seasar.cms.framework.util;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

public class FrameworkUtils {

    private FrameworkUtils() {
    }

    public static Object copyProperties(Object dest, Object src) {

        try {
            BeanUtils.copyProperties(dest, src);
        } catch (IllegalAccessException ex) {
            ;
        } catch (InvocationTargetException ex) {
            ;
        }
        return dest;
    }
}
