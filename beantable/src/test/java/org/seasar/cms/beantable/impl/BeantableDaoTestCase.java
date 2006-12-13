package org.seasar.cms.beantable.impl;

import java.io.File;

import org.seasar.cms.database.identity.Identity;
import org.seasar.cms.database.identity.impl.H2Identity;
import org.seasar.extension.unit.S2TestCase;
import org.seasar.framework.util.ResourceUtil;

abstract public class BeantableDaoTestCase<T> extends S2TestCase {

    private Identity identity_;

    private BeantableDaoBase<T> target_;

    @SuppressWarnings("unchecked")
    protected BeantableImpl<T> newBeantable() {

        BeantableImpl<T> beantable = (BeantableImpl<T>) getComponent(BeantableImpl.class);
        beantable.setBeanClass(getDtoClass());
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
        include(getDiconPath());
    }

    abstract protected String getDiconPath();

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
    @SuppressWarnings("unchecked")
    protected void setUpAfterContainerInit() throws Throwable {
        super.setUpAfterContainerInit();

        identity_ = new H2Identity();
        identity_.setDataSource(getDataSource());
        identity_.startUsingDatabase();

        target_ = (BeantableDaoBase<T>) getComponent(getDaoClass());
        target_.setBeantable(newBeantable());
        target_.start();
    }

    abstract protected Class<?> getDaoClass();

    abstract protected Class<T> getDtoClass();

    @Override
    protected void tearDownBeforeContainerDestroy() throws Throwable {

        identity_.stopUsingDatabase();

        super.tearDownBeforeContainerDestroy();
    }
}
