package org.seasar.cms.ymir;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class PathTest extends TestCase {

    public void test_長さ2以上の配列をパラメタ値として持つmapを指定した場合でも正しくクエリ文字列が生成できること()
            throws Exception {

        Map map = new HashMap();
        map.put("a", new String[] { "A", "B" });
        map.put("c", new String[] { "C", "D" });

        Path target = new Path("/path/to/page", map);

        assertEquals("/path/to/page?a=A&a=B&c=C&c=D", target.asString());
    }
}
