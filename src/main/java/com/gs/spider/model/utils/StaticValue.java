package com.gs.spider.model.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StaticValue {

	@Value("${es.host}")
	private String esHost = "localhost";

	@Value("${es.port}")
	private int esPort = 9300;

	@Value("${es.cluster.name}")
	private String esClusterName = "docker-cluster";

	@Value("${es.need}")
	private boolean isNeedEs = false;

	@Value("${ajax.downloader}")
	private String ajaxDownloader = "http://localhost:7788/";

	@Value("${max.http.downloader.length}")
	private long maxHttpDownloadLength = 1048576;

	private int taskDeleteDelay = 1;
	private int taskDeletePeriod = 2;
	private long limitOfCommonWebpageDownloadQueue = 100000;

	private int commonsWebpageCrawlRatio = 2;

	public String getEsHost() {
		return esHost;
	}

	public void setEsHost(String esHost) {
		this.esHost = esHost;
	}

	public int getEsPort() {
		return esPort;
	}

	public void setEsPort(int esPort) {
		this.esPort = esPort;
	}

	public String getEsClusterName() {
		return esClusterName;
	}

	public void setEsClusterName(String esClusterName) {
		this.esClusterName = esClusterName;
	}

	public boolean isNeedEs() {
		return isNeedEs;
	}

	public void setNeedEs(boolean isNeedEs) {
		this.isNeedEs = isNeedEs;
	}

	public String getAjaxDownloader() {
		return ajaxDownloader;
	}

	public void setAjaxDownloader(String ajaxDownloader) {
		this.ajaxDownloader = ajaxDownloader;
	}

	public long getMaxHttpDownloadLength() {
		return maxHttpDownloadLength;
	}

	public void setMaxHttpDownloadLength(long maxHttpDownloadLength) {
		this.maxHttpDownloadLength = maxHttpDownloadLength;
	}

	public int getTaskDeleteDelay() {
		return taskDeleteDelay;
	}

	public void setTaskDeleteDelay(int taskDeleteDelay) {
		this.taskDeleteDelay = taskDeleteDelay;
	}

	public int getTaskDeletePeriod() {
		return taskDeletePeriod;
	}

	public void setTaskDeletePeriod(int taskDeletePeriod) {
		this.taskDeletePeriod = taskDeletePeriod;
	}

	public long getLimitOfCommonWebpageDownloadQueue() {
		return limitOfCommonWebpageDownloadQueue;
	}

	public void setLimitOfCommonWebpageDownloadQueue(long limitOfCommonWebpageDownloadQueue) {
		this.limitOfCommonWebpageDownloadQueue = limitOfCommonWebpageDownloadQueue;
	}

	public int getCommonsWebpageCrawlRatio() {
		return commonsWebpageCrawlRatio;
	}

	public void setCommonsWebpageCrawlRatio(int commonsWebpageCrawlRatio) {
		this.commonsWebpageCrawlRatio = commonsWebpageCrawlRatio;
	}

}
