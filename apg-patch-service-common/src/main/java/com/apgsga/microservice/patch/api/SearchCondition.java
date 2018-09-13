package com.apgsga.microservice.patch.api;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Maps;

public enum SearchCondition {

	APPLICATION("application"), 
	ALL("all");
	
	private final String enumName; 
	
	private SearchCondition(String enumName) {
		this.enumName = enumName;
		Holder.ENUMMAP.put(enumName,this); 
	}

	@JsonCreator
	public static SearchCondition forValue(String value) {
		return Holder.ENUMMAP.get(StringUtils.lowerCase(value));
	}

	@JsonValue
	public String toValue() {
		return enumName;
	}
	
	private static class Holder {
		static Map<String, SearchCondition> ENUMMAP = Maps.newHashMap();
	}


}