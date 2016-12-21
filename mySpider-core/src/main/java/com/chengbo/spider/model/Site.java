package com.chengbo.spider.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;

public class Site {
	private String domain;
	private String userAgent;
	private String charset;
	private List<Request> startRequests = new ArrayList<Request>();
	private int cycleRetryTimes=0; //下载失败重试次数
	private Map<String,String> headers = new HashMap<String, String>();
	private Map<String,String> defaultCookies = new LinkedHashMap<String,String>();
	private Map<String, Map<String, String>> cookies = new HashMap<String, Map<String, String>>();
	private HttpHost httpProxy;
	private boolean isUseGzip;
	private int timeOut =5000;
	private int retryTimes=3;//建立连接重试次数
	
	public HttpHost getHttpProxy() {
		return httpProxy;
	}
	public String getUserAgent() {
		return userAgent;
	}
	public boolean isUseGzip() {
		return isUseGzip;
	}
	public int getTimeOut() {
		return timeOut;
	}
	public int getRetryTimes() {
		return retryTimes;
	}
	public String getDomain() {
		return domain;
	}
	public Map<String,String> getCookies() {
		return defaultCookies;
	}
	public Map<String,Map<String, String>> getAllCookies() {
        return cookies;
    }

}
