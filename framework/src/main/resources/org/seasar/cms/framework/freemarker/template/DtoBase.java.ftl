<#if packageName != "">package ${packageName};</#if>

abstract public class ${shortName}Base
{
<#list propertyDescs as propertyDesc>
    protected ${propertyDesc.typeDesc.typeString} ${propertyDesc.name}_;

</#list>

    public ${shortName}Base()
    {
    }

    public ${shortName}Base(<#list propertyDescs as propertyDesc>${propertyDesc.typeDesc.typeString} ${propertyDesc.name}<#if propertyDesc_has_next>, </#if></#list>)
    {
<#list propertyDescs as propertyDesc>
        ${propertyDesc.name}_ = ${propertyDesc.name};
</#list>
    }
<#list propertyDescs as propertyDesc>
<#if propertyDesc.readable>

    public ${propertyDesc.typeDesc.typeString} <#if propertyDesc.typeDesc.typeString == "boolean">is<#else>get</#if>${propertyDesc.name?cap_first}()
    {
        return ${propertyDesc.name}_;
    }
</#if>
<#if propertyDesc.writable>

    public void set${propertyDesc.name?cap_first}(${propertyDesc.typeDesc.typeString} ${propertyDesc.name})
    {
        ${propertyDesc.name}_ = ${propertyDesc.name};
    }
</#if>
</#list>
}
