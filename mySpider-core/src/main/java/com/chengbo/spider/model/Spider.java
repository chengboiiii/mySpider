package com.chengbo.spider.model;

import com.chengbo.spider.downloader.Downloader;
import com.chengbo.spider.pageprocess.Pageprocess;
import com.chengbo.spider.pipeline.Pipeline;
import com.chengbo.spider.scheduler.Scheduler;
import com.chengbo.spider.thread.ThreadPool;

public class Spider implements Task{
	private Downloader downloader;
	private Scheduler scheduler;
	private Pipeline pipeline;
	private Pageprocess pageprocess;
	private ThreadPool threadPool;
	private Site site;
	private int threadNum = 1;
	private String uuid;
	
	public String getUUID() {
		return this.uuid;
	}
	public Site getSite() {
		return this.site;
	}
	public Spider create(Pageprocess pageprocess){
		return new Spider(pageprocess);
	}
	public Spider(Pageprocess pageprocess) {
		this.pageprocess = pageprocess;
		this.site = pageprocess.getSite();
	}
	
}
