package com.chengbo.spider.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;

public class Proxy implements Delayed{
	private String user;
	private String password;
	private HttpHost httpHost;
	private int failNum = 0;
	private int reuseTimeInterval = 1500;
	private long canReuseTime = 0L;
	private long lastBorrowTime = System.currentTimeMillis();
	private long responseTime = 0L;
	public static final int ERROR_403 = 403;
	public static final int ERROR_404 = 404;
	public static final int ERROR_BANNED = 10000;// 被禁止
	public static final int ERROR_Proxy = 10001;// 失败
	public static final int SUCCESS = 200;
	private int failedNum = 0;
	private int successNum = 0;
	private int borrowNum = 0;
	private List<Integer> failedErrorType = new ArrayList<Integer>();

 	public String getUser() {
		return user;
	}
	public HttpHost getHttpHost() {
		return httpHost;
	}
	public String getPassword() {
		return password;
	}
	public int getFailNum() {
		return failNum;
	}
	public void setFailNum(int failNum) {
		this.failNum = failNum;
	}
	public Proxy(HttpHost proxyHost, String user, String pwd) {
		this.httpHost = proxyHost;
		this.user = user;
		this.password =pwd;
		this.canReuseTime = System.nanoTime()+TimeUnit.NANOSECONDS.convert(reuseTimeInterval, TimeUnit.MILLISECONDS);
	}
	@Override
	public int compareTo(Delayed o) {
		Proxy that = (Proxy)o;
		return canReuseTime > that.canReuseTime ? 1:(canReuseTime < that.canReuseTime ? -1 : 0);
	}
	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(canReuseTime - System.nanoTime(), TimeUnit.NANOSECONDS);
	}
	public int getSuccessNum() {
		return successNum;
	}

	public void successNumIncrement(int increment) {
		this.successNum += increment;
	}

	public Long getLastUseTime() {
		return lastBorrowTime;
	}

	public void setLastBorrowTime(Long lastBorrowTime) {
		this.lastBorrowTime = lastBorrowTime;
	}

	public void recordResponse() {
		this.responseTime = (System.currentTimeMillis() - lastBorrowTime + responseTime) / 2;
		this.lastBorrowTime = System.currentTimeMillis();
	}

	public List<Integer> getFailedErrorType() {
		return failedErrorType;
	}

	public void setFailedErrorType(List<Integer> failedErrorType) {
		this.failedErrorType = failedErrorType;
	}

	public void fail(int failedErrorType) {
		this.failedNum++;
		this.failedErrorType.add(failedErrorType);
	}

	public void setFailedNum(int failedNum) {
		this.failedNum = failedNum;
	}

	public int getFailedNum() {
		return failedNum;
	}

	public String getFailedType() {
		String re = "";
		for (Integer i : this.failedErrorType) {
			re += i + " . ";
		}
		return re;
	}

	public int getReuseTimeInterval() {
		return reuseTimeInterval;
	}

	public void setReuseTimeInterval(int reuseTimeInterval) {
		this.reuseTimeInterval = reuseTimeInterval;
		this.canReuseTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(reuseTimeInterval, TimeUnit.MILLISECONDS);

	}
	public void borrowNumIncrement(int increment) {
		this.borrowNum += increment;
	}

	public int getBorrowNum() {
		return borrowNum;
	}
}
