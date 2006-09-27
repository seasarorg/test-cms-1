package org.seasar.cms.ymir;

public interface ResponsePathNormalizer {

    String normalize(String path, Request request);

    String normalizeForRedirection(String path, Request request);
}
