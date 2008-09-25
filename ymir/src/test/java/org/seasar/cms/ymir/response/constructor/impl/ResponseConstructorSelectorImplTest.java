package org.seasar.cms.ymir.response.constructor.impl;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;

import junit.framework.TestCase;

public class ResponseConstructorSelectorImplTest extends TestCase {

    private S2Container container_;

    private ResponseConstructorSelectorImpl target_;

    protected void setUp() throws Exception {

        super.setUp();

        container_ = S2ContainerFactory.create(getClass().getName().replace(
                '.', '/')
                + "_root.dicon");
        container_.init();
        target_ = (ResponseConstructorSelectorImpl) container_
                .getComponent(ResponseConstructorSelectorImpl.class);
    }

    /*
     * S2Containerの挙動のspikeに近いテスト。
     */
    public void testInitialize() throws Exception {

        assertNotNull(target_.getResponseConstructor(String.class));
    }
}
