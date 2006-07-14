package org.seasar.cms.beantable.impl;

import org.seasar.cms.beantable.Beantable;
import org.seasar.cms.database.identity.Identity;
import org.seasar.cms.database.identity.impl.HsqlIdentity;
import org.seasar.extension.unit.S2TestCase;

public class BeantableImplTest extends S2TestCase {

    private Identity identity_;

    protected Beantable newBeantable(Class beanClass) {

        BeantableImpl beantable = new BeantableImpl(beanClass);
        beantable.setIdentity(identity_);
        beantable.setDataSource(getDataSource());
        return beantable;
    }

    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        include("j2ee.dicon");
    }

    @Override
    protected void setUpAfterContainerInit() throws Throwable {
        super.setUpAfterContainerInit();

        identity_ = new HsqlIdentity();
        identity_.setDataSource(getDataSource());
        identity_.startUsingDatabase();
    }

    @Override
    protected void tearDownBeforeContainerDestroy() throws Throwable {

        identity_.stopUsingDatabase();

        super.tearDownBeforeContainerDestroy();
    }

    public void testActivate() throws Exception {

        try {
            newBeantable(Hoge.class).activate();
        } catch (Throwable t) {
            fail("正常にactivate()できること");
        }
    }

    public void testCreateTable() throws Exception {

        Beantable beanTable = newBeantable(Hoge.class);
        beanTable.activate();
        beanTable.dropTable(true);

        assertTrue(beanTable.createTable());
        assertFalse("テーブルが存在する場合は何もしないこと", beanTable.createTable());
    }

    public void testCreateTable2() throws Exception {

        Beantable beanTable = newBeantable(Hoge2.class);
        beanTable.activate();
        beanTable.dropTable(true);

        try {
            beanTable.createTable();
        } catch (Throwable t) {
            t.printStackTrace();
            fail("IDカラムがシーケンスに対応付けられている場合に正常にテーブルを生成できること");
        }
    }

    public void testDropTable() throws Exception {

        Beantable beanTable = newBeantable(Hoge.class);
        beanTable.activate();
        beanTable.createTable(true);

        assertTrue(beanTable.dropTable());
        assertFalse("テーブルが存在しない場合は何もしないこと", beanTable.dropTable());
    }
}
