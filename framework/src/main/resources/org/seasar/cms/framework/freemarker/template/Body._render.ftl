<#list propertyDescs as propertyDesc>

${propertyDesc.name}_ = ${entityMetaData.dxoTypeDesc.instanceName}_.convert(${entityMetaData.daoTypeDesc.instanceName}_.selectAll());
</#list>
