package org.seasar.cms.wiki.plugin.singleton;

import org.seasar.cms.wiki.engine.WikiContext;
import org.seasar.cms.wiki.engine.impl.WikiEngineTestFramework;

public class DivPluginTest extends WikiEngineTestFramework {

	public void testBasic() {
		String actual = engine.evaluate("#div(a,b)\nhoge\n#divclose",
				new WikiContext());
		String expected = "<div id=\"a\" class=\"b\"><p>hoge</p></div>";
		assertWikiEquals(expected, actual);

		actual = engine
				.evaluate("#div(,b)\nhoge\n#divclose", new WikiContext());
		expected = "<div class=\"b\"><p>hoge</p></div>";
		assertWikiEquals(expected, actual);

		actual = engine.evaluate("#div(a)\nhoge\n#divclose", new WikiContext());
		expected = "<div id=\"a\"><p>hoge</p></div>";
		assertWikiEquals(expected, actual);
	}
}
