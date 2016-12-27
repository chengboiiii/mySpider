package com.chengbo.spider.downloader;

import com.chengbo.spider.model.Page;
import com.chengbo.spider.model.Request;
import com.chengbo.spider.model.Task;

public interface Downloader {
	public Page download(Request request,Task task);
}
