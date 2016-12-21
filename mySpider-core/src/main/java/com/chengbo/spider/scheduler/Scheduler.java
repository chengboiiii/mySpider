package com.chengbo.spider.scheduler;

import com.chengbo.spider.model.Request;
import com.chengbo.spider.model.Task;

public interface Scheduler {
	public void push(Request request, Task task);
	public Request poll(Task task);
}
