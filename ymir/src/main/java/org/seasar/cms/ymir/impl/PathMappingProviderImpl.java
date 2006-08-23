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

    public void addPathMapping(String patternString,
            String componentNameTemplate, String actionNameTemplate,
            String pathInfoTemplate, String defaultPathTemplate) {

        pathMappings_ = addPathMapping(pathMappings_, new PathMappingImpl(
                patternString, componentNameTemplate, actionNameTemplate,
                pathInfoTemplate, defaultPathTemplate));
    }

    PathMapping[] addPathMapping(PathMapping[] patterns, PathMapping pattern) {

        PathMapping[] newPatterns;
        if (patterns == null) {
            newPatterns = new PathMapping[] { pattern };
        } else {
            newPatterns = new PathMapping[patterns.length + 1];
            System.arraycopy(patterns, 0, newPatterns, 0, patterns.length);
            newPatterns[patterns.length] = pattern;
        }
        return newPatterns;
    }
}
