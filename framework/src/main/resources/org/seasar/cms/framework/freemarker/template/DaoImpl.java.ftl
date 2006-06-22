<#if packageName != "">package ${packageName};</#if>

public interface ${shortName} extends ${shortName}Base
{
    Class BEAN = ${packageName}.${baseName}.class;
}
