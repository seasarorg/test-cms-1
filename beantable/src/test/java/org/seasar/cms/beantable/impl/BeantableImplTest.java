package org.seasar.cms.beantable.impl;

import java.io.File;
import java.util.HashMap;

import org.seasar.cms.beantable.Beantable;
import org.seasar.cms.database.identity.Identity;
import org.seasar.cms.database.identity.impl.H2Identity;
import org.seasar.extension.unit.S2TestCase;
import org.seasar.framework.util.ResourceUtil;

public class BeantableImplTest extends S2TestCase {

    private Identity identity_;

    protected <T> BeantableImpl<T> newBeantable(Class<T> beanClass) {

        BeantableImpl<T> beantable = new BeantableImpl<T>(beanClass);
        beantable.setIdentity(identity_);
        beantable.setDataSource(getDataSource());
        return beantable;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        File dbDir = new File(ResourceUtil.getBuildDir(getClass())
                .getCanonicalPath(), "h2");
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

        identity_ = new H2Identity();
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

    public void testAdjust_Boolean型に正しくadjustできること() throws Exception {

        BeantableImpl<Hoge> target = newBeantable(Hoge.class);

        Boolean value = Boolean.TRUE;
        assertSame(value, target.adjust(value, Boolean.TYPE));
        assertEquals(Boolean.TRUE, target.adjust(Long.valueOf(1), Boolean.TYPE));
        assertEquals(Boolean.FALSE, target.adjust(null, Boolean.TYPE));

        assertSame(value, target.adjust(value, Boolean.class));
        assertEquals(Boolean.TRUE, target
                .adjust(Long.valueOf(1), Boolean.class));
        assertEquals(Boolean.FALSE, target.adjust(null, Boolean.class));
        assertNull(target.adjust(new HashMap(), Boolean.class));
    }

    public void testAdjust_Character型に正しくadjustできること() throws Exception {

        BeantableImpl<Hoge> target = newBeantable(Hoge.class);

        Character value = Character.valueOf((char) 1);
        assertSame(value, target.adjust(value, Character.TYPE));
        assertEquals(Character.valueOf((char) 1), target.adjust(
                Long.valueOf(1), Character.TYPE));
        assertEquals(Character.valueOf((char) 0), target.adjust(null,
                Character.TYPE));
        assertNull(target.adjust(new HashMap(), Character.TYPE));

        assertSame(value, target.adjust(value, Character.class));
        assertEquals(Character.valueOf((char) 1), target.adjust(
                Long.valueOf(1), Character.class));
        assertEquals(Character.valueOf((char) 0), target.adjust(null,
                Character.class));
        assertNull(target.adjust(new HashMap(), Character.class));
    }

    public void testAdjust_Short型に正しくadjustできること() throws Exception {

        BeantableImpl<Hoge> target = newBeantable(Hoge.class);

        Short value = Short.valueOf((short) 1);
        assertSame(value, target.adjust(value, Short.TYPE));
        assertEquals(Short.valueOf((short) 1), target.adjust(Long.valueOf(1),
                Short.TYPE));
        assertEquals(Short.valueOf((short) 0), target.adjust(null, Short.TYPE));
        assertNull(target.adjust(new HashMap(), Short.TYPE));

        assertSame(value, target.adjust(value, Short.class));
        assertEquals(Short.valueOf((short) 1), target.adjust(Long.valueOf(1),
                Short.class));
        assertEquals(Short.valueOf((short) 0), target.adjust(null, Short.class));
        assertNull(target.adjust(new HashMap(), Short.class));
    }

    public void testAdjust_Integer型に正しくadjustできること() throws Exception {

        BeantableImpl<Hoge> target = newBeantable(Hoge.class);

        Integer value = Integer.valueOf(1);
        assertSame(value, target.adjust(value, Integer.TYPE));
        assertEquals(Integer.valueOf(1), target.adjust(Long.valueOf(1),
                Integer.TYPE));
        assertEquals(Integer.valueOf(0), target.adjust(null, Integer.TYPE));
        assertNull(target.adjust(new HashMap(), Integer.TYPE));

        assertSame(value, target.adjust(value, Integer.class));
        assertEquals(Integer.valueOf(1), target.adjust(Long.valueOf(1),
                Integer.class));
        assertEquals(Integer.valueOf(0), target.adjust(null, Integer.class));
        assertNull(target.adjust(new HashMap(), Integer.class));
    }

    public void testAdjust_Long型に正しくadjustできること() throws Exception {

        BeantableImpl<Hoge> target = newBeantable(Hoge.class);

        Long value = Long.valueOf(1);
        assertSame(value, target.adjust(value, Long.TYPE));
        assertEquals(Long.valueOf(1), target.adjust(Long.valueOf(1), Long.TYPE));
        assertEquals(Long.valueOf(0), target.adjust(null, Long.TYPE));
        assertNull(target.adjust(new HashMap(), Long.TYPE));

        assertSame(value, target.adjust(value, Long.class));
        assertEquals(Long.valueOf(1), target.adjust(Integer.valueOf(1),
                Long.class));
        assertEquals(Long.valueOf(0), target.adjust(null, Long.class));
        assertNull(target.adjust(new HashMap(), Long.class));
    }

    public void testAdjust_Float型に正しくadjustできること() throws Exception {

        BeantableImpl<Hoge> target = newBeantable(Hoge.class);

        Float value = Float.valueOf(1);
        assertSame(value, target.adjust(value, Float.TYPE));
        assertEquals(Float.valueOf(1), target.adjust(Long.valueOf(1),
                Float.TYPE));
        assertEquals(Float.valueOf(0), target.adjust(null, Float.TYPE));
        assertNull(target.adjust(new HashMap(), Float.TYPE));

        assertSame(value, target.adjust(value, Float.class));
        assertEquals(Float.valueOf(1), target.adjust(Long.valueOf(1),
                Float.class));
        assertEquals(Float.valueOf(0), target.adjust(null, Float.class));
        assertNull(target.adjust(new HashMap(), Float.class));
    }

    public void testAdjust_Double型に正しくadjustできること() throws Exception {

        BeantableImpl<Hoge> target = newBeantable(Hoge.class);

        Double value = Double.valueOf(1);
        assertSame(value, target.adjust(value, Double.TYPE));
        assertEquals(Double.valueOf(1), target.adjust(Long.valueOf(1),
                Double.TYPE));
        assertEquals(Double.valueOf(0), target.adjust(null, Double.TYPE));
        assertNull(target.adjust(new HashMap(), Double.TYPE));

        assertSame(value, target.adjust(value, Double.class));
        assertEquals(Double.valueOf(1), target.adjust(Long.valueOf(1),
                Double.class));
        assertEquals(Double.valueOf(0), target.adjust(null, Double.class));
        assertNull(target.adjust(new HashMap(), Double.class));
    }
}
