package org.seasar.cms.wiki.factory;

import org.seasar.cms.wiki.engine.WikiContext;

public interface WikiBodyFactory {

	public static final String KEY = WikiBodyFactory.class.getName() + ".KEY";

	public String eval(WikiContext context, String body);

}
