<#if classDesc.packageName != "">package ${classDesc.packageName};</#if>

public class ${classDesc.shortName} extends ${classDesc.shortName}Base
{
<#list classDesc.methodDescs as methodDesc><#if methodDesc.body?exists>

    public ${methodDesc.returnTypeDesc.name} ${methodDesc.name}()
    {
        ${methodDesc.body?default("")}
    }
</#if></#list>
}
