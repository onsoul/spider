package com.gs.spider.es;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.gs.spider.model.async.Task;

/**
 * ESClient
 *
 * @author Gao Shen
 * @version 16/1/21
 */
@Component
@Scope("prototype")
public class ESUtil {
	private Logger log = LogManager.getLogger(ESUtil.class);

	private static final int SCROLL_TIMEOUT = 5;
	
	@Autowired
	private Client client;

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public boolean checkType(String index, String type, String mapping) {
		if (client == null) {
			log.info("client is null,please check.");
			return false;
		}
		if (!client.admin().indices().typesExists(new TypesExistsRequest(new String[] { index }, type)).actionGet()
				.isExists()) {
			log.info(type + " type不存在,正在准备创建type");
			File mappingFile;
			try {
				mappingFile = new File(this.getClass().getClassLoader().getResource(mapping).getFile());
			} catch (Exception e) {
				log.fatal("查找ES mapping配置文件出错, " + e.getLocalizedMessage());
				return false;
			}
			log.debug(type + " MappingFile:" + mappingFile.getPath());
			PutMappingResponse mapPuttingResponse = null;

			PutMappingRequest putMappingRequest = null;
			try {
				putMappingRequest = Requests.putMappingRequest(index).type(type)
						.source(FileUtils.readFileToString(mappingFile), XContentType.JSON);
			} catch (IOException e) {
				log.error("创建 jvmSample mapping 失败," + e.getLocalizedMessage());
			}
			mapPuttingResponse = client.admin().indices().putMapping(putMappingRequest).actionGet();

			if (mapPuttingResponse.isAcknowledged())
				log.info("创建" + type + "type成功");
			else {
				log.error("创建" + type + "type索引失败");
				return false;
			}
		} else
			log.debug(type + " type 存在");
		return true;
	}

	public boolean checkIndex(String index, String mapping) {
		if (client == null)
			return false;
		if (!client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists()) {
			File indexMappingFile;
			try {
				indexMappingFile = new File(this.getClass().getClassLoader().getResource(mapping).getFile());
			} catch (Exception e) {
				log.fatal("查找" + index + "index mapping配置文件出错, " + e.getLocalizedMessage());
				return false;
			}
			log.debug(index + "index MappingFile:" + indexMappingFile.getPath());
			log.info(index + " index 不存在,正在准备创建index");
			CreateIndexResponse createIndexResponse = null;
			try {
				createIndexResponse = client.admin().indices().prepareCreate(index)
						.setSettings(FileUtils.readFileToString(indexMappingFile), XContentType.JSON).execute()
						.actionGet();
			} catch (IOException e) {
				log.error("创建 " + index + " index 失败");
				return false;
			}
			if (createIndexResponse.isAcknowledged())
				log.info(index + " index 成功");
			else {
				log.fatal(index + " index失败");
				return false;
			}
		} else
			log.debug(index + " index 存在");
		return true;
	}

