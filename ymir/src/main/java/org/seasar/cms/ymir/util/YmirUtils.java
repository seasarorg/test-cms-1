package org.seasar.cms.ymir.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

public class YmirUtils {

    private YmirUtils() {
    }

    public static Object copyProperties(Class destClass, Object src) {

        try {
            return copyProperties(destClass.newInstance(), src);
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
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

    public static Object[] copyProperties(Object[] dests, List srcList) {

        Class destClass = dests.getClass().getComponentType();
        if (dests.length < srcList.size()) {
            dests = (Object[]) Array.newInstance(destClass, srcList.size());
        }
        int idx = 0;
        for (Iterator itr = srcList.iterator(); itr.hasNext();) {
            dests[idx++] = copyProperties(destClass, itr.next());
        }
        return dests;
    }
}
