<#if packageName != "">package ${packageName};</#if>

public interface ${shortName}
{
<#list methodDescs as methodDesc>

    ${methodDesc.returnTypeDesc.typeString} ${methodDesc.name}(<#list methodDesc.parameterTypeDescs as parameterTypeDesc>${parameterTypeDesc.typeString} ${parameterTypeDesc.argumentNameString}<#if parameterTypeDesc_has_next>, </#if></#list>);
</#list>
}
