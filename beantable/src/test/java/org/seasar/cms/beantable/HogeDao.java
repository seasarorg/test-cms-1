package org.seasar.cms.beantable;

import java.util.Map;

import org.seasar.cms.beantable.impl.Hoge;
import org.seasar.dao.annotation.tiger.Sql;

public interface HogeDao {

    Hoge[] getDtos();

    Hoge getDtoById(Integer id);

    Hoge getDtoByIdAndUsername(Integer id, String username);

    Hoge[] getDtosByUsername(Map<String, Object> map);

    Hoge getDtoByUsername(Map<String, Object> map);

    void insert(Hoge hoge);

    int update(Hoge hoge);

    int updateById(Hoge hoge, Integer id);

    int delete();

    int deleteById(Integer id);

    Number getDtoCount();

    Number[] getIds();

    String getUsername();

    String[] getUsernames();

    Object getUsername2();

    Object[] getUsernames2();

    @Sql("SELECT username FROM hoge")
    String getUsername3();

    @Sql("SELECT COUNT(*) FROM hoge")
    boolean exists();
}
