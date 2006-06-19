package org.seasar.cms.framework.creator.impl;

import java.io.File;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;

interface UpdateAction {

    Response act(Request request, String className, File sourceFile,
        File templateFile);
}
