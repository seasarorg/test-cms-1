package org.seasar.cms.beantable.impl;

import java.io.File;

import org.seasar.cms.beantable.Beantable;
import org.seasar.cms.database.identity.Identity;
import org.seasar.cms.database.identity.impl.HsqlIdentity;
import org.seasar.extension.unit.S2TestCase;
import org.seasar.framework.util.ResourceUtil;

public class BeantableImplTest extends S2TestCase {

    private Identity identity_;

    protected <T> Beantable<T> newBeantable(Class<T> beanClass) {

        BeantableImpl<T> beantable = new BeantableImpl<T>(beanClass);
        beantable.setIdentity(identity_);
        beantable.setDataSource(getDataSource());
        return beantable;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        File dbDir = new File(ResourceUtil.getBuildDir(getClass())
                .getCanonicalPath(), "hsqldb");
        delete(dbDir);

        include("j2ee.dicon");
    }

    void delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                delete(files[i]);
            }
        }
        file.delete();
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

        Beantable<Hoge> beanTable = newBeantable(Hoge.class);
        beanTable.activate();
        beanTable.dropTable(true);

        assertTrue(beanTable.createTable());
        assertFalse("テーブルが存在する場合は何もしないこと", beanTable.createTable());
    }

    public void testCreateTable2() throws Exception {

        Beantable<Hoge2> beanTable = newBeantable(Hoge2.class);
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

        Beantable<Hoge> beanTable = newBeantable(Hoge.class);
        beanTable.activate();
        beanTable.createTable(true);

        assertTrue(beanTable.dropTable());
        assertFalse("テーブルが存在しない場合は何もしないこと", beanTable.dropTable());
    }
//
//    public void testSpike() throws Exception {
//
//        Connection con = null;
//        Statement st = null;
//        try {
//            con = getConnection();
//            st = con.createStatement();
//            st.executeQuery("CREATE TABLE fuga (ID INTEGER DEFAULT NEXT VALUE FOR hoe NOT NULL)");
//        } finally {
//            if (st != null) {
//                st.close();
//            }
//            if (con != null) {
//                con.close();
//            }
//        }
//    }
}
