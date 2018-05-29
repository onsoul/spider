package com.gs.spider.dao.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import redis.clients.jedis.Jedis;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * Created by gsh199449 on 2016/10/24.
 */
@Component
public class CommonWebpageRedisPipeline implements Pipeline {
	private final Gson gson = new Gson();
	private Jedis jedis;
	private Logger log=LoggerFactory.getLogger(CommonWebpageRedisPipeline.class);
	

	@Value("${redis.need}")
	private boolean needRedis = true;

	@Value("${redis.host}")
	private String redisHost = "localhost";

	@Value("${redis.port}")
	private int redisPort = 10000;
	
	@Value("${redis.webpage.channel.name}")
	private String publishChannelName="webpage";
	
	
	@Autowired
	public CommonWebpageRedisPipeline() {
		if (needRedis) {
			log.info("正在初始化Redis客户端,Host:{},Port:{}", redisHost, redisPort);
			jedis = new Jedis(redisHost, redisPort);
			log.info("Jedis初始化成功,Clients List:{}", jedis.clientList());
		} else {
			log.warn("未初始化Redis客户端");
		}
	}

	@Override
	public void process(ResultItems resultItems, Task task) {
		if (!needRedis)
			return;
		long receivedClientsCount = jedis.publish(publishChannelName, gson.toJson(resultItems.getAll()));
	}
}
