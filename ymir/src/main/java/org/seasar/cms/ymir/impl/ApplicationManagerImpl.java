package org.seasar.cms.ymir.impl;

import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.ApplicationManager;
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

        Application application = (Application) application_.get();
        if (application == null) {
            application = baseApplication_;
        }
        return application;
    }

    public void setContextApplication(Application application) {

        application_.set(application);
    }

    public void setBaseApplication(Application application) {

        baseApplication_ = application;
    }
}
