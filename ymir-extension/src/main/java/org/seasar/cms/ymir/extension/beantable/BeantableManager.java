package org.seasar.cms.ymir.extension.beantable;

import static org.seasar.cms.ymir.extension.Globals.APPKEY_BEANTABLE_ENABLE;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.beantable.Beantable;
import org.seasar.cms.pluggable.ClassTraverser;
import org.seasar.cms.pluggable.Configuration;
import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.ApplicationManager;
import org.seasar.cms.ymir.LifecycleListener;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.hotdeploy.HotdeployListener;
import org.seasar.framework.log.Logger;
import org.seasar.framework.util.ClassTraversal.ClassHandler;

public class BeantableManager implements LifecycleListener, HotdeployListener {

    private Configuration configuration_;

    private S2Container container_;

    private ApplicationManager applicationManager_;

    private ClassHandler beantableClassHandler_;

    private ClassTraverser[] traversers_;

    private Logger logger_ = Logger.getLogger(getClass());

    /*
     * LifecycleListener
     */

    public void init() {

        List<ClassTraverser> traverserList = new ArrayList<ClassTraverser>();
        Application[] applications = applicationManager_.getApplications();
        for (int i = 0; i < applications.length; i++) {
            if (isEnabled(applications[i])) {
                ClassTraverser traverser = newClassTraverser(applications[i]);
                traverserList.add(traverser);
            }
        }
        traversers_ = traverserList.toArray(new ClassTraverser[0]);

        traserse();
    }

    void traserse() {
        for (int i = 0; i < traversers_.length; i++) {
            traversers_[i].traverse();
        }
    }

    ClassTraverser newClassTraverser(Application application) {
        ClassTraverser traverser = new ClassTraverser();
        traverser.setClassHandler(beantableClassHandler_);
        String targetPackageName = application.getRootPackageName() + ".dao";
        traverser.addClassPattern(targetPackageName, ".*");
        traverser.addIgnoreClassPattern(targetPackageName,
                "(Abstract)?.*(Base|Dao)");
        traverser.addReferenceClass(application.getReferenceClass());
        return traverser;
    }

    boolean isEnabled(Application application) {

        return ("true".equals(application.getProperty(APPKEY_BEANTABLE_ENABLE)) && application
                .getReferenceClass() != null);
    }

    public void destroy() {
    }

    /*
     * HotdeployListener
     */

    public void definedClass(Class clazz) {

        if (!configuration_
                .equalsProjectStatus(Configuration.PROJECTSTATUS_DEVELOP)) {
            return;
        }

        Application application = getApplication(clazz);
        if (application == null) {
            return;
        }

        if (!isEnabled(application)) {
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

    Application getApplication(Class clazz) {

        Application[] applications = applicationManager_.getApplications();
        for (int i = 0; i < applications.length; i++) {
            if (applications[i].isCapable(clazz)) {
                return applications[i];
            }
        }
        return null;
    }

    boolean isBeanClass(Class clazz) {

        String packageName;
        String shortClassName;
        String className = clazz.getName();
        int dot = className.lastIndexOf('.');
        if (dot < 0) {
            packageName = "";
            shortClassName = className;
        } else {
            packageName = className.substring(0, dot);
            shortClassName = className.substring(dot + 1);
        }
        return isMatched(packageName, shortClassName);
    }

    boolean isMatched(String packageName, String shortClassName) {
        for (int i = 0; i < traversers_.length; i++) {
            if (traversers_[i].isMatched(packageName, shortClassName)) {
                return true;
            }
        }
        return false;
    }

    Beantable newBeantable(Class beanClass) {

        Beantable beanTable = (Beantable) container_
                .getComponent(Beantable.class);
        beanTable.setBeanClass(beanClass);
        return beanTable;
    }

    public void setConfiguration(Configuration configuration) {

        configuration_ = configuration;
    }

    public void setContainer(S2Container container) {

        container_ = container;
    }

    public void setApplicationManager(ApplicationManager applicationManager) {

        applicationManager_ = applicationManager;
    }

    public void setBeantableClassHandler(
            BeantableClassHandler beantableClassHandler) {

        beantableClassHandler_ = beantableClassHandler;
    }
}
