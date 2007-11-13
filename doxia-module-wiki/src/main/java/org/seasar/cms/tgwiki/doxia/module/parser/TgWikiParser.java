package org.seasar.cms.tgwiki.doxia.module.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.Locale;

import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;


import org.seasar.cms.wiki.engine.WikiEngine;
import org.seasar.cms.wiki.engine.impl.WikiEngineImpl;


/**
 * Parse a tgwiki document and emit events into the specified doxia
 * Sink.
 *
 * @author SOMEDA Takashi
 * @plexus.component role="org.apache.maven.doxia.parser.Parser"
 * role-hint="tgwiki"
 */
public class TgWikiParser
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
