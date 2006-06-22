<#if packageName != "">package ${packageName};</#if>

import java.util.List;

public interface ${shortName}
{
    Class BEAN = ${baseName}.class;

    List get();

    int insert(${baseName} ${baseName?uncap_first});

    int update(${baseName} ${baseName?uncap_first});

    int delete(${baseName} ${baseName?uncap_first});
}
