package org.seasar.cms.ymir.extension.creator.action.impl;

import java.io.IOException;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.Template;
import org.seasar.cms.ymir.response.SelfContainedResponse;
import org.seasar.cms.ymir.response.VoidResponse;

public class DoEditTemplateAction extends DoTemplateActionBase {

    public DoEditTemplateAction(SourceCreator sourceCreator) {
        super(sourceCreator);
    }

    public Response act(Request request, PathMetaData pathMetaData) {
        Template template = getTemplate(request);
        if (template == null || !template.exists()) {
            return VoidResponse.INSTANCE;
        }

        try {
            return new SelfContainedResponse(template.getInputStream(),
                    "text/html");
        } catch (IOException ex) {
            return VoidResponse.INSTANCE;
        }
    }
}
