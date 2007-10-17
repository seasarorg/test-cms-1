package org.seasar.cms.wiki.engine.plugin;

public class WikiPageLink {

	private String body;

	private String url;

	private String preMsg;

	private String postMsg;

	public WikiPageLink(String body, String url) {
		this.body = body;
		this.url = url;
	}

	public String getBody() {
		return body;
	}

	public String getUrl() {
		return url;
	}

	public boolean hasPreMsg() {
		if (preMsg == null || preMsg.length() == 0) {
			return false;
		}
		return true;
	}

	public boolean hasBody() {
		if (body == null || body.length() == 0) {
			return false;
		}
		return true;
	}

	public boolean hasPostMsg() {
		if (postMsg == null || postMsg.length() == 0) {
			return false;
		}
		return true;
	}

	public String getPreMsg() {
		return preMsg;
	}

	public void setPreMsg(String preMsg) {
		this.preMsg = preMsg;
	}

	public String getPostMsg() {
		return postMsg;
	}

	public void setPostMsg(String postMsg) {
		this.postMsg = postMsg;
	}
}
