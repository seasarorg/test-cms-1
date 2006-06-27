<#if packageName != "">package ${packageName};</#if>

abstract public class ${shortName}Base
{
<#list propertyDescs as propertyDesc>
    protected ${propertyDesc.typeDesc.name} ${propertyDesc.name}_;

</#list>

    public ${shortName}Base()
    {
    }

    public ${shortName}Base(<#list propertyDescs as propertyDesc>${propertyDesc.typeDesc.name} ${propertyDesc.name}<#if propertyDesc_has_next>, </#if></#list>)
    {
<#list propertyDescs as propertyDesc>
        ${propertyDesc.name}_ = ${propertyDesc.name};
</#list>
    }
<#list propertyDescs as propertyDesc>
<#if propertyDesc.readable>

    public ${propertyDesc.typeDesc.name} <#if propertyDesc.typeDesc.name == "boolean">is<#else>get</#if>${propertyDesc.name?cap_first}()
    {
        return ${propertyDesc.name}_;
    }
</#if>
<#if propertyDesc.writable>

    public void set${propertyDesc.name?cap_first}(${propertyDesc.typeDesc.name} ${propertyDesc.name})
    {
        ${propertyDesc.name}_ = ${propertyDesc.name};
    }
</#if>
</#list>
}
