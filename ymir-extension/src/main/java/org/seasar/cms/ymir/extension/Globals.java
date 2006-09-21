package org.seasar.cms.ymir.extension;

public interface Globals extends org.seasar.cms.ymir.Globals {

    String APPKEY_BEANTABLE_ENABLE = "extension.beantable.enable";

    String APPKEY_SOURCEDIRECTORY = "extension.sourceDirectory";

    String APPKEY_RESOURCESDIRECTORY = "extension.resourcesDirectory";

    String APPKEY_SOURCECREATOR_ENABLE = "extension.sourceCreator.enable";

    String APPKEY_SOURCECREATOR_USEFREYJARENDERCLASSES = "extension.sourceCreator.useFreyjaRenderClasses";

    String APPKEY_SOURCECREATOR_SUPERCLASS = "extension.sourceCreator.superclass";

    String APPKEYPREFIX_SOURCECREATOR_SUPERCLASS = APPKEY_SOURCECREATOR_SUPERCLASS
            + ".";
}
