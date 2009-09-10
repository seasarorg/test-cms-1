package org.seasar.cms.classbuilder.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.seasar.cms.classbuilder.util.S2ContainerBuilderUtils;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.factory.XmlS2ContainerBuilder;
import org.seasar.framework.xml.SaxHandlerParser;
import org.seasar.framework.xml.TagHandlerContext;

public class RedefinableXmlS2ContainerBuilder extends XmlS2ContainerBuilder {
    static final String PARAMETER_BUILDER = "builder";

    static final String PARAMETER_BASEPATH = "originalPath";

    public static final String DELIMITER = "+";

    private static final String NAME_ADDITIONAL = "+";

    private static final ThreadLocal<LinkedList<String>> basePathStack_ = new ThreadLocal<LinkedList<String>>();

    public RedefinableXmlS2ContainerBuilder() {
        rule
                .addTagHandler("/components",
                        new RedefinableComponentsTagHandler());
        rule.addTagHandler("component", new RedefinableComponentTagHandler());
    }

    @Override
    protected SaxHandlerParser createSaxHandlerParser(S2Container parent,
            String path) {
        SaxHandlerParser parser = super.createSaxHandlerParser(parent, path);
        TagHandlerContext context = parser.getSaxHandler()
                .getTagHandlerContext();
        context.addParameter(PARAMETER_BUILDER, this);
        context.addParameter(PARAMETER_BASEPATH, getCurrentBasePath());
        return parser;
    }

    @Override
    protected S2Container parse(S2Container parent, String path) {
        pushPath(path);
        try {
            S2Container container = super.parse(parent, path);

            mergeContainers(container, path, true);

            return container;
        } finally {
            popPath(path);
        }
    }

    private void popPath(String path) {
        if (path.equals(getCurrentBasePath())) {
            LinkedList<String> pathStack = basePathStack_.get();
            pathStack.removeFirst();
            if (pathStack.isEmpty()) {
                basePathStack_.set(null);
            }
        }
    }

    private String getCurrentBasePath() {
        LinkedList<String> pathStack = basePathStack_.get();
        if (pathStack == null || pathStack.isEmpty()) {
            return null;
        }
        return pathStack.peek();
    }

    private void pushPath(String path) {
        LinkedList<String> pathStack = basePathStack_.get();
        if (pathStack == null) {
            pathStack = new LinkedList<String>();
            basePathStack_.set(pathStack);
        }
        if (isBasePath(path)) {
            pathStack.addFirst(path);
        }
    }

    private boolean isBasePath(String path) {
        // 厳密には「～+.dicon」形式もbaseでないと判定しなければいけないが、「～+.dicon」は他のリソースと別の仕組みで扱われている
        // ため、このロジックを通ることはない。そのためここで「～+.dicon」形式の判定を行なわなくても問題ない。
        if (path == null) {
            return true;
        }
        return path.indexOf(DELIMITER + NAME_ADDITIONAL) < 0
                && path.indexOf(NAME_ADDITIONAL + DELIMITER) < 0;
    }

    protected void mergeContainers(S2Container container, String path,
            boolean addToTail) {
        Set<URL> additionalURLSet = gatherAdditionalDiconURLs(path, addToTail);
        for (Iterator<URL> itr = additionalURLSet.iterator(); itr.hasNext();) {
            String url = itr.next().toExternalForm();
            if (S2ContainerBuilderUtils.resourceExists(url, this)) {
                S2ContainerBuilderUtils.mergeContainer(container,
                        S2ContainerFactory.create(url));
            }
        }
    }

    protected Set<URL> gatherAdditionalDiconURLs(String path, boolean addToTail) {
        String[] additionalDiconPaths = constructAdditionalDiconPaths(path,
                addToTail);
        Set<URL> urlSet = new LinkedHashSet<URL>();
        for (int i = 0; i < additionalDiconPaths.length; i++) {
            URL[] urls = S2ContainerBuilderUtils
                    .getResourceURLs(additionalDiconPaths[i]);
            for (int j = 0; j < urls.length; j++) {
                urlSet.add(urls[j]);
            }
        }
        return urlSet;
    }

    protected String[] constructAdditionalDiconPaths(String path,
            boolean addToTail) {
        int delimiter = path.lastIndexOf(DELIMITER);
        int slash = path.lastIndexOf('/');
        if (delimiter >= 0 && delimiter > slash) {
            // 訳が分からなくならないよう、現状ではリソース名に「+」が含まれていない場合だけ
            // 特別な処理を行なうようにしている。
            return new String[0];
        }

        List<String> pathList = new ArrayList<String>();
        String body;
        String suffix;
        int dot = path.lastIndexOf('.');
        if (dot < 0) {
            body = path;
            suffix = "";
        } else {
            body = path.substring(0, dot);
            suffix = path.substring(dot);
        }
        StringBuilder sb = new StringBuilder();
        if (!addToTail) {
            sb.append(NAME_ADDITIONAL).append(DELIMITER);
        }
        sb.append(body);
        if (addToTail) {
            sb.append(DELIMITER).append(NAME_ADDITIONAL);
        }
        sb.append(suffix);
        String additionalPath = sb.toString();
        String additionalResourcePath = S2ContainerBuilderUtils
                .fromURLToResourcePath(additionalPath);
        if (additionalResourcePath != null) {
            // パスがJarのURLの場合はURLをリソースパスに変換した上で作成したパスを候補に含める。
            pathList.add(additionalResourcePath);
        }
        pathList.add(additionalPath);
        return pathList.toArray(new String[0]);
    }
}
