package com.chengbo.spider.downloader;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;

public class HttpClientDownloader implements Downloader {
	private HttpClientGenerator httpClientGenerator = new HttpClientGenerator();
	private Map<String,CloseableHttpClient> httpclientMap = new HashMap<String,CloseableHttpClient>();
}
