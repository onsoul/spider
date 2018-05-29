package com.gs.spider.dao;

import java.text.DateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

/**
 * IDAO Elasticsearch数据接口
 *
 * @author Gao Shen
 * @version 16/1/25
 */

public abstract class IDAO<T> {

	protected Logger log = LoggerFactory.getLogger(this.getClass());
	protected Queue<T> queue = new ConcurrentLinkedDeque<>();
	protected static final Gson gson = new GsonBuilder()
			.registerTypeAdapter(Date.class,
					(JsonDeserializer<Date>) (json, typeOfT,
							context) -> new Date(json.getAsJsonPrimitive().getAsLong()))
			.registerTypeAdapter(Date.class,
					(JsonSerializer<Date>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getTime()))
			.setDateFormat(DateFormat.LONG).create();

	public abstract String index(T t);

}
