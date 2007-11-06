package org.seasar.cms.doxia;

import java.io.Reader;

import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;

import org.seasar.cms.wiki.engine.WikiEngine;
import org.seasar.cms.wiki.engine.impl.WikiEngineImpl;


/**
 * @plexus.component role="org.apache.maven.doxia.parser.Parser"
 * role-hint="tgwiki"
 */
public class WikiParser
    implements Parser
{

    private WikiEngine engine = WikiEngineImpl.getInstance();


    public void parse(Reader reader, Sink sink)
        throws ParseException
    {

        sink.head();

        sink.head_();
        sink.body();

        String body = engine.evaluate(reader);
        sink.rawText(body);

        sink.body_();
    }
}
