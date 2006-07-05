package org.seasar.cms.framework.creator.action;


import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.creator.PathMetaData;

public interface UpdateAction {

    Response act(Request request, PathMetaData pathMetaData);
}
