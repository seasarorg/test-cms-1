package org.seasar.cms.ymir.container.hotdeploy;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.hotdeploy.OndemandCreator;
import org.seasar.framework.container.hotdeploy.OndemandS2Container;
import org.seasar.framework.container.hotdeploy.impl.OndemandProjectImpl;

public class YmirOndemandProject extends OndemandProjectImpl {

    // TODO OndemandProject#getComponentClassName()を追加していただくわけには
    // いかないだろうか…。
    public ComponentDef getComponentDef(OndemandS2Container container,
        String componentName) {

        OndemandCreator[] creators = getCreators();
        String rootPackageName = getRootPackageName();

        for (int i = 0; i < creators.length; ++i) {
            OndemandCreator creator = creators[i];
            ComponentDef cd = creator.getComponentDef(container,
                rootPackageName, componentName);
            if (cd != null) {
                return cd;
            }
        }
        return null;
    }
}
