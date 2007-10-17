package org.seasar.cms.wiki.factory.impl;

import java.util.List;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.factory.WikiBodyFactory;

public class WikiBodyFactoryImpl implements WikiBodyFactory {

	private static final String TAG_SPAN_HIGHLIGHT = "<span class=\"highlight\">%s</span>";

	public String eval(WikiContext context, String body) {
		if (!"html".equals(context.getNamespace())) {
			return body;
		}

		List<String> keywords = context.get(KEY);

		if (keywords == null) {
			return body;
		}

		for (String keyword : keywords) {
			String tag = String.format(TAG_SPAN_HIGHLIGHT, keyword);
			body = body.replaceAll(keyword, tag);
		}
		return body;
	}
}
