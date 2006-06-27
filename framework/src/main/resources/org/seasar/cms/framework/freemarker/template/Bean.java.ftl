<#if packageName != "">package ${packageName};</#if>

public class ${shortName} extends ${shortName}Base
{
    public ${shortName}()
    {
    }

    public ${shortName}(<#list propertyDescs as propertyDesc>${propertyDesc.typeDesc.typeString} ${propertyDesc.name}<#if propertyDesc_has_next>, </#if></#list>)
    {
        super(<#list propertyDescs as propertyDesc>${propertyDesc.name}<#if propertyDesc_has_next>, </#if></#list>);
    }
}
