package org.seasar.cms.ymir.extension.creator.action.impl;

import static org.seasar.cms.ymir.impl.DefaultRequestProcessor.PARAM_METHOD;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.ymir.MessagesNotFoundRuntimeException;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.action.UpdateByExceptionAction;
import org.seasar.kvasir.util.io.IORuntimeException;
import org.seasar.kvasir.util.io.IOUtils;
import org.seasar.kvasir.util.io.impl.FileResource;

public class CreateMessagesAction extends AbstractAction implements
        UpdateByExceptionAction {

    private static final String SUFFIX_XPROPERTIES = ".xproperties";

    public CreateMessagesAction(SourceCreator sourceCreator) {
        super(sourceCreator);
    }

    public Response act(Request request, Throwable t) {

        String subTask = request.getParameter(PARAM_SUBTASK);
        if ("create".equals(subTask)) {
            return actCreate(request, t);
        } else {
            return actDefault(request, t);
        }
    }

    Response actDefault(Request request, Throwable t) {

        MessagesNotFoundRuntimeException mnfre = (MessagesNotFoundRuntimeException) t;

        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("request", request);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("messageKey", mnfre.getMessageKey());
        variableMap.put("messagesName", mnfre.getMessagesName());
        return getSourceCreator().getResponseCreator().createResponse(
                "createMessages", variableMap);
    }

    Response actCreate(Request request, Throwable t) {

        MessagesNotFoundRuntimeException mnfre = (MessagesNotFoundRuntimeException) t;

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        createMessages(mnfre.getMessagesName());

        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("request", request);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("method", method);
        variableMap.put("messagesName", mnfre.getMessagesName());
        return getSourceCreator().getResponseCreator().createResponse(
                "createMessages_create", variableMap);
    }

    void createMessages(String messagesName) {

        OutputStream os;
        try {
            os = new FileResource(getSourceCreator().getResourcesDirectory())
                    .getChildResource(messagesName + SUFFIX_XPROPERTIES)
                    .getOutputStream();
        } catch (IOException ex) {
            throw new IORuntimeException(ex);
        }
        IOUtils.writeString(os, "", "UTF-8", false);
    }
}
