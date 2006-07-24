package org.seasar.cms.ymir.extension.creator.action.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.action.impl.AbstractUpdateAction.Parameter;
import org.seasar.cms.ymir.impl.RequestImpl;

public class AbstractUpdateActionTest extends TestCase {

    public void testGetParameters() throws Exception {

        RequestImpl request = new RequestImpl();
        Map<String, String[]> parameterMap = new LinkedHashMap<String, String[]>();
        parameterMap.put("test", new String[] { "value1", "value2" });
        parameterMap.put("__ymir__task", new String[] { "value" });
        parameterMap.put("__ymir__template", new String[] { "value" });
        parameterMap.put("test2", new String[] { "value3" });
        request.setParameterMap(parameterMap);

        Parameter[] parameters = new AbstractUpdateAction(null) {

            public Response act(Request request, PathMetaData pathMetaData) {
                return null;
            }
        }.getParameters(request);

        assertEquals(3, parameters.length);
        assertEquals("test", parameters[0].getName());
        assertEquals("value1", parameters[0].getValue());
        assertEquals("test", parameters[1].getName());
        assertEquals("value2", parameters[1].getValue());
        assertEquals("test2", parameters[2].getName());
        assertEquals("value3", parameters[2].getValue());
    }
}
