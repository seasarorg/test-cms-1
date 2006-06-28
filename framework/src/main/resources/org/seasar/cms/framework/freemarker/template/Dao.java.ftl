<#if classDesc.packageName != "">package ${classDesc.packageName};</#if>

import java.util.List;

public interface ${classDesc.shortName}
{
    Class BEAN = ${entityMetaData.beanClassDesc.shortName}.class;

    List select();

    int insert(${entityMetaData.beanClassDesc.shortName} ${entityMetaData.beanClassDesc.instanceName});

    int update(${entityMetaData.beanClassDesc.shortName} ${entityMetaData.beanClassDesc.instanceName});

    int delete(${entityMetaData.beanClassDesc.shortName} ${entityMetaData.beanClassDesc.instanceName});
}
