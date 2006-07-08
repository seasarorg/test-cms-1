package org.seasar.cms.beantable.impl;

import org.seasar.cms.beantable.BeanTable;
import org.seasar.cms.beantable.identity.IdentitySelector;
import org.seasar.extension.unit.S2TestCase;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.autoregister.AbstractAutoRegister;

public class BeanTableImplTest extends S2TestCase {

    protected BeanTable newBeanTable(Class beanClass) {

        BeanTable beanTable = (BeanTable) getContainer().getComponent(
            BeanTable.class);
        beanTable.setBeanClass(beanClass);
        return beanTable;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        include("META-INF/s2container/components.dicon");
        include("test.dicon");
    }

    @Override
    protected void setUpAfterContainerInit() throws Throwable {
        super.setUpAfterContainerInit();
        ((IdentitySelector) getContainer().getComponent(IdentitySelector.class))
            .getIdentity().startUsingDatabase();
    }

    void registerComponents(S2Container container) {

        Object[] registers = container
            .findComponents(AbstractAutoRegister.class);
        for (int i = 0; i < registers.length; i++) {
            ((AbstractAutoRegister) registers[i]).registerAll();
        }
    }

    @Override
    protected void tearDownBeforeContainerDestroy() throws Throwable {

        ((IdentitySelector) getContainer().getComponent(IdentitySelector.class))
            .getIdentity().stopUsingDatabase();

        super.tearDownBeforeContainerDestroy();
    }

    public void testActivate() throws Exception {

        try {
            newBeanTable(Hoge.class).activate();
        } catch (Throwable t) {
            fail("正常にactivate()できること");
        }
    }

    public void testCreateTable() throws Exception {

        BeanTable beanTable = newBeanTable(Hoge.class);
        beanTable.activate();
        beanTable.dropTable(true);

        assertTrue(beanTable.createTable());
        assertFalse("テーブルが存在する場合は何もしないこと", beanTable.createTable());
    }

    public void testCreateTable2() throws Exception {

        BeanTable beanTable = newBeanTable(Hoge2.class);
        beanTable.activate();
        beanTable.dropTable(true);

        try {
            beanTable.createTable();
        } catch (Throwable t) {
            fail("IDカラムがシーケンスに対応付けられている場合に正常にテーブルを生成できること");
        }
    }

    public void testDropTable() throws Exception {

        BeanTable beanTable = newBeanTable(Hoge.class);
        beanTable.activate();
        beanTable.createTable(true);

        assertTrue(beanTable.dropTable());
        assertFalse("テーブルが存在しない場合は何もしないこと", beanTable.dropTable());
    }
}
