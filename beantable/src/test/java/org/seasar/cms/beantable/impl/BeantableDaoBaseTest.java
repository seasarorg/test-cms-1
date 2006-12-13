package org.seasar.cms.beantable.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.beantable.HogeDao;

public class BeantableDaoBaseTest extends BeantableDaoTestCase<Hoge> {

    private HogeDao target_;

    @Override
    protected String getDiconPath() {
        return "BeantableDaoBaseTest.dicon";
    }

    @Override
    protected Class<?> getDaoClass() {
        return HogeDaoImpl.class;
    }

    @Override
    protected Class<Hoge> getDtoClass() {
        return Hoge.class;
    }

    public void testExecute1Tx() throws Exception {
        Hoge[] actual = target_.getDtos();

        assertNotNull(actual);
        assertEquals(0, actual.length);
    }

    public void testExecute2Tx() throws Exception {
        assertNull(target_.getDtoById(10000));
    }

    public void testExecute3Tx() throws Exception {
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user1", "comment1"));
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user2", "comment2"));
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user2", "comment3"));

        Hoge[] actual = target_.getDtos();

        assertEquals(3, actual.length);
        assertEquals("comment1", actual[0].getComment());
        assertEquals("comment2", actual[1].getComment());
        assertEquals("comment3", actual[2].getComment());
    }

    public void testExecute4Tx() throws Exception {
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user1", "comment1"));
        Hoge hoge = new Hoge(new Timestamp(System.currentTimeMillis()),
                "user2", "comment2");
        target_.insert(hoge);
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user2", "comment3"));

        assertEquals("comment2", target_.getDtoById(hoge.getId()).getComment());
    }

    public void testExecute5Tx() throws Exception {
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user1", "comment1"));
        Hoge hoge = new Hoge(new Timestamp(System.currentTimeMillis()),
                "user2", "comment2");
        target_.insert(hoge);
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user2", "comment3"));

        assertEquals("comment2", target_.getDtoByIdAndUsername(hoge.getId(),
                "user2").getComment());
    }

    public void testExecute6Tx() throws Exception {
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user1", "comment1"));
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user2", "comment2"));
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user2", "comment3"));

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("username", "user2");
        Hoge[] actual = target_.getDtosByUsername(map);
        assertNotNull(actual);
        assertEquals(2, actual.length);
        assertEquals("comment2", actual[0].getComment());
        assertEquals("comment3", actual[1].getComment());

        map.put("username", "user10");
        actual = target_.getDtosByUsername(map);
        assertNotNull(actual);
        assertEquals(0, actual.length);
    }

    public void testExecute7Tx() throws Exception {
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user1", "comment1"));
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user2", "comment2"));
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user2", "comment3"));

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("username", "user2");
        Hoge actual = target_.getDtoByUsername(map);
        assertNotNull(actual);
        assertEquals("comment2", actual.getComment());

        map.put("username", "user10");
        actual = target_.getDtoByUsername(map);
        assertNull(actual);
    }

    public void testExecute8Tx() throws Exception {
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user1", "comment1"));
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user2", "comment2"));

        Hoge changeSet = new Hoge();
        changeSet.setUsername("userA");

        int actual = target_.update(changeSet);

        assertEquals(2, actual);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("username", "userA");
        assertEquals(2, target_.getDtosByUsername(map).length);
    }

    public void testExecute9Tx() throws Exception {
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user1", "comment1"));
        Hoge hoge = new Hoge(new Timestamp(System.currentTimeMillis()),
                "user2", "comment2");
        target_.insert(hoge);

        Hoge changeSet = new Hoge();
        changeSet.setUsername("userA");

        int actual = target_.updateById(changeSet, hoge.getId());

        assertEquals(1, actual);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("username", "userA");
        assertEquals(hoge.getId(), target_.getDtosByUsername(map)[0].getId());
    }

    public void testExecute10Tx() throws Exception {
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user1", "comment1"));
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user2", "comment2"));

        int actual = target_.delete();

        assertEquals(2, actual);
        assertEquals(0, target_.getDtos().length);
    }

    public void testExecute11Tx() throws Exception {
        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user1", "comment1"));
        Hoge hoge = new Hoge(new Timestamp(System.currentTimeMillis()),
                "user2", "comment2");
        target_.insert(hoge);

        int actual = target_.deleteById(hoge.getId());

        assertEquals(1, actual);
        assertEquals(1, target_.getDtos().length);
    }

    public void testExecute_返り値がNumberでも正しく動作することTx() throws Exception {
        assertEquals(0, target_.getDtoCount());
    }

    public void testExecute_返り値がNumberの配列でも正しく動作することTx() throws Exception {
        assertEquals(0, target_.getIds().length);

        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user1", "comment1"));

        assertEquals(1, target_.getIds().length);
    }

    public void testExecute_返り値がStringでも正しく動作することTx() throws Exception {
        assertNull(target_.getUsername());

        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user1", "comment1"));

        assertEquals("user1", target_.getUsername());
    }

    public void testExecute_返り値がStringの配列でも正しく動作することTx() throws Exception {
        assertEquals(0, target_.getIds().length);

        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user1", "comment1"));

        assertEquals(1, target_.getUsernames().length);
    }

    public void testExecute_返り値がObjectでも正しく動作することTx() throws Exception {
        assertNull(target_.getUsername2());

        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user1", "comment1"));

        assertEquals("user1", target_.getUsername2());
    }

    public void testExecute_返り値がObjectの配列でも正しく動作することTx() throws Exception {
        assertEquals(0, target_.getIds().length);

        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user1", "comment1"));

        assertEquals(1, target_.getUsernames2().length);
    }

    public void testExecute_Sqlアノテーションを正しく読み取れることTx() throws Exception {
        assertNull(target_.getUsername3());

        target_.insert(new Hoge(new Timestamp(System.currentTimeMillis()),
                "user1", "comment1"));

        assertEquals("user1", target_.getUsername3());
    }
}
