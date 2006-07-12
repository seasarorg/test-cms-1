package org.seasar.cms.database.identity.impl;

import org.seasar.cms.database.identity.Identity;
import org.seasar.extension.unit.S2TestCase;

public class IdentitySelectorImplTest extends S2TestCase {

    private IdentitySelectorImpl target_;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        include("META-INF/s2container/components.dicon");
    }

    @Override
    protected void setUpAfterContainerInit() throws Throwable {
        super.setUpAfterContainerInit();
        target_ = (IdentitySelectorImpl) getContainer().getComponent(
            IdentitySelectorImpl.class);
    }

    public void testSelectIdentity() throws Exception {

        Identity actual = target_.getIdentity();
        assertNotNull(actual);
        assertSame(HsqlIdentity.class, actual.getClass());
    }
}
