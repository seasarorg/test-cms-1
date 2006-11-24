<#if classDesc.packageName != "">package ${classDesc.packageName};</#if>

public class ${classDesc.shortName}Base
{
<#list classDesc.propertyDescs as propertyDesc>
    protected ${propertyDesc.typeDesc.name} ${propertyDesc.name}_;

</#list>

    public ${classDesc.shortName}Base()
    {
    }

    public ${classDesc.shortName}Base(<#list classDesc.propertyDescs as propertyDesc>${propertyDesc.typeDesc.name} ${propertyDesc.name}<#if propertyDesc_has_next>, </#if></#list>)
    {
<#list classDesc.propertyDescs as propertyDesc>
        ${propertyDesc.name}_ = ${propertyDesc.name};
</#list>
    }
<#list classDesc.propertyDescs as propertyDesc>
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
