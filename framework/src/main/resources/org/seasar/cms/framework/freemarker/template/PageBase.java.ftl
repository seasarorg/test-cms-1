<#if packageName != "">package ${packageName};</#if>

abstract public class ${shortName}Base<#if superclassName?exists> extends ${superclassName}</#if>
{
<#list propertyDescs as propertyDesc>
    protected ${propertyDesc.typeString} ${propertyDesc.name}_;

</#list>
<#list propertyDescs as propertyDesc>
<#if propertyDesc.readable>

    public ${propertyDesc.typeString} <#if propertyDesc.typeString == "boolean">is<#else>get</#if>${propertyDesc.name?cap_first}()
    {
        return ${propertyDesc.name}_;
    }
</#if>
<#if propertyDesc.writable>

    public void set${propertyDesc.name?cap_first}(${propertyDesc.typeString} ${propertyDesc.name})
    {
        ${propertyDesc.name}_ = ${propertyDesc.name};
    }
</#if>
</#list>
<#list methodDescs as methodDesc>

    public ${methodDesc.returnTypeString} ${methodDesc.name}()
    {
        <#if methodDesc.returnTypeString != "void">return null;</#if>
    }
</#list>
}
