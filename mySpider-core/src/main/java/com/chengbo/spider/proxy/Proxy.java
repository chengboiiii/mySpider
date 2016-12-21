package com.chengbo.spider.proxy;

import org.apache.http.HttpHost;

public class Proxy {
	private String user;
	private String password;
	private HttpHost httpHost;
	
	public String getUser() {
		return user;
	}
	public HttpHost getHttpHost() {
		return httpHost;
	}
	public String getPassword() {
		return password;
	}

}
