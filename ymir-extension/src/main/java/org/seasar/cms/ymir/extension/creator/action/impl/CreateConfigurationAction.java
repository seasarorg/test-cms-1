package org.seasar.cms.ymir.extension.creator.action.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.seasar.cms.ymir.Configuration;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.impl.SourceCreatorImpl;
import org.seasar.cms.ymir.impl.ConfigurationImpl;

public class CreateConfigurationAction extends AbstractUpdateAction {

    private static final String APP_PROPERTIES_PATH = "src/main/resources/app.properties";

    private static final String PARAMPREFIX_KEY = SourceCreatorImpl.PARAM_PREFIX
        + "key_";

    private static final String POM_XML = "pom.xml";

    public CreateConfigurationAction(SourceCreatorImpl sourceCreator) {
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

        Configuration configuration = getConfiguration();
        String webappRoot = configuration
            .getProperty(Configuration.KEY_WEBAPPROOT);
        if (webappRoot != null) {
            String projectRoot = findProjectRootDirectory(webappRoot);
            if (projectRoot != null) {
                configuration.setProperty(Configuration.KEY_PROJECTROOT,
                    projectRoot);
            }
        }

        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("request", request);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("configuration", configuration);
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

        Configuration configuration = new ConfigurationImpl();
        for (Iterator itr = request.getParameterNames(); itr.hasNext();) {
            String name = (String) itr.next();
            if (!name.startsWith(PARAMPREFIX_KEY)) {
                continue;
            }
            String value = request.getParameter(name);
            configuration.setProperty(name.substring(PARAMPREFIX_KEY.length()),
                value);
        }

        String projectRoot = configuration
            .getProperty(Configuration.KEY_PROJECTROOT);
        if (projectRoot != null) {
            File file = new File(projectRoot, APP_PROPERTIES_PATH);
            file.getParentFile().mkdirs();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                configuration.save(fos, null);
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

    Configuration getConfiguration() {

        Configuration configuration = getSourceCreator().getConfiguration();
        if (configuration == null) {
            configuration = new ConfigurationImpl();
        }
        return configuration;

    }
}
