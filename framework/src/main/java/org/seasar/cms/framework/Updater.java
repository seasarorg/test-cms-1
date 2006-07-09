package org.seasar.cms.framework;

public interface Updater {

    Response update(String path, String method, Request request);
}
