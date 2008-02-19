package org.seasar.cms.pluggable.impl;

import org.seasar.framework.container.autoregister.AbstractAutoRegister;

public class MockAutoRegister extends AbstractAutoRegister {
    private int count_ = 0;

    public void registerAll() {
        count_++;
    }

    public int getCount() {
        return count_;
    }
}
