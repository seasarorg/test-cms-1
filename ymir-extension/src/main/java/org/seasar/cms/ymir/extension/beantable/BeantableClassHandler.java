package org.seasar.cms.ymir.extension.beantable;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.cms.beantable.Beantable;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.seasar.framework.container.annotation.tiger.BindingType;
import org.seasar.framework.util.ClassTraversal.ClassHandler;

public class BeantableClassHandler implements ClassHandler {

    private Log log_ = LogFactory.getLog(getClass());

    private S2Container container_;

    private BeantableManager manager_;

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
            if (log_.isDebugEnabled()) {
                log_.debug("[SKIP] Class not found: " + className);
            }
            return;
        }

        if (!manager_.isManaged(beanClass)) {
            return;
        }

        if (log_.isInfoEnabled()) {
            log_.info("UPDATE TABLE FOR class: " + className);
        }

        Beantable beanTable = manager_.newBeantable(beanClass);
        try {
            beanTable.activate();
        } catch (SQLException ex) {
            log_.error("[SKIP] Can't activate Beantable for: " + className, ex);
            return;
        }
        try {
            beanTable.update(false);
        } catch (SQLException ex) {
            log_.error("[SKIP] Can't update Beantable for: " + className, ex);
            return;
        }

        if (log_.isInfoEnabled()) {
            log_.info("TABLE UPDATED SUCCESSFULLY");
        }
    }

    public void setContainer(S2Container container) {

        container_ = container;
    }

    @Binding(bindingType = BindingType.MUST, value = "beantableManager")
    public void setBeantableManager(BeantableManager manager) {

        manager_ = manager;
    }
}
