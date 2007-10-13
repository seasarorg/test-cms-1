package org.seasar.cms.wiki.engine.plugin;

import org.seasar.cms.wiki.engine.WikiContext;

public interface WikiBodyEvaluator {

	public static final String KEY = WikiBodyEvaluator.class.getName() + ".KEY";

	public String eval(WikiContext context, String body);

}
