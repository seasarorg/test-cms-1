<#if classDesc.packageName != "">package ${classDesc.packageName};</#if>


public interface ${classDesc.shortName}
{
    Class BEAN = ${entityMetaData.beanClassDesc.shortName}.class;

    ${entityMetaData.beanClassDesc.shortName}[] selectAll();

    int insert(${entityMetaData.beanClassDesc.shortName} ${entityMetaData.beanClassDesc.instanceName});

    int update(${entityMetaData.beanClassDesc.shortName} ${entityMetaData.beanClassDesc.instanceName});

    int delete(${entityMetaData.beanClassDesc.shortName} ${entityMetaData.beanClassDesc.instanceName});
}
