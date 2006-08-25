package org.seasar.cms.pluggable.hotdeploy;

import org.seasar.framework.container.hotdeploy.HotdeployClassLoader;
import org.seasar.framework.container.hotdeploy.OndemandProject;
import org.seasar.framework.container.hotdeploy.impl.OndemandProjectImpl;

import junit.framework.TestCase;

public class SpikeTest extends TestCase {

    private static final String NAME = "org.seasar.cms.pluggable.hotdeploy.SpikeTest";

    private ClassLoader newClassLoader() {
        HotdeployClassLoader cl = new HotdeployClassLoader(getClass()
                .getClassLoader());
        OndemandProjectImpl project = new OndemandProjectImpl();
        project.setRootPackageName("org.seasar.cms.pluggable.hotdeploy");
        cl.setProjects(new OndemandProject[] { project });
        return cl;
    }

    public void testClassForNameAndLoadClass1() throws Exception {

        ProxyClassLoader pcl = new ProxyClassLoader(newClassLoader());
        Class clazz1 = Class.forName(NAME, false, pcl);
        Class clazz2 = pcl.loadClass(NAME);
        pcl.setClassLoader(newClassLoader());
        Class clazz3 = Class.forName(NAME, false, pcl);
        Class clazz4 = pcl.loadClass(NAME);

        assertSame("Class.forName()では、Proxyが持つClassLoaderを変えても返されるクラスは同一",
                clazz3, clazz1);
        assertNotSame("loadClass()では、Proxyが持つClassLoaderを変えると返されるクラスが変わる",
                clazz4, clazz2);
    }

    public void testClassForNameAndLoadClass2() throws Exception {

        ProxyClassLoader pcl = new ProxyClassLoader(newClassLoader());
        ProxyClassLoader pclpcl1 = new ProxyClassLoader(pcl);
        ProxyClassLoader pclpcl2 = new ProxyClassLoader(pcl);
        Class clazz1 = Class.forName(NAME, false, pclpcl1);
        pcl.setClassLoader(newClassLoader());
        Class clazz2 = Class.forName(NAME, false, pclpcl2);

        assertNotSame("指定するClassLoaderProxyが違えばキャッシュされない", clazz2, clazz1);
    }
}
