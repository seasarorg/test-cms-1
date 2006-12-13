package org.seasar.cms.beantable.impl;

import org.seasar.cms.beantable.HogeDao;

abstract public class HogeDaoImpl extends BeantableDaoBase<Hoge> implements
        HogeDao {

    @Override
    protected Class<Hoge> getDtoClass() {
        return Hoge.class;
    }
}
