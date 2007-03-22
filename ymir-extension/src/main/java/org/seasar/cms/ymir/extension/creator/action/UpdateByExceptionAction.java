package org.seasar.cms.ymir.extension.creator.action;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;

public interface UpdateByExceptionAction {

    Response act(Request request, Throwable t);
}
