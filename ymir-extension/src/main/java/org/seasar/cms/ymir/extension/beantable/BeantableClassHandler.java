package org.seasar.cms.ymir.extension.beantable;

import java.sql.SQLException;

import org.seasar.cms.beantable.Beantable;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.log.Logger;
import org.seasar.framework.util.ClassTraversal.ClassHandler;

public class BeantableClassHandler implements ClassHandler {

    private Logger logger_ = Logger.getLogger(getClass());

    private S2Container container_;

    public void processClass(String packageName, String shortClassName) {

        Class beanClass;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = getClass().getClassLoader();
        }
        String className = packageName + "." + shortClassName;
        try {
            beanClass = Class.forName(className, true, cl);
        } catch (ClassNotFoundException ex) {
            if (logger_.isDebugEnabled()) {
                logger_.debug("[SKIP] Class not found: " + className);
            }
            return;
        }

        if (logger_.isInfoEnabled()) {
            logger_.info("UPDATE TABLE FOR class: " + className);
        }

        Beantable beanTable = newBeantable(beanClass);
        try {
            beanTable.activate();
        } catch (SQLException ex) {
            logger_.error("[SKIP] Can't activate Beantable for: " + className,
                ex);
            return;
        }
        try {
            beanTable.update(false);
        } catch (SQLException ex) {
            logger_
                .error("[SKIP] Can't update Beantable for: " + className, ex);
            return;
        }

        if (logger_.isInfoEnabled()) {
            logger_.info("TABLE UPDATED SUCCESSFULLY");
        }
    }

    Beantable newBeantable(Class beanClass) {

        Beantable beanTable = (Beantable) container_
            .getComponent(Beantable.class);
        beanTable.setBeanClass(beanClass);
        return beanTable;
    }

    public void setContainer(S2Container container) {

        container_ = container;
    }
}
