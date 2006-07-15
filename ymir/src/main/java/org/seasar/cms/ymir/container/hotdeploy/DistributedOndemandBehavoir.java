package org.seasar.cms.ymir.container.hotdeploy;

import org.seasar.cms.ymir.container.ContainerUtils;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.impl.S2ContainerBehavior.DefaultProvider;

public class DistributedOndemandBehavoir extends DefaultProvider {

    protected ComponentDef getComponentDef(S2Container container, Object key) {

        ComponentDef cd = super.getComponentDef(container, key);
        if (cd != null) {
            return cd;
        }
        return findComponentDefFromCreatorContainers(container, key);
    }

    protected ComponentDef findComponentDefFromCreatorContainers(
        S2Container container, Object key) {

        // FIXME findAllComponents()をつかうべき？
        LocalOndemandCreatorContainer[] localOndemandCreatorContainers = (LocalOndemandCreatorContainer[]) ContainerUtils
            .findDescendantComponents(container, LocalOndemandCreatorContainer.class);
        for (int i = 0; i < localOndemandCreatorContainers.length; i++) {
            ComponentDef cd = localOndemandCreatorContainers[i]
                .findComponentDef(key);
            if (cd != null) {
                return cd;
            }
        }
        return null;
    }
}
