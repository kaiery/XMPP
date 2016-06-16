package com.softfun.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


public class ResultBean implements Serializable {
	/**
	 * 返回成功
	 */
	private Boolean success = false;
	/**
	 * 返回键值对
	 */
	private Map<String, Object> datamap = null;
	/**
	 * 返回数据集合
	 */
	private List<?> datalist = null;
	/**
	 * 返回消息
	 */
	private String msg = null;
	/**
	 * 当前页码
	 */
	private Integer page = 0;
	/**
	 * 每行页数
	 */
	private Integer perrows = 0;
	/***
	 * 排序字段
	 */
	private String sort = null;
	/**
	 * 排序方式
	 */
	private String order = null;
	
	
	
	
	
	
	
	
	
 
	public Map<String, Object> getDatamap() {
		return datamap;
	}
	public void setDatamap(Map<String, Object> datamap) {
		this.datamap = datamap;
	}
	public Boolean getSuccess() {
		return success;
	}
	public void setSuccess(Boolean success) {
		this.success = success;
	}
	public List<?> getDatalist() {
		return datalist;
	}
	public void setDatalist(List<?> datalist) {
		this.datalist = datalist;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	public Integer getPerrows() {
		return perrows;
	}
	public void setPerrows(Integer perrows) {
		this.perrows = perrows;
	}
	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	
}