	/**
	 * 根据query删除数据
	 * 
	 * @param queryBuilder
	 *            query
	 * @param task
	 *            任务实体
	 * @return 是否全部数据删除成功
	 */
	public boolean deleteByQuery(QueryBuilder queryBuilder, Task task,String indexName,String type) {
		BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName).setTypes(type)
				.setQuery(queryBuilder).setSize(100).setScroll(TimeValue.timeValueMinutes(SCROLL_TIMEOUT));
		SearchResponse response = searchRequestBuilder.execute().actionGet();
		while (true) {
			for (SearchHit hit : response.getHits()) {
				bulkRequestBuilder.add(new DeleteRequest(indexName, type, hit.getId()));
				if (task != null) {
					task.increaseCount();
				}
			}
			response = client.prepareSearchScroll(response.getScrollId())
					.setScroll(TimeValue.timeValueMinutes(SCROLL_TIMEOUT)).execute().actionGet();
			if (response.getHits().getHits().length == 0) {
				if (task != null) {
					task.setDescription("按query%s删除数据ID添加完毕,已经添加%s条,准备执行删除", queryBuilder.toString(),
							bulkRequestBuilder.numberOfActions());
				}
				log.debug("按query{}删除数据ID添加完毕,准备执行删除", queryBuilder.toString());
				break;
			} else {
				if (task != null) {
					task.setDescription("按query%s删除数据已经添加%s条", queryBuilder.toString(),
							bulkRequestBuilder.numberOfActions());
				}
				log.debug("按query{}删除数据已经添加{}条", queryBuilder.toString(), bulkRequestBuilder.numberOfActions());
			}
		}
		if (bulkRequestBuilder.numberOfActions() <= 0) {
			if (task != null) {
				task.setDescription("按query%s删除数据时未找到数据,请检查参数", queryBuilder.toString());
			}
			log.debug("按query{}删除数据时未找到数据,请检查参数", queryBuilder.toString());
			return false;
		}
		BulkResponse bulkResponse = bulkRequestBuilder.get();
		if (bulkResponse.hasFailures()) {
			if (task != null) {
				task.setDescription("按query%s删除数据部分失败,%s", queryBuilder.toString(), bulkResponse.buildFailureMessage());
			}
			log.error("按query{}删除数据部分失败,{}", queryBuilder.toString(), bulkResponse.buildFailureMessage());
		} else {
			if (task != null) {
				task.setDescription("按query%s删除数据成功,耗时:%s毫秒", queryBuilder.toString(),
						bulkResponse.getIngestTookInMillis());
			}
			log.info("按query{}删除数据成功,耗时:{}毫秒", queryBuilder.toString(), bulkResponse.getIngestTookInMillis());
		}
		return bulkResponse.hasFailures();
	}

	/**
	 * 获取库中数据总数
	 *
	 * @return
	 */
	public Long getTotal(String indexName, String type) {
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName).setTypes(type)
				.setQuery(QueryBuilders.matchAllQuery());
		SearchResponse response = searchRequestBuilder.execute().actionGet();
		return response.getHits().getTotalHits();
	}

	/**
	 * 获取库中符合条件是数据数量
	 *
	 * @param queryBuilder
	 *            匹配条件
	 * @return
	 */
	public Long getCountBy(QueryBuilder queryBuilder, String indexName, String type) {
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName).setTypes(type)
				.setQuery(queryBuilder);
		SearchResponse response = searchRequestBuilder.execute().actionGet();
		return response.getHits().getTotalHits();
	}

	/**
	 * 导出数据
	 *
	 * @param queryBuilder
	 *            数据查询
	 * @param labelsSupplier
	 *            提取labels,每篇文章返回一个label的List
	 * @param contentSupplier
	 *            提取正文
	 * @return 经过分词的输出流
	 */
	public void exportData(QueryBuilder queryBuilder, Function<SearchResponse, List<List<String>>> labelsSupplier,
			Function<SearchResponse, List<String>> contentSupplier, OutputStream outputStream,String indexName,String type) {
		final int size = 50;
		String scrollId = null;
		for (int page = 1;; page++) {
			log.debug("正在输出{}第{}页", queryBuilder, page);
			SearchResponse response;
			if (StringUtils.isBlank(scrollId)) {
				response = client.prepareSearch(indexName).setTypes(type).setQuery(queryBuilder).setSize(size)
						.setScroll(TimeValue.timeValueMinutes(SCROLL_TIMEOUT)).execute().actionGet();
				scrollId = response.getScrollId();
			} else {
				response = client.prepareSearchScroll(scrollId).setScroll(TimeValue.timeValueMinutes(SCROLL_TIMEOUT))
						.execute().actionGet();
			}
			final List<List<String>> labels = labelsSupplier.apply(response);
			final List<String> contentList = contentSupplier.apply(response);
			Preconditions.checkNotNull(labels);
			Preconditions.checkNotNull(contentList);
			if (contentList.size() <= 0)
				break;
			List<String> combine;
			if (labels.size() > 0) {
				combine = labels.stream().map(labelList -> labelList.parallelStream().collect(Collectors.joining("/")))
						.collect(Collectors.toList());
				for (int i = 0; i < labels.size(); i++) {
					String content = contentList.get(i);
					combine.set(i, combine.get(i) + " " + content);
				}
			} else {
				combine = contentList;
			}
			try {
				IOUtils.write(combine.stream().collect(Collectors.joining("\n")) + "\n", outputStream, "utf-8");
				outputStream.flush();
			} catch (IOException e) {
				log.error("输出错误,{}", e.getLocalizedMessage());
			}
		}
	}

}
