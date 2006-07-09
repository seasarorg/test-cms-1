<#list propertyDescs as propertyDesc>

${propertyDesc.name}_ = ${entityMetaData.dxoClassDesc.instanceName}_.convert(${entityMetaData.daoClassDesc.instanceName}_.selectAll());
</#list>
