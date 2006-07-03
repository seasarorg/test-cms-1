<#if classDesc.packageName != "">package ${classDesc.packageName};</#if>

abstract public class ${classDesc.shortName}Base<#if classDesc.superclassName?exists> extends ${classDesc.superclassName}</#if>
{
<#list classDesc.propertyDescs as propertyDesc>
    protected ${propertyDesc.typeDesc.name} ${propertyDesc.name}_;

</#list>
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
<#list classDesc.methodDescs as methodDesc>

    public ${methodDesc.returnTypeDesc.name} ${methodDesc.name}(<#list methodDesc.parameterDescs as parameterTypeDesc>${parameterTypeDesc.name} ${parameterTypeDesc.argumentName}<#if parameterDesc_has_next>, </#if></#list>)
    {
        <#if methodDesc.evaluatedBody?exists>${methodDesc.evaluatedBody}<#elseif methodDesc.returnTypeDesc.name != "void">return ${methodDesc.returnTypeDesc.defaultValue};</#if>
    }
</#list>
}
