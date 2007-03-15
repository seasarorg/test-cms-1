package org.seasar.cms.ymir.util;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.seasar.framework.log.Logger;

public class BeanUtils {

    private static final Logger logger_ = Logger.getLogger(BeanUtils.class);

    protected BeanUtils() {
    }

    public static void populate(BeanUtilsBean beanUtilsBean, Object bean,
            Map properties) {
        if (bean == null || properties == null) {
            return;
        }

        for (Iterator itr = properties.keySet().iterator(); itr.hasNext();) {
            String name = (String) itr.next();
            if (name == null) {
                continue;
            }
            try {
                beanUtilsBean.setProperty(bean, name, properties.get(name));
            } catch (Throwable t) {
                if (logger_.isDebugEnabled()) {
                    logger_.debug("Can't populate property '" + name + "'", t);
                }
            }
        }
    }

    public static void copyProperties(BeanUtilsBean beanUtilsBean, Object bean,
            Map properties) {
        if (bean == null || properties == null) {
            return;
        }

        for (Iterator itr = properties.keySet().iterator(); itr.hasNext();) {
            String name = (String) itr.next();
            if (name == null) {
                continue;
            }
            if (beanUtilsBean.getPropertyUtils().isWriteable(bean, name)) {
                try {
                    beanUtilsBean
                            .copyProperty(bean, name, properties.get(name));
                } catch (Throwable t) {
                    if (logger_.isDebugEnabled()) {
                        logger_.debug("Can't copy property '" + name + "'", t);
                    }
                }
            }
        }
    }
}
