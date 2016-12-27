package com.chengbo.spider.model;

public class Page {
	private String rawText;//响应的文本
	private int statusCode;//响应码
	private Request request;
	private String url;
	
	public void setRawText(String rawText) {
		this.rawText = rawText;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public void setRequest(Request request) {
		this.request = request;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getRawText() {
		return rawText;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public Request getRequest() {
		return request;
	}
	public String getUrl() {
		return url;
	}
}
