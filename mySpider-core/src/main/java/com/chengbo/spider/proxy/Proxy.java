package com.chengbo.spider.proxy;

import org.apache.http.HttpHost;

public class Proxy {
	private String user;
	private String password;
	private HttpHost httpHost;
	private int failNum;
	public String getUser() {
		return user;
	}
	public HttpHost getHttpHost() {
		return httpHost;
	}
	public String getPassword() {
		return password;
	}
	public int getFailNum() {
		return failNum;
	}
	public void setFailNum(int failNum) {
		this.failNum = failNum;
	}
	public Proxy(HttpHost proxyHost, String user, String pwd) {
		this.httpHost = proxyHost;
		this.user = user;
		this.password =pwd;
	}
}
