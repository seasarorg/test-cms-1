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
}
