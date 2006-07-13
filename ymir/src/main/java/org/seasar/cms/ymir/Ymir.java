package org.seasar.cms.ymir;

import javax.servlet.ServletContext;

public interface Ymir {

    void init(ServletContext servletContext, String configPath);

    void destroy();
}
