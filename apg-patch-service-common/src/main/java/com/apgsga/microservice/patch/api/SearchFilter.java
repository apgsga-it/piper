package com.apgsga.microservice.patch.api;

public class SearchFilter {
	
	public static SearchFilter DEFAULT = new SearchFilter();
	public static SearchFilter ALL = new SearchFilter(SearchCondition.ALL);

	public static enum SearchCondition {
		APPLICATION, ALL
	}

	private SearchCondition condition = SearchCondition.APPLICATION;

	public SearchFilter(SearchCondition searchCondition) {
	}

	public SearchFilter() {
	}

	public SearchCondition getCondition() {
		return condition;
	}

	public void setCondition(SearchCondition condition) {
		this.condition = condition;
	}

}
