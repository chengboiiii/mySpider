package com.chengbo.spider.main;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chengbo.spider.model.Page;
import com.chengbo.spider.model.Site;
import com.chengbo.spider.model.Spider;
import com.chengbo.spider.pageprocess.Pageprocess;
import com.chengbo.spider.scheduler.RedisScheduler;



public class DoubanMovieProcessor implements Pageprocess {
	private static List<String[]> httpProxyList = new ArrayList<String[]>();
	static{
    	String[] s1 = {"","","124.88.67.20","80"};
//    	String[] s2 = {"","","120.27.5.242","9001"};
//    	String[] s3 = {"","","59.110.61.84","9999"};
    	httpProxyList.add(s1);
//    	httpProxyList.add(s2);
//    	httpProxyList.add(s3);
	}
	
    private Site site = Site.me()
    .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31")
    .setProxypool(httpProxyList);
    
    public static final String URL_LIST = "https://movie\\.douban\\.com/tag/%E7%88%B1%E6%83%85\\?start=\\d+&type=T";
    public static final String URL_POST = "https://movie\\.douban\\.com/subject/\\d+/";

    @Override
    public void process(Page page) {
      String html = page.getRawText();
      Document doc = Jsoup.parse(html);
      Elements links = doc.select("a[href]");
      for(Element link:links){
    	  System.out.println(link.attr("abs:href"));
      }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
    	String[] urls = {"https://movie.douban.com/tag/爱情"};
        Spider.create(new DoubanMovieProcessor())
        .setScheduler(new RedisScheduler("localhost"))
        .addstartUrls(urls).run();
            
    }
}