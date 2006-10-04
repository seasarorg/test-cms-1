package org.seasar.cms.ymir;

public interface RedirectionPathResolver {

    String resolve(String path, Request request);
}
