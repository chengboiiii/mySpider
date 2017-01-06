package com.chengbo.spider.proxy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

import org.apache.http.HttpHost;

import com.chengbo.spider.utils.ProxyUtils;

public class Proxypool {
	private BlockingQueue<Proxy> proxyPool = new DelayQueue<Proxy>();
	private Map<String,Proxy> allProxy = new ConcurrentHashMap<String,Proxy>();
	private int reuseInterval = 1500;// ms
    private int reviveTime = 2 * 60 * 60 * 1000;// ms
    private int saveProxyInterval = 10 * 60 * 1000;// ms
    
	public Proxypool addProxy(List<String[]> proxyList){
		for(String[] s:proxyList){
			try {
				HttpHost proxyHost = new HttpHost(InetAddress.getByName(s[2]), Integer.valueOf(s[3]));
				if(ProxyUtils.validateProxy(proxyHost)){
					 Proxy p = new Proxy(proxyHost, s[0], s[1]);
					 proxyPool.add(p);
					 allProxy.put(s[2], p);
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
		Proxy proxy = null;
		try {
			Long time = System.currentTimeMillis();
			proxy = proxyPool.take();
			Double costTime = (System.currentTimeMillis() - time)/1000.0;
			if(costTime > reuseInterval){
				System.out.println("get proxy time >>>> " + costTime);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(proxy == null){
			throw new NoSuchElementException();
		}
		return proxy;
	}
	
	public void returnProxypool(HttpHost host, int statusCode){
		Proxy p = allProxy.get(host.getAddress().getHostAddress());
		if(p == null){
			return;
		}
		switch (statusCode) {
		case Proxy.SUCCESS:
			p.setReuseTimeInterval(reuseInterval);
            p.setFailedNum(0);
            p.setFailedErrorType(new ArrayList<Integer>());
            p.recordResponse();
            p.successNumIncrement(1);
			break;
	    case Proxy.ERROR_403:
            p.fail(Proxy.ERROR_403);
            p.setReuseTimeInterval(reuseInterval * p.getFailedNum());
            break;
        case Proxy.ERROR_BANNED:
            p.fail(Proxy.ERROR_BANNED);
            p.setReuseTimeInterval(10 * 60 * 1000 * p.getFailedNum());
            break;
        case Proxy.ERROR_404:
            break;
        default:
            p.fail(statusCode);
            break;
	    }
	    if (p.getFailedNum() > 20) {
	        p.setReuseTimeInterval(reviveTime);
	        return;
	    }
	    if (p.getFailedNum() > 0 && p.getFailedNum() % 5 == 0) {
	        if (!ProxyUtils.validateProxy(host)) {
	            p.setReuseTimeInterval(reviveTime);
	            return;
	        }
	    }
	    try {
	    	proxyPool.put(p);
	    } catch (InterruptedException e) {
		}
	}
}
