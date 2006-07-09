package org.seasar.cms.ymir;

public interface Updater {

    Response update(String path, String method, Request request);
}
