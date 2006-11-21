package org.seasar.cms.ymir.extension.creator.action.impl;

import static org.seasar.cms.ymir.impl.DefaultRequestProcessor.PARAM_METHOD;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.impl.SingleApplication;

public class CreateConfigurationAction extends AbstractUpdateAction {

    private static final String PARAMPREFIX_KEY = SourceCreator.PARAM_PREFIX
            + "key_";

    private static final String POM_XML = "pom.xml";

    private static final Object KEY_PROJECTROOT = "projectRoot";

    private static final String KEY_ROOTPACKAGENAME = "rootPackageName";

    private static final String KEY_WEBAPPROOT = "webappRoot";

    private static final String KEY_WEBAPPSOURCEROOT = "webappSourceRoot";

    public CreateConfigurationAction(SourceCreator sourceCreator) {
        super(sourceCreator);
    }

    public Response act(Request request, PathMetaData pathMetaData) {

        String subTask = request.getParameter(PARAM_SUBTASK);
        if ("create".equals(subTask)) {
            return actCreate(request, pathMetaData);
        } else {
            return actDefault(request, pathMetaData);
        }
    }

    Response actDefault(Request request, PathMetaData pathMetaData) {

        Application application = getSourceCreator().getApplication();
        String webappRoot = application.getWebappRoot();
        if (webappRoot != null) {
            String projectRoot = findProjectRootDirectory(webappRoot);
            if (projectRoot != null) {
                application.setProjectRoot(projectRoot);
            }
        }

        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("request", request);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("application", application);
        return getSourceCreator().getResponseCreator().createResponse(
                "createConfiguration", variableMap);
    }

    String findProjectRootDirectory(String webappRoot) {

        File dir = new File(webappRoot);
        while (dir != null && !new File(dir, POM_XML).exists()) {
            dir = dir.getParentFile();
        }
        if (dir != null) {
            return dir.getAbsolutePath();
        } else {
            return null;
        }
    }

    Response actCreate(Request request, PathMetaData pathMetaDataf) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        Application application = getSourceCreator().getApplication();
        for (Iterator itr = request.getParameterNames(); itr.hasNext();) {
            String name = (String) itr.next();
            if (!name.startsWith(PARAMPREFIX_KEY)) {
                continue;
            }
            String key = name.substring(PARAMPREFIX_KEY.length());
            String value = request.getParameter(name);
            if (SingleApplication.KEY_PROJECTROOT.equals(key)) {
                application.setProjectRoot(value);
            } else if (SingleApplication.KEY_ROOTPACKAGENAME.equals(key)) {
                application.setRootPackageName(value);
            } else {
                application.setProperty(key, value);
            }
        }

        String propertiesFilePath = application.getDefaultPropertiesFilePath();
        if (propertiesFilePath != null) {
            File file = new File(propertiesFilePath);
            file.getParentFile().mkdirs();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                application.save(fos, null);
            } catch (IOException ex) {
                throw new RuntimeException("Can't write property file: "
                        + file.getAbsolutePath());
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }

        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("request", request);
        variableMap.put("method", method);
        variableMap.put("parameters", getParameters(request));
        return getSourceCreator().getResponseCreator().createResponse(
                "createConfiguration_create", variableMap);
    }
}
