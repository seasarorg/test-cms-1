package org.seasar.cms.ymir.impl;

import org.seasar.cms.ymir.PathMapping;
import org.seasar.cms.ymir.PathMappingProvider;

public class PathMappingProviderImpl implements PathMappingProvider {

    private PathMapping[] pathMappings_;

    public PathMapping[] getPathMappings() {

        return pathMappings_;
    }

    public void setPathMappings(PathMapping[] pathMappings) {

        pathMappings_ = pathMappings;
    }
}
