package org.seasar.cms.mailsender.creator;

import org.seasar.framework.container.ComponentCustomizer;
import org.seasar.framework.container.creator.ComponentCreatorImpl;
import org.seasar.framework.container.deployer.InstanceDefFactory;
import org.seasar.framework.convention.NamingConvention;

public class MailsenderCreator extends ComponentCreatorImpl {
    private static final String NAME_SUFFIX_MAILSENDER = "Mailsender";

    public MailsenderCreator(NamingConvention namingConvention) {
        super(namingConvention);
        setNameSuffix(NAME_SUFFIX_MAILSENDER);
        setInstanceDef(InstanceDefFactory.SINGLETON);
        setEnableInterface(true);
        setEnableAbstract(true);
    }

    public ComponentCustomizer getMailsenderCustomizer() {
        return getCustomizer();
    }

    public void setMailsenderCustomizer(ComponentCustomizer customizer) {
        setCustomizer(customizer);
    }
}
