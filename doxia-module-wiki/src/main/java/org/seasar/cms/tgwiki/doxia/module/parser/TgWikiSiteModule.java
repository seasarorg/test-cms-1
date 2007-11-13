package org.seasar.cms.tgwiki.doxia.module.parser;

import org.apache.maven.doxia.site.module.AbstractSiteModule;


/**
 * @author SOMEDA Takashi
 * @plexus.component role="org.apache.maven.doxia.site.module.SiteModule"
 * role-hint="tgwiki"
 */
public class TgWikiSiteModule extends AbstractSiteModule
{
    public String getSourceDirectory()
    {
        
        return "tgwiki";
    }


    public String getExtension()
    {
        return "txt";
    }


    public String getParserId()
    {
        return "tgwiki";
    }
}
