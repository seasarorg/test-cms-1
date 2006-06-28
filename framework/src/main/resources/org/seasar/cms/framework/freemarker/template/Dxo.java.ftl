<#if classDesc.packageName != "">package ${classDesc.packageName};</#if>

import java.util.List;
import ${entityMetaData.beanTypeDesc.name};
import ${entityMetaData.dtoTypeDesc.name};

public interface ${classDesc.shortName}
{
    ${entityMetaData.dtoTypeDesc.shortName} convert(${entityMetaData.beanTypeDesc.shortName} ${entityMetaData.beanTypeDesc.instanceName});

    ${entityMetaData.beanTypeDesc.shortName} convert(${entityMetaData.dtoTypeDesc.shortName} ${entityMetaData.dtoTypeDesc.instanceName});

    void convert(${entityMetaData.dtoTypeDesc.shortName} src, ${entityMetaData.beanTypeDesc.shortName} dest);

    void convert(${entityMetaData.beanTypeDesc.shortName} src, ${entityMetaData.dtoTypeDesc.shortName} dest);

    ${entityMetaData.dtoTypeDesc.shortName}[] convert(${entityMetaData.beanTypeDesc.shortName}[] ${entityMetaData.beanTypeDesc.instanceName}s);

    ${entityMetaData.dtoTypeDesc.shortName}[] convert(List list);

    ${entityMetaData.beanTypeDesc.shortName}[] convert(${entityMetaData.dtoTypeDesc.shortName}[] ${entityMetaData.dtoTypeDesc.instanceName}s);
}
