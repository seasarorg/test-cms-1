package org.seasar.cms.ymir.extension.creator.action;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.extension.creator.PathMetaData;

public interface UpdateAction {

    Response act(Request request, PathMetaData pathMetaData);
}
