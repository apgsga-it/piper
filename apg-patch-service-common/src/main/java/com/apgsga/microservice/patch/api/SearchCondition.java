package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

public enum SearchCondition {

	APPLICATION("application"), ALL("all"), IT21UI("it21ui"), FORMS2JAVA("forms2java"), PERSISTENT("persistent"); 

	private final String enumName;

	SearchCondition(String enumName) {
		this.enumName = enumName;
		Holder.ENUMMAP.put(enumName, this);
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