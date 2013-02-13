package ru.fuzzysearch;

public class SearchResult {
	public Long index;
	public Integer distance;
	public SearchResult(Long i,Integer d)
	{
		index = i;
		distance = d;
	}
}
