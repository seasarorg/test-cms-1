package org.seasar.cms.wiki.engine.plugin;

public class WikiPageLink {

	private String body;

	private String url;

	private String creationMark;

	public WikiPageLink(String body, String url, String creationMark) {
		this.body = body;
		this.url = url;
		this.creationMark = creationMark;
	}

	public String getBody() {
		return body;
	}

	public String getUrl() {
		return url;
	}

	public String getCreationMark() {
		return creationMark;
	}

	public boolean hasBody() {
		if (body == null || body.length() == 0) {
			return false;
		}
		return true;
	}

	public boolean hasCreationMark() {
		if (creationMark == null || creationMark.length() == 0) {
			return false;
		}
		return true;
	}

}
