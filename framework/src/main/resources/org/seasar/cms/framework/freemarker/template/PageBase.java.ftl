<#if packageName != "">package ${packageName};</#if>

abstract public class ${shortName}Base<#if superclassName?exists> extends ${superclassName}</#if>
{
<#list propertyDescs as propertyDesc>
    protected ${propertyDesc.typeDesc.typeString} ${propertyDesc.name}_;

</#list>
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
<#list methodDescs as methodDesc>

    public ${methodDesc.returnTypeDesc.typeString} ${methodDesc.name}()
    {
        <#if methodDesc.returnTypeDesc.typeString != "void">return null;</#if>
    }
</#list>
}
