package org.seasar.cms.pluggable;

import org.seasar.framework.convention.NamingConvention;

public interface PluggableNamingConvention extends NamingConvention {

    String fromComponentNameToClassName(String componentName);
}
