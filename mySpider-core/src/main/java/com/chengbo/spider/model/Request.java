package com.chengbo.spider.model;

public class Request {
	private String url;

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public Request(String url){
		this.url = url;
	}
}
