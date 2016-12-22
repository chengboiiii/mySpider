package com.chengbo.spider.model;

import org.apache.http.NameValuePair;

public class Request {
	private String url;
	private String method;
	private NameValuePair[] params;
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public Request(String url){
		this.url = url;
	}
	public String getMethod() {
		return method;
	}
	public NameValuePair[] getParams() {
		return params;
	}
}
