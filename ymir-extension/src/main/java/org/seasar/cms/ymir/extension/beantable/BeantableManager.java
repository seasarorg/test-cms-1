package org.seasar.cms.ymir.extension.beantable;

import static org.seasar.cms.ymir.extension.Globals.APPKEY_BEANTABLE_ENABLE;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.beantable.Beantable;
import org.seasar.cms.pluggable.ClassTraverser;
import org.seasar.cms.pluggable.Configuration;
import org.seasar.cms.pluggable.hotdeploy.HotdeployListener;
import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.ApplicationManager;
import org.seasar.cms.ymir.LifecycleListener;
import org.seasar.cms.ymir.extension.ClassTraverserBag;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.seasar.framework.container.annotation.tiger.BindingType;
import org.seasar.framework.log.Logger;
import org.seasar.framework.util.ClassTraversal.ClassHandler;

public class BeantableManager implements LifecycleListener, HotdeployListener {

    private Configuration configuration_;

    private S2Container container_;

    private ApplicationManager applicationManager_;

    private ClassHandler beantableClassHandler_;

    private ClassTraverserBag[] traverserBags_;

    private Logger logger_ = Logger.getLogger(getClass());

    /*
     * LifecycleListener
     */

    public void init() {

        List<ClassTraverserBag> bagList = new ArrayList<ClassTraverserBag>();

        Application[] applications = applicationManager_.getApplications();
        for (int i = 0; i < applications.length; i++) {
            if (isEnabled(applications[i])) {
                ClassTraverserBag bag = newClassTraverserBag(applications[i]);
                bagList.add(bag);
            }
        }
        traverserBags_ = bagList.toArray(new ClassTraverserBag[0]);

        traserse();
    }

    void traserse() {

        for (int i = 0; i < traverserBags_.length; i++) {
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(
                        traverserBags_[i].getApplication().getS2Container()
                                .getClassLoader());
                traverserBags_[i].getClassTraverser().traverse();
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        }
    }

    ClassTraverserBag newClassTraverserBag(Application application) {

        ClassTraverser traverser = new ClassTraverser();
        traverser.setClassHandler(beantableClassHandler_);
        String targetPackageName = application.getRootPackageName() + ".dao";
        traverser.addClassPattern(targetPackageName, ".*");
        traverser.addIgnoreClassPattern(targetPackageName,
                "(Abstract)?.*(Base|Dao)");
        traverser.addReferenceClass(application.getReferenceClass());
        return new ClassTraverserBag(traverser, application);
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

        ClassTraverserBag bag = getClassTraverserBag(clazz);
        if (bag == null) {
            return;
        }

        if (!isEnabled(bag.getApplication())) {
            return;
        }
        if (!isBeanClass(clazz, bag.getClassTraverser())) {
            return;
        }
        if (!isManaged(clazz)) {
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

    ClassTraverserBag getClassTraverserBag(Class clazz) {

        if (traverserBags_ != null) {
            for (int i = 0; i < traverserBags_.length; i++) {
                if (traverserBags_[i].getApplication().isCapable(clazz)) {
                    return traverserBags_[i];
                }
            }
        }
        return null;
    }

    boolean isBeanClass(Class clazz, ClassTraverser traverser) {

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
        return traverser.isMatched(packageName, shortClassName);
    }

    @SuppressWarnings("unchecked")
    Beantable newBeantable(Class beanClass) {

        Beantable beanTable = (Beantable) container_
                .getComponent(Beantable.class);
        beanTable.setBeanClass(beanClass);
        return beanTable;
    }

    boolean isManaged(Class<?> beanClass) {

        Managed managed = beanClass.getAnnotation(Managed.class);
        return (managed != null && managed.value());
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

    @Binding(bindingType = BindingType.MUST, value = "beantableClassHandler")
    public void setBeantableClassHandler(
            BeantableClassHandler beantableClassHandler) {

        beantableClassHandler_ = beantableClassHandler;
    }
}
