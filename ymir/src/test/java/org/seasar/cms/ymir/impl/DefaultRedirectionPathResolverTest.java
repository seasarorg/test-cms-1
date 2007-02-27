package org.seasar.cms.ymir.impl;

import junit.framework.TestCase;

import org.seasar.cms.ymir.mock.MockRequest;

public class DefaultRedirectionPathResolverTest extends TestCase {

    private DefaultRedirectionPathResolver target_ = new DefaultRedirectionPathResolver();

    public void testResolve1() throws Exception {

        assertEquals("/", target_.resolve("", new MockRequest()
                .setContextPath("")));
    }

    public void testResolve2() throws Exception {

        assertEquals("/hoe.do", target_.resolve("/hoe.do", new MockRequest()
                .setContextPath("")));
    }

    public void testResolve3() throws Exception {

        assertEquals("/?hoe=fuga", target_.resolve("?hoe=fuga",
                new MockRequest().setContextPath("")));
    }

    public void testResolve4() throws Exception {

        assertEquals("/;jsessionid=XXXX", target_.resolve(";jsessionid=XXXX",
                new MockRequest().setContextPath("")));
    }

    public void testResolve5() throws Exception {

        assertEquals("/context/", target_.resolve("", new MockRequest()
                .setContextPath("/context")));
    }

    public void testResolve6() throws Exception {

        assertEquals("/context/hoe.do", target_.resolve("/hoe.do",
                new MockRequest().setContextPath("/context")));
    }

    public void testResolve7() throws Exception {

        assertEquals("/context/?hoe=fuga", target_.resolve("?hoe=fuga",
                new MockRequest().setContextPath("/context")));
    }

    public void testResolve8() throws Exception {

        assertEquals("/context/;jsessionid=XXXX", target_.resolve(
                ";jsessionid=XXXX", new MockRequest()
                        .setContextPath("/context")));
    }

}
