package org.seasar.cms.ymir.extension.creator.action;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.impl.SourceCreatorImpl;

public interface UpdateAction {

    String PARAM_METHOD = SourceCreatorImpl.PARAM_PREFIX + "method";

    Response act(Request request, PathMetaData pathMetaData);
}
