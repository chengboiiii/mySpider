package com.chengbo.spider.scheduler;



import com.chengbo.spider.model.Request;
import com.chengbo.spider.model.Task;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisScheduler implements Scheduler {
	private JedisPool jedisPool;
	private static final String VISITED = "visited_";
	private static final String UNVISITED = "unvisited_";

	@Override
	public void push(Request request, Task task) {
		Jedis jedis = jedisPool.getResource();
		if(!isDuplicate(request, task)){
			try{
				jedis.rpush(getUnVisited(task), request.getUrl());
				//if()
			}finally{
				jedisPool.returnResource(jedis);
			}
		}
	}


	@Override
	public synchronized Request poll(Task task) {
		Jedis jedis = jedisPool.getResource();
		try{
			String url = jedis.lpop(getUnVisited(task));
			if(url == null){
				return null;
			}else{
				return new Request(url);
			}
		}finally{
			jedisPool.returnResource(jedis);
		}
	}
	
	public RedisScheduler(String host){
		this(new JedisPool(new JedisPoolConfig(), host));
	}

	public RedisScheduler(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}
	
	public boolean isDuplicate(Request request,Task task){
		Jedis jedis = jedisPool.getResource();
		try{
			boolean isDuplicate = jedis.sismember(getVisited(task), request.getUrl());
			if(!isDuplicate){
				jedis.sadd(getVisited(task), request.getUrl());
			}
			return isDuplicate;
		}finally{
			jedisPool.returnResource(jedis);
		}
	}

	private String getVisited(Task task) {
		return VISITED+task.getUUID();
	}
	private String getUnVisited(Task task) {
		return UNVISITED+task.getUUID();
	}
	
}
