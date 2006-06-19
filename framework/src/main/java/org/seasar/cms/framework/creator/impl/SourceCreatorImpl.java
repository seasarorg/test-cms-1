package org.seasar.cms.framework.creator.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.seasar.cms.framework.MatchedPathMapping;
import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.RequestProcessor;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.ResponseCreator;
import org.seasar.cms.framework.container.hotdeploy.LocalOndemandCreatorContainer;
import org.seasar.cms.framework.creator.SourceCreator;
import org.seasar.cms.framework.creator.SourceGenerator;
import org.seasar.cms.framework.creator.TemplateAnalyzer;
import org.seasar.cms.framework.impl.DefaultRequestProcessor;
import org.seasar.cms.framework.impl.RedirectResponse;
import org.seasar.cms.framework.zpt.ZptResponseCreator;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.hotdeploy.OndemandCreatorContainer;
import org.seasar.framework.exception.ClassNotFoundRuntimeException;

import net.skirnir.xom.IllegalSyntaxException;
import net.skirnir.xom.ValidationException;
import net.skirnir.xom.XMLParserFactory;
import net.skirnir.xom.XOMapper;
import net.skirnir.xom.XOMapperFactory;

public class SourceCreatorImpl implements SourceCreator {

    public static final String PARAM_PREFIX = "__cms__";

    public static final String PARAM_TASK = PARAM_PREFIX + "task";

    private S2Container container_;

    private DefaultRequestProcessor defaultRequestProcessor_;

    private LocalOndemandCreatorContainer creatorContainer_;

    private File sourceDirectory_;

    private File classesDirectory_;

    private File webappDirectory_;

    private TemplateAnalyzer analyzer_;

    private String encoding_ = "UTF-8";

    private String pagePackageName_;

    private String dtoPackageName_;

    private SourceGenerator sourceGenerator_;

    private ResponseCreator responseCreator_ = new ZptResponseCreator();

    private UpdateActionSelector actionSelector_ = new UpdateActionSelector()
        .register(new Condition(false, false, false, Request.METHOD_GET),
            new CreateTemplateAction(this)).register(
            new Condition(true, false, false, Request.METHOD_GET),
            new CreateTemplateAction(this)).register(
            new Condition(true, false, true, Request.METHOD_GET),
            new UpdateClassesAction(this)).register(
            new Condition(true, true, false, Request.METHOD_GET),
            new CreateTemplateAction(this)).register(
            new Condition(true, true, true, Request.METHOD_GET),
            new UpdateClassesAction(this)).register(
            new Condition(false, false, false, Request.METHOD_POST),
            new CreateClassAndTemplateAction(this)).register(
            new Condition(true, false, false, Request.METHOD_POST),
            new CreateClassAndTemplateAction(this)).register(
            new Condition(true, false, true, Request.METHOD_POST),
            new UpdateClassesAction(this)).register(
            new Condition(true, true, false, Request.METHOD_POST),
            new CreateTemplateAction(this)).register(
            new Condition(true, true, true, Request.METHOD_POST),
            new UpdateClassesAction(this)).register("createClass",
            new CreateClassAction(this)).register("createTemplate",
            new CreateTemplateAction(this)).register("createClassAndTemplate",
            new CreateClassAndTemplateAction(this));

    public Response update(String path, String method, Request request) {

        String className = getClassName(getComponentName(path, method));
        File sourceFile = getSourceFile(className + "Base");
        File templateFile = getTemplateFile(path);

        Object condition;

        String task = request.getParameter(PARAM_TASK);
        if (task != null) {
            condition = task;
        } else {
            if ("".equals(path)) {
                String welcomeFile = getWelcomeFile();
                if (welcomeFile != null) {
                    return new RedirectResponse("/" + welcomeFile);
                }
                if (className == null || sourceFile.exists()) {
                    return null;
                }
                condition = "createClass";
            } else {
                condition = new Condition((className != null), sourceFile
                    .exists(), templateFile.exists(), method);
            }
        }

        UpdateAction action = actionSelector_.getAction(condition);
        if (action != null) {
            return action.act(request, className, sourceFile, templateFile);
        } else {
            return null;
        }
    }

