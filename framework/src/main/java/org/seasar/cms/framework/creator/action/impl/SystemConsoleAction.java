package org.seasar.cms.framework.creator.action.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.creator.ClassDescBag;
import org.seasar.cms.framework.creator.PathMetaData;
import org.seasar.cms.framework.creator.impl.LazyPathMetaData;
import org.seasar.cms.framework.creator.impl.SourceCreatorImpl;

public class SystemConsoleAction extends AbstractUpdateAction {

    public SystemConsoleAction(SourceCreatorImpl sourceCreator) {
        super(sourceCreator);
    }

    public Response act(Request request, PathMetaData pathMetaData) {

        String subTask = request.getParameter(PARAM_SUBTASK);
        if ("confirmUpdateAllClasses".equals(subTask)) {
            return actConfirmUpdateAllClasses(request, pathMetaData);
        } else if ("updateAllClasses".equals(subTask)) {
            return actUpdateAllClasses(request, pathMetaData);
        } else {
            return actDefault(request, pathMetaData);
        }
    }

    Response actDefault(Request request, PathMetaData pathMetaData) {

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("method", request.getMethod());
        return getSourceCreator().getResponseCreator().createResponse(
            "systemConsole", variableMap);
    }

    Response actConfirmUpdateAllClasses(Request request,
        PathMetaData pathMetaData) {

        ClassDescBag classDescBag = getSourceCreator().gatherClassDescs(
            gatherPaths());

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("method", request.getMethod());
        variableMap.put("classDescBag", classDescBag);
        return getSourceCreator().getResponseCreator().createResponse(
            "systemConsole", variableMap);
    }

    Response actUpdateAllClasses(Request request, PathMetaData pathMetaData) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        ClassDescBag classDescBag = getSourceCreator().gatherClassDescs(
            gatherPaths());
        getSourceCreator().updateClasses(classDescBag, false);

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("method", method);
        return getSourceCreator().getResponseCreator().createResponse(
            "systemConsole_updateAllClasses", variableMap);
    }

    PathMetaData[] gatherPaths() {

        List pathList = new ArrayList();
        File dir = getSourceCreator().getWebappDirectory();
        gatherPaths(dir, "", pathList);
        return (PathMetaData[]) pathList.toArray(new PathMetaData[0]);
    }

    void gatherPaths(File root, String path, List pathList) {

        File dir;
        if (path.length() == 0) {
            dir = root;
        } else {
            dir = new File(root, path);
        }
        File[] childFiles = dir.listFiles();
        for (int i = 0; i < childFiles.length; i++) {
            String childName = childFiles[i].getName();
            String childPath = path + "/" + childName;
            if (shouldIgnore(childName)) {
                continue;
            }
            if (childFiles[i].isDirectory()) {
                gatherPaths(root, childPath, pathList);
            } else {
                addPathMetaDataIfNecessary(childPath, Request.METHOD_GET,
                    pathList);
                addPathMetaDataIfNecessary(childPath, Request.METHOD_POST,
                    pathList);
            }
        }
    }

    void addPathMetaDataIfNecessary(String path, String method, List pathList) {

        PathMetaData pathMetaData = new LazyPathMetaData(getSourceCreator(),
            path, method);
        if (pathMetaData.getComponentName() != null) {
            pathList.add(pathMetaData);
        }
    }

    boolean shouldIgnore(String name) {

        return ("CVS".equals(name) || ".svn".equals(name) || "_svn"
            .equals(name));
    }
}
