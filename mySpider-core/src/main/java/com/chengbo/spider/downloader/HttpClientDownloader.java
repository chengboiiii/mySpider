package com.chengbo.spider.downloader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chengbo.spider.model.Page;
import com.chengbo.spider.model.Request;
import com.chengbo.spider.model.Site;
import com.chengbo.spider.model.Task;
import com.chengbo.spider.proxy.Proxy;
import com.chengbo.spider.utils.UrlUtils;

public class HttpClientDownloader implements Downloader{
	private HttpClientGenerator httpClientGenerator = new HttpClientGenerator();
	private Map<String,CloseableHttpClient> httpclientMap = new HashMap<String,CloseableHttpClient>();
	
	private CloseableHttpClient getHttpClient(Site site, Proxy proxy) {
		if(site == null){
			return httpClientGenerator.getClient(site,proxy);
		}else{
			String domain = site.getDomain();
			CloseableHttpClient httpClient = httpclientMap.get(domain);
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
			acceptStatCode.add(200);
			charset = site.getCharset();
			headers = site.getHeaders();
		}else{
			acceptStatCode = new HashSet<>();
			acceptStatCode.add(200);
		}
		CloseableHttpResponse httpResponse = null;
		int statusCode = 0;
		HttpHost proxyHost = null;
		Proxy proxy = null;
		try{
			if(site.getProxypool()!=null){
				proxy = site.getProxypool().getProxy();
				proxyHost = proxy.getHttpHost();
			}
			HttpUriRequest httpUriRequest = getHttpUriRequest(request, site, headers, proxyHost);
			httpResponse = getHttpClient(site, proxy).execute(httpUriRequest);
			statusCode = httpResponse.getStatusLine().getStatusCode();
			if(acceptStatCode.contains(statusCode)){
				 Page page = handleResponse(request, charset, httpResponse, task);
				 return page;
			}
		}catch(IOException e){
			e.printStackTrace();
			if(site.getCycleRetryTimes()>0){
				 return addToCycleRetry(request, site);
			}
		}finally {
			try {
                if (httpResponse != null) {
                    //连接释放回连接池
                    EntityUtils.consume(httpResponse.getEntity());
                }
            } catch (IOException e) {
                System.out.println("关闭httpResponse失败...");
            }
		}
		return null;
	}

	private Page addToCycleRetry(Request request, Site site) {
		return null;
	}

	private Page handleResponse(Request request, String charset, CloseableHttpResponse httpResponse, Task task) throws IllegalStateException, IOException {
		String content = getContent(charset, httpResponse);
        Page page = new Page();
        page.setRawText(content);
        page.setUrl(request.getUrl());
        page.setRequest(request);
        page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        return page;
	}
	private String getContent(String charset, CloseableHttpResponse httpResponse) throws IllegalStateException, IOException {
		if (charset == null) {
            byte[] contentBytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());
            String htmlCharset = getHtmlCharset(httpResponse, contentBytes);
            if (htmlCharset != null) {
                return new String(contentBytes, htmlCharset);
            } else {
                return new String(contentBytes);
            }
        } else {
            return IOUtils.toString(httpResponse.getEntity().getContent(), charset);
        }
	}

	private String getHtmlCharset(CloseableHttpResponse httpResponse, byte[] contentBytes) throws UnsupportedEncodingException {
		String charset;
        String value = httpResponse.getEntity().getContentType().getValue();
        charset = UrlUtils.getCharset(value);
        if (StringUtils.isNotBlank(charset)) {
            return charset;
        }
        Charset defaultCharset = Charset.defaultCharset();
        String content = new String(contentBytes, defaultCharset.name());
        if (StringUtils.isNotEmpty(content)) {
            Document document = Jsoup.parse(content);
            Elements links = document.select("meta");
            for (Element link : links) {
                // 2.1、html4.01 <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                String metaContent = link.attr("content");
                String metaCharset = link.attr("charset");
                if (metaContent.indexOf("charset") != -1) {
                    metaContent = metaContent.substring(metaContent.indexOf("charset"), metaContent.length());
                    charset = metaContent.split("=")[1];
                    break;
                }
                // 2.2、html5 <meta charset="UTF-8" />
                else if (StringUtils.isNotEmpty(metaCharset)) {
                    charset = metaCharset;
                    break;
                }
            }
        }
        return charset;
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
