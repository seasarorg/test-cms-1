<#if classDesc.packageName != "">package ${classDesc.packageName};</#if>

import java.util.List;

public interface ${classDesc.shortName}
{
    Class BEAN = ${entityMetaData.beanTypeDesc.shortName}.class;

    ${entityMetaData.beanTypeDesc.shortName}[] selectAll();

    int insert(${entityMetaData.beanTypeDesc.shortName} ${entityMetaData.beanTypeDesc.instanceName});

    int update(${entityMetaData.beanTypeDesc.shortName} ${entityMetaData.beanTypeDesc.instanceName});

    int delete(${entityMetaData.beanTypeDesc.shortName} ${entityMetaData.beanTypeDesc.instanceName});
}
