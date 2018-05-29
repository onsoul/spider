package com.gs.spider.dao.pipeline;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.gs.spider.core.es.ESUtil;
import com.gs.spider.dao.IDAO;
import com.gs.spider.model.commons.SpiderInfo;
import com.gs.spider.model.commons.Webpage;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

/**
 * CommonWebpagePipeline
 *
 * @author Gao Shen
 * @version 16/4/12
 */
@Component
public class WebpagePipeline extends IDAO<Webpage> implements DuplicateRemover, Pipeline {
	
	private final static String COMMON_INDEX_CONFIG = "mapping/commonIndex.json";
	private static final String COMMONS_INDEX_NAME = "commons";
	private static final String TYPE_WEBPAGE = "webpage";
	
	private static Logger log=LoggerFactory.getLogger(WebpagePipeline.class);

	private static final String DYNAMIC_FIELD = "dynamic_fields";
	private static final Gson gson = new GsonBuilder()
			.registerTypeAdapter(Date.class,
					(JsonDeserializer<Date>) (json, typeOfT,
							context) -> new Date(json.getAsJsonPrimitive().getAsLong()))
			.registerTypeAdapter(Date.class,
					(JsonSerializer<Date>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getTime()))
			.setDateFormat(DateFormat.LONG).create();
	
	private static int COUNT = 0;
	
	private Map<String, Set<String>> urls = Maps.newConcurrentMap();

	@Autowired
	private ESUtil esUtil;
	
    @Autowired
    private Client client;

	public WebpagePipeline() {
		//check();
	}

	@PostConstruct
	protected boolean check() {
		return esUtil.checkIndex(COMMONS_INDEX_NAME, COMMON_INDEX_CONFIG)
				&& esUtil.checkType(COMMONS_INDEX_NAME, TYPE_WEBPAGE, "mapping/webpage.json");
	}
	
	/**
	 * 将webmagic的resultItems转换成webpage对象
	 * @param resultItems
	 * @return
	 */
	public static Webpage convertResultItems2Webpage(ResultItems resultItems) {
		Webpage webpage = new Webpage();
		webpage.setContent(resultItems.get("content"));
		webpage.setTitle(resultItems.get("title"));
		webpage.setUrl(resultItems.get("url"));
		webpage.setId(Hashing.md5().hashString(webpage.getUrl(), Charset.forName("utf-8")).toString());
		webpage.setDomain(resultItems.get("domain"));
		webpage.setSpiderInfoId(resultItems.get("spiderInfoId"));
		webpage.setGathertime(resultItems.get("gatherTime"));
		webpage.setSpiderUUID(resultItems.get("spiderUUID"));
		webpage.setKeywords(resultItems.get("keywords"));
		webpage.setSummary(resultItems.get("summary"));
		webpage.setNamedEntity(resultItems.get("namedEntity"));
		webpage.setPublishTime(resultItems.get("publishTime"));
		webpage.setCategory(resultItems.get("category"));
		webpage.setRawHTML(resultItems.get("rawHTML"));
		webpage.setDynamicFields(resultItems.get(DYNAMIC_FIELD));
		webpage.setStaticFields(resultItems.get("staticField"));
		webpage.setAttachmentList(resultItems.get("attachmentList"));
		webpage.setImageList(resultItems.get("imageList"));
		webpage.setProcessTime(resultItems.get("processTime"));
		//log.info(webpage.toString());
		return webpage;
	}

	@Override
	public String index(Webpage webpage) {
		return null;
	}
	
	
	@Override
	public boolean isDuplicate(Request request, Task task) {
		Set<String> tempLists = urls.computeIfAbsent(task.getUUID(), k -> Sets.newConcurrentHashSet());
		// 初始化已采集网站列表缓存
		if (tempLists.add(request.getUrl())) {// 先检查当前生命周期是否抓取过,如果当前生命周期未抓取,则进一步检查ES
			GetResponse response = client.prepareGet(COMMONS_INDEX_NAME, TYPE_WEBPAGE,
					Hashing.md5().hashString(request.getUrl(), Charset.forName("utf-8")).toString()).get();
			return response.isExists();
		} else {// 如果当前生命周期已抓取,直接置为重复
			return true;
		}

	}

	@Override
	public void resetDuplicateCheck(Task task) {
	}

	@Override
	public int getTotalRequestsCount(Task task) {
		return COUNT++;
	}

	@Override
	public void process(ResultItems resultItems, Task task) {
		SpiderInfo spiderInfo = resultItems.get("spiderInfo");
		Webpage webpage = convertResultItems2Webpage(resultItems);
		try {
			client.prepareIndex(COMMONS_INDEX_NAME, TYPE_WEBPAGE)
					.setId(Hashing.md5().hashString(webpage.getUrl(), Charset.forName("utf-8")).toString())
					.setSource(gson.toJson(webpage), XContentType.JSON).get();
		} catch (Exception e) {
			log.error("索引 Webpage 出错," + e.getLocalizedMessage());
		}
	}

	/**
	 * 清除已停止任务的抓取url列表
	 *
	 * @param taskId
	 *            任务id
	 */
	public void deleteUrls(String taskId) {
		urls.remove(taskId);
		log.info("任务{}已结束,抓取列表缓存已清除", taskId);
	}
}
