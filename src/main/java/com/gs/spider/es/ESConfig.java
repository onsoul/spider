package com.gs.spider.es;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ESConfig {

	private Logger LOG = LogManager.getLogger(ESConfig.class);

	@Value("${es.host}")
	private String esHost = "localhost";

	@Value("${es.port}")
	private int esPort = 9300;

	@Value("${es.cluster.name}")
	private String esClusterName = "docker-cluster";

	@Value("${es.need}")
	private boolean isNeedEs = false;

	@Bean
	public Client getClient() {
		Client client = null;
		if (!isNeedEs) {
			LOG.info("已在配置文件中声明不需要ES,如需要ES,请在配置文件中进行配置");
			return null;
		}

		LOG.info("正在初始化ElasticSearch客户端," + esHost);

		Settings settings = Settings.builder().put("cluster.name", esClusterName).build();
		try {
			client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new TransportAddress(InetAddress.getByName(esHost), esPort));
			final ClusterHealthResponse healthResponse = client.admin().cluster().prepareHealth()
					.setTimeout(TimeValue.timeValueMinutes(1)).execute().actionGet();
			if (healthResponse.isTimedOut()) {
				LOG.error("ES客户端初始化失败");
			} else {
				LOG.info("ES客户端初始化成功");
			}
		} catch (IOException e) {
			LOG.fatal("构建ElasticSearch客户端失败!");
		}
		return client;
	}

}
