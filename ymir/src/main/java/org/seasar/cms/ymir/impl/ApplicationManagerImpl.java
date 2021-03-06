package org.seasar.cms.ymir.impl;

import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.ApplicationManager;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.seasar.framework.container.annotation.tiger.BindingType;
import org.seasar.framework.util.ArrayUtil;

public class ApplicationManagerImpl implements ApplicationManager {

    private Application[] applications_ = new Application[0];

    private Application baseApplication_;

    private ThreadLocal application_ = new ThreadLocal();

    public void addApplication(Application application) {

        applications_ = (Application[]) ArrayUtil.add(applications_,
                application);
    }

    public Application[] getApplications() {

        return applications_;
    }

    public Application getContextApplication() {

        return (Application) application_.get();
    }

    public Application findContextApplication() {

        Application application = getContextApplication();
        if (application == null) {
            application = baseApplication_;
        }
        return application;
    }

    @Binding(bindingType = BindingType.NONE)
    public void setContextApplication(Application application) {

        application_.set(application);
    }

    @Binding(bindingType = BindingType.NONE)
    public void setBaseApplication(Application application) {

        baseApplication_ = application;
    }
}
