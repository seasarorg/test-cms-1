<#if packageName != "">package ${packageName};</#if>

abstract public class ${shortName}Base<#if superclassName?exists> extends ${superclassName}</#if>
{
<#list propertyDescs as propertyDesc>
    protected ${propertyDesc.typeDesc.name} ${propertyDesc.name}_;

</#list>
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
<#list methodDescs as methodDesc>

    public ${methodDesc.returnTypeDesc.name} ${methodDesc.name}()
    {
        <#if methodDesc.returnTypeDesc.name != "void">return null;</#if>
    }
</#list>
}
