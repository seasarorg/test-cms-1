<#if packageName != "">package ${packageName};</#if>

abstract public class ${shortName}Base
{
<#list propertyDescs as propertyDesc>
    protected ${propertyDesc.typeString} ${propertyDesc.name}_;

</#list>
<#list propertyDescs as propertyDesc>
<#if propertyDesc.readable>

    public ${propertyDesc.typeString} get${propertyDesc.name?cap_first}()
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
}