    String getWelcomeFile() {

        XOMapper mapper = XOMapperFactory.newInstance();
        mapper.setStrict(false);
        File webXml = new File(getWebappDirectory(), "WEB-INF/web.xml");
        if (!webXml.exists()) {
            return null;
        }

        WebApp webApp;
        try {
            webApp = (WebApp) mapper.toBean(
                XMLParserFactory.newInstance()
                    .parse(
                        new InputStreamReader(new FileInputStream(webXml),
                            "UTF-8")).getRootElement(), WebApp.class);
        } catch (ValidationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalSyntaxException ex) {
            throw new RuntimeException(ex);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        WelcomeFileList welcomeFileList = webApp.getWelcomeFileList();
        if (welcomeFileList == null) {
            return null;
        }
        String[] welcomeFiles = welcomeFileList.getWelcomeFiles();
        if (welcomeFiles.length > 0) {
            return welcomeFiles[0];
        } else {
            return null;
        }
    }

    public String getComponentName(String path, String method) {

        if (path == null) {
            return null;
        }
        MatchedPathMapping matched = defaultRequestProcessor_
            .findMatchedPathMapping(path, method);
        if (matched == null) {
            return null;
        } else {
            return matched.getPathMapping().getComponentName(
                matched.getVariableResolver());
        }
    }

    public String getActionName(String path, String method) {

        if (path == null) {
            return null;
        }
        MatchedPathMapping matched = defaultRequestProcessor_
            .findMatchedPathMapping(path, method);
        if (matched == null) {
            return null;
        } else {
            return matched.getPathMapping().getActionName(
                matched.getVariableResolver());
        }
    }

    public String getClassName(String componentName) {

        if (componentName == null) {
            return null;
            // TODO 自動生成対象外のコンポーネントを取得する必要はないため。
            //        } else if (container_.hasComponentDef(componentName)) {
            //            return container_.getComponentDef(componentName)
            //                .getComponentClass().getName();
        } else {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(
                    creatorContainer_.getClassLoader());
                int size = creatorContainer_.getCreatorSize();
                for (int i = 0; i < size; i++) {
                    try {
                        ComponentDef componentDef = creatorContainer_
                            .getCreator(i).getComponentDef(container_,
                                componentName);
                        if (componentDef != null) {
                            return componentDef.getComponentClass().getName();
                        }
                    } catch (ClassNotFoundRuntimeException ex) {
                        return ex.getCause().getMessage();
                    }
                }
            } finally {
                Thread.currentThread().setContextClassLoader(cl);
            }
        }
        return null;
    }

    public Class getClass(String className) {

        if (className == null) {
            return null;
        }
        try {
            return Class.forName(className, true, creatorContainer_
                .getClassLoader());
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    File getTemplateFile(String path) {

        return new File(webappDirectory_, path);
    }

    File getSourceFile(String className) {

        return new File(sourceDirectory_, className.replace('.', '/') + ".java");
    }

    File getClassFile(String className) {

        return new File(classesDirectory_, className.replace('.', '/')
            + ".class");
    }

    public void setOndemandCreatorContainer(OndemandCreatorContainer container) {

        if (container instanceof LocalOndemandCreatorContainer) {
            creatorContainer_ = (LocalOndemandCreatorContainer) container;
        } else {
            throw new ComponentNotFoundRuntimeException(
                "LocalOndemandCreatorContainer");
        }
    }

    public S2Container getContainer() {

        return container_;
    }

    public void setContainer(S2Container container) {

        container_ = container;
    }

    public void setRequestProcessor(RequestProcessor requestProcessor) {

        if (requestProcessor instanceof DefaultRequestProcessor) {
            defaultRequestProcessor_ = (DefaultRequestProcessor) requestProcessor;
        } else {
            throw new ComponentNotFoundRuntimeException(
                "DefaultRequestProcessor");
        }
    }

    public void setSourceDirectoryPath(String sourceDirectoryPath) {

        sourceDirectory_ = new File(sourceDirectoryPath);
    }

    public void setClassesDirectoryPath(String classesDirectoryPath) {

        classesDirectory_ = new File(classesDirectoryPath);
    }

    public File getWebappDirectory() {

        return webappDirectory_;
    }

    public void setWebappDirectoryPath(String webappDirectoryPath) {

        webappDirectory_ = new File(webappDirectoryPath);
    }

    public TemplateAnalyzer getTemplateAnalyzer() {

        return analyzer_;
    }

    public void setTemplateAnalyzer(TemplateAnalyzer analyzer) {

        analyzer_ = analyzer;
    }

    public String getEncoding() {

        return encoding_;
    }

    public void setEncoding(String encoding) {

        encoding_ = encoding;
    }

    public String getPagePackageName() {

        return pagePackageName_;
    }

    public void setPagePackageName(String pagePackageName) {

        pagePackageName_ = pagePackageName;
    }

    public String getDtoPackageName() {

        return dtoPackageName_;
    }

    public void setDtoPackageName(String dtoPackageName) {

        dtoPackageName_ = dtoPackageName;
    }

    public SourceGenerator getSourceGenerator() {

        return sourceGenerator_;
    }

    public void setSourceGenerator(SourceGenerator sourceGenerator) {

        sourceGenerator_ = sourceGenerator;
    }

    public ResponseCreator getResponseCreator() {

        return responseCreator_;
    }

    public void setResponseCreaator(ResponseCreator responseCreator) {

        responseCreator_ = responseCreator;
    }
}
