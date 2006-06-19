package org.seasar.cms.framework.creator.impl;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.creator.impl.AbstractUpdateAction.Parameter;
import org.seasar.cms.framework.impl.RequestImpl;

public class AbstractUpdateActionTest extends TestCase {

    public void testGetParameters() throws Exception {

        RequestImpl request = new RequestImpl();
        Map parameterMap = new LinkedHashMap();
        parameterMap.put("test", new String[] { "value1", "value2" });
        parameterMap.put("__cms__task", new String[] { "value" });
        parameterMap.put("__cms__template", new String[] { "value" });
        parameterMap.put("test2", new String[] { "value3" });
        request.setParameterMap(parameterMap);

        Parameter[] parameters = new AbstractUpdateAction(null) {

            public Response act(Request request, String className,
                File sourceFile, File templateFile) {
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
