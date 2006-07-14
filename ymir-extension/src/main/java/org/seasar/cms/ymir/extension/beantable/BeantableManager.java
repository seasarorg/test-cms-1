package org.seasar.cms.ymir.beantable;

import static org.seasar.cms.ymir.extension.Globals.CONFIG_KEY_BEANTABLE_ENABLE;

import java.sql.SQLException;

import org.seasar.cms.beantable.Beantable;
import org.seasar.cms.ymir.Configuration;
import org.seasar.cms.ymir.LifecycleListener;
import org.seasar.cms.ymir.container.ClassTraverser;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.hotdeploy.HotdeployListener;
import org.seasar.framework.log.Logger;
import static org.seasar.cms.ymir.Configuration.PROJECTSTATUS_DEVELOP;

public class BeantableManager implements LifecycleListener, HotdeployListener {

    private ClassTraverser classTraverser_;

    private Configuration configuration_;

    private S2Container container_;

    private Logger logger_ = Logger.getLogger(getClass());

    /*
     * LifecycleListener
     */

    public void init() {

        if (!isEnabled()) {
            return;
        }

        classTraverser_.traverse();
    }

    boolean isEnabled() {

        return "true".equals(configuration_
            .getProperty(CONFIG_KEY_BEANTABLE_ENABLE));
    }

    public void destroy() {
    }

    /*
     * HotdeployListener
     */

    public void definedClass(Class clazz) {

        if (!configuration_.equalsProjectStatus(PROJECTSTATUS_DEVELOP)) {
            return;
        }
        if (!isEnabled()) {
            return;
        }
        if (!isBeanClass(clazz)) {
            return;
        }

        Beantable beantable = newBeantable(clazz);
        try {
            beantable.activate();
        } catch (SQLException ex) {
            logger_.error("Can't activate table: " + clazz.getName(), ex);
        }
        try {
            beantable.update();
        } catch (SQLException ex) {
            logger_.error("Can't update table: " + clazz.getName(), ex);
        }
    }

    boolean isBeanClass(Class clazz) {

        String packageName;
        String shortName;
        String className = clazz.getName();
        int dot = className.lastIndexOf('.');
        if (dot < 0) {
            packageName = "";
            shortName = className;
        } else {
            packageName = className.substring(0, dot);
            shortName = className.substring(dot + 1);
        }
        return classTraverser_.isMatched(packageName, shortName);
    }

    Beantable newBeantable(Class beanClass) {

        Beantable beanTable = (Beantable) container_
            .getComponent(Beantable.class);
        beanTable.setBeanClass(beanClass);
        return beanTable;
    }

    public void setClassTraverser(ClassTraverser classTraverser) {

        classTraverser_ = classTraverser;
    }

    public void setConfiguration(Configuration configuration) {

        configuration_ = configuration;
    }

    public void setContainer(S2Container container) {

        container_ = container;
    }
}
