package org.seasar.cms.doxia;

import org.apache.maven.doxia.site.module.AbstractSiteModule;

/**
 * @plexus.component role="org.apache.maven.doxia.site.module.SiteModule"
 * role-hint="tgwiki"
 */
public class WikiSiteModule extends AbstractSiteModule
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
