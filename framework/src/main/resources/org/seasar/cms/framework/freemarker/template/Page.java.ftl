<#if packageName != "">package ${packageName};</#if>

public class ${shortName} extends ${shortName}Base
{
<#list methodDescs as methodDesc><#if methodDesc.body?exists>

    public ${methodDesc.returnTypeDesc.typeString} ${methodDesc.name}()
    {
        ${methodDesc.body?default("")}
    }
</#if></#list>
}
