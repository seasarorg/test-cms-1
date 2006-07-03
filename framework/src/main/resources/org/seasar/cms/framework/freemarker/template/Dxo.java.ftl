<#if classDesc.packageName != "">package ${classDesc.packageName};</#if>

import java.util.List;
import ${entityMetaData.beanClassDesc.name};
import ${entityMetaData.dtoClassDesc.name};

public interface ${classDesc.shortName}
{
    ${entityMetaData.dtoClassDesc.shortName} convert(${entityMetaData.beanClassDesc.shortName} ${entityMetaData.beanClassDesc.instanceName});

    ${entityMetaData.beanClassDesc.shortName} convert(${entityMetaData.dtoClassDesc.shortName} ${entityMetaData.dtoClassDesc.instanceName});

    void convert(${entityMetaData.dtoClassDesc.shortName} src, ${entityMetaData.beanClassDesc.shortName} dest);

    void convert(${entityMetaData.beanClassDesc.shortName} src, ${entityMetaData.dtoClassDesc.shortName} dest);

    ${entityMetaData.dtoClassDesc.shortName}[] convert(${entityMetaData.beanClassDesc.shortName}[] ${entityMetaData.beanClassDesc.instanceName}s);

    ${entityMetaData.dtoClassDesc.shortName}[] convert(List list);

    ${entityMetaData.beanClassDesc.shortName}[] convert(${entityMetaData.dtoClassDesc.shortName}[] ${entityMetaData.dtoClassDesc.instanceName}s);
}
