package com.chengbo.spider.downloader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import com.chengbo.spider.model.Page;
import com.chengbo.spider.model.Request;
import com.chengbo.spider.model.Site;
import com.chengbo.spider.model.Task;
import com.chengbo.spider.proxy.Proxy;

public class HttpClientDownloader implements Downloader{
	private HttpClientGenerator httpClientGenerator = new HttpClientGenerator();
	private Map<String,CloseableHttpClient> httpclientMap = new HashMap<String,CloseableHttpClient>();
	
	private CloseableHttpClient getHttpClient(Site site, Proxy proxy) {
		if(site == null){
			return httpClientGenerator.getClient(site,proxy);
		}else{
			String domain = site.getDomain();
			CloseableHttpClient httpClient = null;
			if(httpClient == null){
				synchronized (this) {
					httpClient = httpclientMap.get(domain);
					if(httpClient == null){
						httpClient = httpClientGenerator.getClient(site, proxy);
						httpclientMap.put(domain, httpClient);
					}
				}
			}
			return httpClient;
		}
	}
	
	public Page download(Request request,Task task){
		Site site = null;
		if(task != null){
			site = task.getSite();
		}
		Set<Integer> acceptStatCode;
		String charset = null;
		Map<String,String> headers = null;
		if(site != null){
			acceptStatCode = site.getAcceptStatCode();
			charset = site.getCharset();
			headers = site.getHeaders();
		}else{
			acceptStatCode = new HashSet<Integer>(200);
		}
		CloseableHttpResponse httpResponse = null;
		int statusCode = 0;
		HttpHost proxyHost = null;
		Proxy proxy = null;
		HttpUriRequest httpUriRequest = getHttpUriRequest(request, site, headers, proxyHost);
		return null;
	}

	public HttpUriRequest getHttpUriRequest(Request request, Site site, Map<String, String> headers,
			HttpHost proxyHost) {
		RequestBuilder requestBuilder = selectMethod(request).setUri(request.getUrl());
		if(headers != null){
			for(Map.Entry<String, String> entry : headers.entrySet()){
				requestBuilder.addHeader(entry.getKey(), entry.getValue());
			}
		}
		RequestConfig.Builder reBuilder = RequestConfig.custom();
		if(site !=null){
			reBuilder.setConnectionRequestTimeout(site.getTimeOut());
			reBuilder.setConnectTimeout(site.getTimeOut());
			reBuilder.setSocketTimeout(site.getTimeOut());
		}
		reBuilder.setCookieSpec(CookieSpecs.BEST_MATCH);
		if(proxyHost != null){
			reBuilder.setProxy(proxyHost);
		}
		requestBuilder.setConfig(reBuilder.build());
		return requestBuilder.build();
	}
	//根据请求方法组建RequestBuilder
	public RequestBuilder selectMethod(Request request){
		String method = request.getMethod();
		if("post".equalsIgnoreCase(method)){
			RequestBuilder requestBuilder = RequestBuilder.post();
			NameValuePair[] nameValuePair = request.getParams();
			if(nameValuePair != null && nameValuePair.length>0){
				requestBuilder.addParameters(nameValuePair);
			}
			return requestBuilder;
		}else{
			return RequestBuilder.get();
		}
	}
}
