package org.seasar.cms.pluggable.util;

import org.seasar.cms.pluggable.hotdeploy.HotdeployEventListener;
import org.seasar.framework.util.ArrayUtil;

public class HotdeployEventUtils {
    private static HotdeployEventListener[] listeners_ = new HotdeployEventListener[0];

    protected HotdeployEventUtils() {
    }

    public static void add(HotdeployEventListener listener) {
        listeners_ = (HotdeployEventListener[]) ArrayUtil.add(listeners_,
                listener);
    }

    public static void start() {
        for (int i = 0; i < listeners_.length; i++) {
            listeners_[i].start();
        }
    }

    public static void stop() {
        for (int i = 0; i < listeners_.length; i++) {
            listeners_[i].stop();
        }
    }
}
