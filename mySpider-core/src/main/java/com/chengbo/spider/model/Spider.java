package com.chengbo.spider.model;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.chengbo.spider.downloader.Downloader;
import com.chengbo.spider.downloader.HttpClientDownloader;
import com.chengbo.spider.pageprocess.Pageprocess;
import com.chengbo.spider.pipeline.Pipeline;
import com.chengbo.spider.scheduler.Scheduler;
import com.chengbo.spider.thread.ThreadPool;

public class Spider implements Runnable, Task{
	private Downloader downloader;
	private Scheduler scheduler;
	private Pipeline pipeline;
	private Pageprocess pageprocess;
	private ThreadPool threadPool;
	private Site site;
	private int threadNum = 1;
	private String uuid;
	private AtomicInteger status = new AtomicInteger(STAT_INIT);
	protected final static int STAT_INIT = 0;
    protected final static int STAT_RUNNING = 1;
    protected final static int STAT_STOPPED = 2;
    private List<Request> startRequests = new ArrayList<Request>();;
    protected boolean exitWhenComplete = true;
    protected boolean destroyWhenExit = true;
    private ReentrantLock newUrlLock = new ReentrantLock();
    private Condition urlCondition = newUrlLock.newCondition();
    
	public String getUUID() {
		return this.uuid;
	}
	public Site getSite() {
		return this.site;
	}
	public static Spider create(Pageprocess pageprocess){
		return new Spider(pageprocess);
	}
	public Spider(Pageprocess pageprocess) {
		this.pageprocess = pageprocess;
		this.site = pageprocess.getSite();
		this.uuid = UUID.randomUUID().toString();
	}
	public Spider setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
		return this;
	}
	private void init() {
		if(downloader == null){
			this.downloader = new HttpClientDownloader();
		}
		if(threadPool == null || threadPool.isShutdown()){
			this.threadPool = new ThreadPool(threadNum);
		}
		if(startRequests != null && startRequests.size()>0){
			for(Request request:startRequests){
				scheduler.push(request, this);
			}
			startRequests.clear();
		}
	}
	public void checkRunningStatus(){
		while(true){
			int statusNow = status.get();
			if(statusNow == STAT_RUNNING){
				System.out.println("爬虫正在运行.....");
			}
			if(status.compareAndSet(statusNow, STAT_RUNNING)){
				break;
			}
		}
	}
	
	@Override
	public void run() {
		checkRunningStatus();
		init();
		System.out.println("爬虫:"+uuid+"开始运行...");
		while(!Thread.currentThread().isInterrupted() && status.get() == STAT_RUNNING){
			Request request = scheduler.poll(this);
			if(request == null){
				if(threadPool.getThreadAlive() == 0 && exitWhenComplete){
					break;
				}
				waitNewUrls();
			}else{
				final Request frequest = request;
				threadPool.execute(new Runnable() {
					@Override
					public void run() {
						try{
							processRequest(frequest);
						}finally{
							signalNewUrl();
						}
					}
				});
			}
		}
		status.set(STAT_STOPPED);
        // 释放资源
        if (destroyWhenExit) {
            close();
        }
	}
	
	public void processRequest(Request request){
		Page page = downloader.download(request, this);
		if(page == null){
			 throw new RuntimeException("无法接收响应...");
		}
		pageprocess.process(page);
	}
	
	public Spider addstartUrls(String[] urls) {
        for (String url : urls) {
            scheduler.push(new Request(url), this);
        }
        signalNewUrl();
        return this;
    }
	
	private void waitNewUrls() {
		newUrlLock.lock();
		try{
			if(threadPool.getThreadAlive() == 0 && exitWhenComplete){
				return;
			}
			urlCondition.await(30000, TimeUnit.MILLISECONDS);
		}catch(InterruptedException e){
			System.out.println("等待新的url被打断...");
		}finally{
			newUrlLock.unlock();
		}
	}
	private void signalNewUrl() {
        try {
            newUrlLock.lock();
            urlCondition.signalAll();
        } finally {
            newUrlLock.unlock();
        }
    }
	public void close() {
        destroyEach(downloader);
        destroyEach(pageprocess);
        destroyEach(scheduler);
        destroyEach(pipeline);
        threadPool.shutdown();
    }
    private void destroyEach(Object object) {
        if (object instanceof Closeable) {
            try {
                ((Closeable) object).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
