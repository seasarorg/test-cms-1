package org.seasar.cms.ymir;

public interface ApplicationManager {

    void addApplication(Application application);

    public Application[] getApplications();

    void setContextApplication(Application application);

    Application getContextApplication();

    Application findContextApplication();

    void setBaseApplication(Application application);
}
