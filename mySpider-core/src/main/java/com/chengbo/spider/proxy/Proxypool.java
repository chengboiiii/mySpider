package com.chengbo.spider.proxy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpHost;

import com.chengbo.spider.utils.ProxyUtils;

public class Proxypool {
	private List<Proxy> proxyPool = new ArrayList<Proxy>();
	//public void returnProxy(HttpHost host, int statusCode);
	
	public Proxypool addProxy(List<String[]> proxyList){
		for(String[] s:proxyList){
			try {
				HttpHost proxyHost = new HttpHost(InetAddress.getByName(s[2]), Integer.valueOf(s[3]));
				if(ProxyUtils.validateProxy(proxyHost)){
					 Proxy p = new Proxy(proxyHost, s[0], s[1]);
					 proxyPool.add(p);
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		return this;
	}
	public Proxy getProxy(){
		int rnum = new Random().nextInt(proxyPool.size());
		Proxy proxy = proxyPool.get(rnum);
		return proxy;
	}
}
