package org.seasar.cms.wiki.engine;

import java.util.Date;

public class WikiContext {

	private Date modificationDate;

	private String outputType = "html";

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	public String getOutputType() {
		return outputType;
	}

	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	public Date getModificationDate() {
		return modificationDate;
	}

}
