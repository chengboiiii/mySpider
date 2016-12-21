package com.chengbo.spider.pageprocess;

import com.chengbo.spider.model.Page;
import com.chengbo.spider.model.Site;

public interface Pageprocess {
	public void process(Page page);
	public Site getSite();
}
