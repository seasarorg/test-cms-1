package org.seasar.cms.ymir.extension.creator.action.impl;

import java.io.IOException;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.Template;
import org.seasar.cms.ymir.response.VoidResponse;
import org.seasar.kvasir.util.io.IORuntimeException;
import org.seasar.kvasir.util.io.IOUtils;

public class DoUpdateTemplateAction extends DoEditTemplateAction {

    private static final String PARAM_BODY = "body";

    public DoUpdateTemplateAction(SourceCreator sourceCreator) {
        super(sourceCreator);
    }

    public Response act(Request request, PathMetaData pathMetaData) {
        Template template = getTemplate(request);
        if (template == null || !template.exists()) {
            return VoidResponse.INSTANCE;
        }

        String body = request.getParameter(PARAM_BODY);
        if (body == null) {
            return VoidResponse.INSTANCE;
        }

        try {
            IOUtils.writeString(template.getOutputStream(), body, "UTF-8",
                    false);
        } catch (IOException ex) {
            throw new IORuntimeException("Can't write template: path="
                    + template.getPath(), ex);
        }

        return VoidResponse.INSTANCE;
    }
}
