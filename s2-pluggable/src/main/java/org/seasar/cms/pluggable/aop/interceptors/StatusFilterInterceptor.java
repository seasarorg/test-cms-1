package org.seasar.cms.pluggable.aop.interceptors;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.seasar.cms.pluggable.Configuration;
import org.seasar.framework.aop.interceptors.AbstractInterceptor;
import org.seasar.framework.util.ArrayUtil;

public class StatusFilterInterceptor extends AbstractInterceptor {

    private static final long serialVersionUID = -8072142267040138908L;

    private Configuration configuration_;

    private String[] enabledStatuses_ = new String[0];

    private MethodInterceptor interceptor_;

    private boolean enabled_;

    public Object invoke(MethodInvocation invocation) throws Throwable {

        if (enabled_) {
            return interceptor_.invoke(invocation);
        } else {
            return invocation.proceed();
        }
    }

    public void init() {

        enabled_ = false;
        if (configuration_ != null) {
            String status = configuration_
                .getProperty(Configuration.KEY_PROJECTSTATUS);
            if (status != null) {
                for (int i = 0; i < enabledStatuses_.length; i++) {
                    if (status.equals(enabledStatuses_[i])) {
                        enabled_ = true;
                        break;
                    }
                }
            }
        }
    }

    public void addEnabledStatus(String status) {

        enabledStatuses_ = (String[]) ArrayUtil.add(enabledStatuses_, status);
    }

    public void setConfiguration(Configuration configuration) {

        configuration_ = configuration;
    }

    public void setInterceptor(MethodInterceptor interceptor) {

        interceptor_ = interceptor;
    }
}
