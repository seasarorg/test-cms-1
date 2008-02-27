package org.seasar.cms.classbuilder.util;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;

// TODO S2.3-PluggableのContainerUtilsと共通化したい。
public class ContainerUtils {
    protected ContainerUtils() {
    }

    public static ComponentDef[] findLocalComponentDefs(S2Container container,
            Object componentKey) {
        ComponentDef[] componentDefs = container
                .findComponentDefs(componentKey);
        if (componentDefs.length > 0
                && componentDefs[0].getContainer() != container) {
            // 見つかったComponentDefの束（親コンテナはどれも同じはず）の親が検索元のコンテナ
            // ではない場合は、ローカルにはcomponentKeyに合致するComponentDefは存在しな
            // かったということ。
            componentDefs = new ComponentDef[0];
        }
        return componentDefs;
    }
}
