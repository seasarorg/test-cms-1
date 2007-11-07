package org.seasar.cms.doxia.module.parser;

import java.io.Reader;

import javax.print.attribute.standard.MediaSize.Engineering;

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

        engine.setProperty("class.core.table", "bodyTable");  
        engine.setProperty("class.core.tr.odd", "a");
        engine.setProperty("class.core.tr.even", "b");        
        sink.head();

        sink.head_();
        sink.body();

        
        String body = engine.evaluate(reader);
        sink.rawText(body);

        sink.body_();
    }
}
