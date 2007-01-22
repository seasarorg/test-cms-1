package org.seasar.cms.ymir;

import junit.framework.TestCase;

import org.seasar.cms.ymir.convention.YmirNamingConvention;
import org.seasar.cms.ymir.web._RootPage;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.creator.PageCreator;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.kvasir.util.el.VariableResolver;

public class YmirConventionTest extends TestCase {

    /*
     * 標準のマッピングをテストします。
     */
    public void testMapping() throws Exception {

        S2Container container = S2ContainerFactory.create("mapping.dicon");
        PathMappingProvider provider = (PathMappingProvider) container
                .getComponent(PathMappingProvider.class);
        PathMapping[] mappings = provider.getPathMappings();

        MatchedPathMapping matched = match("", "GET", mappings);
        assertNotNull(matched);
        assertEquals("_RootPage", matched.getComponentName());
        assertEquals("_get", matched.getActionName());

        matched = match("/_Root", "GET", mappings);
        assertNull(matched);

        matched = match("/article.html", "GET", mappings);
        assertNotNull(matched);
        assertEquals("articlePage", matched.getComponentName());
        assertEquals("_get", matched.getActionName());

        matched = match("/article/update.html", "POST", mappings);
        assertNotNull(matched);
        assertEquals("articlePage", matched.getComponentName());
        assertEquals("_updateByPost", matched.getActionName());

        matched = match("/article/_update.html", "POST", mappings);
        assertNull(matched);
    }

    public void testFromComponentNameToClassName() throws Exception {

        YmirNamingConvention convention = new YmirNamingConvention();
        convention.addRootPackageName("org.seasar.cms.ymir");
        PageCreator creator = new PageCreator(convention);
        assertEquals(_RootPage.class, creator.getNamingConvention()
                .fromComponentNameToClass("_RootPage"));
    }

    private MatchedPathMapping match(String path, String method,
            PathMapping[] mappings) {
        for (int i = 0; i < mappings.length; i++) {
            VariableResolver resolver = mappings[i].match(path, method);
            if (resolver != null) {
                return new MatchedPathMapping(mappings[i], resolver);
            }
        }
        return null;
    }
}
