<#if packageName != "">package ${packageName};</#if>

public interface ${shortName}
{
<#list methodDescs as methodDesc>

    ${methodDesc.returnTypeDesc.name} ${methodDesc.name}(<#list methodDesc.parameterTypeDescs as parameterTypeDesc>${parameterTypeDesc.name} ${parameterTypeDesc.argumentName}<#if parameterTypeDesc_has_next>, </#if></#list>);
</#list>
}
