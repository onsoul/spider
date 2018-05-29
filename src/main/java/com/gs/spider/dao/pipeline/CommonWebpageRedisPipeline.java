package com.gs.spider.dao.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.gs.spider.model.utils.StaticValue;

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
	private Logger log = LoggerFactory.getLogger(CommonWebpageRedisPipeline.class);
	private StaticValue staticValue;

	@Autowired
	public CommonWebpageRedisPipeline(StaticValue staticValue) {
		this.staticValue = staticValue;
		if (staticValue.isNeedRedis()) {
			log.info("正在初始化Redis客户端,Host:{},Port:{}", staticValue.getRedisHost(), staticValue.getRedisPort());
			jedis = new Jedis(staticValue.getRedisHost(), staticValue.getRedisPort());
			log.info("Jedis初始化成功,Clients List:{}", jedis.clientList());
		} else {
			log.warn("未初始化Redis客户端");
		}
	}

	@Override
	public void process(ResultItems resultItems, Task task) {
		if (!staticValue.isNeedRedis())
			return;
		long receivedClientsCount = jedis.publish(staticValue.getPublishChannelName(),
				gson.toJson(resultItems.getAll()));
	}
}
