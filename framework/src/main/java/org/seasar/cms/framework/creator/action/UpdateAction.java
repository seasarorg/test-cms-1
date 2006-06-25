package org.seasar.cms.framework.creator.action;

import java.io.File;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;

public interface UpdateAction {

    Response act(Request request, String className, File sourceFile,
        File templateFile);
}
