package org.seasar.cms.framework.generator;

import java.io.File;

import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.container.hotdeploy.LocalOndemandCreatorContainer;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.hotdeploy.OndemandCreator;
import org.seasar.framework.container.hotdeploy.OndemandCreatorContainer;
import org.seasar.framework.exception.ClassNotFoundRuntimeException;

abstract public class AbstractPageClassUpdater implements PageClassUpdater {

    private OndemandCreator[] creators_;

    private File classesDirectory_;
    private File webappDirectory_;

    public Response update(S2Container container, String componentName, String path) {

        String className = getClassName(container, componentName);
        if (className == null) {
            return null;
        }
        
        File templateFile = getTemplateFile(path);
        if (!templateFile.exists()) {
            return null;
        }

        File classFile = getClassFile(className);
        if (classFile.exists() && classFile.lastModified() >= templateFile.lastModified()) {
            return null;
        }
        
        // TODO ここから。あとcommon.diconにsetRequest,Responseするようなfilterを
        //作る？
        
        // TODO Auto-generated method stub
        return null;
    }

    File getTemplateFile(String path) {

        return new File(webappDirectory_, path);
    }

    File getClassFile(String className) {

        return new File(classesDirectory_, className.replace('.', '/') + ".class");
    }

    String getClassName(S2Container container, String componentName) {

        if (container.hasComponentDef(componentName)) {
            return container.getComponentDef(componentName).getComponentClass()
                .getName();
        } else if (creators_ != null) {
            for (int i = 0; i < creators_.length; i++) {
                try {
                    creators_[i].getComponentDef(container, componentName);
                } catch (ClassNotFoundRuntimeException ex) {
                    return ex.getCause().getMessage();
                }
            }
        }
        return null;
    }

    public void setOndemandCreatorContainer(OndemandCreatorContainer container) {

        if (container instanceof LocalOndemandCreatorContainer) {
            LocalOndemandCreatorContainer localContainer = (LocalOndemandCreatorContainer) container;
            creators_ = new OndemandCreator[localContainer.getCreatorSize()];
            for (int i = 0; i < creators_.length; i++) {
                creators_[i] = localContainer.getCreator(i);
            }
        } else {
            creators_ = new OndemandCreator[0];
        }
    }

    public void setClassesDirectoryPath(String classesDirectoryPath) {

        classesDirectory_ = new File(classesDirectoryPath);
    }

    public void setWebappDirectoryPath(String webappDirectoryPath) {

        webappDirectory_ = new File(webappDirectoryPath);
    }
}
