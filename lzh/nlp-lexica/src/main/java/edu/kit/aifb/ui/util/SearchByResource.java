package edu.kit.aifb.ui.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import edu.kit.aifb.gwifi.lexica.search.mongodb.ResourceLabelCoOccurrenceMongoDBSearcher;
import edu.kit.aifb.gwifi.lexica.search.mongodb.ResourceLabelSenseMongoDBSearcher;
import edu.kit.aifb.gwifi.lexica.search.mongodb.ResourceWordCoOccurrenceMongoDBSearcher;

public class SearchByResource {
	
	
	private static ResourceLabelSenseMongoDBSearcher labelResourceSenseSearcher;
	private static ResourceLabelCoOccurrenceMongoDBSearcher resourceLabelCoOccurrenceSearcher;
	private static ResourceWordCoOccurrenceMongoDBSearcher resourceWordCoOccurrenceSearcher;
	
	private int resultNum;
	
	public SearchByResource(String lang, int resultNum) throws Exception{
		this.resultNum = resultNum;
		
		labelResourceSenseSearcher = new ResourceLabelSenseMongoDBSearcher(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getLabelResourceSenseColl(),lang);
		resourceLabelCoOccurrenceSearcher = new ResourceLabelCoOccurrenceMongoDBSearcher(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getResourceLabelCoOccurrenceColl(),lang);
		resourceWordCoOccurrenceSearcher = new ResourceWordCoOccurrenceMongoDBSearcher(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getResourceWordCoOccurrenceColl(),lang);
	}

	public void close() throws IOException{
		labelResourceSenseSearcher.close();
		resourceLabelCoOccurrenceSearcher.close();
		resourceWordCoOccurrenceSearcher.close();
	}
	
	public Result getResult(String resource) throws IOException{
		Result result = new Result();
		
		result.setRlCoOccurrencePlr(getResourceLabelCoOccurrencePlr(resource));
		result.setRlCoOccurrencePmi(getResourceLabelCoOccurrencePmi(resource));
		result.setRlSensePlr(getLabelResourceSensePlr(resource));
		result.setRlSensePmi(getLabelResourceSensePmi(resource));
		result.setRwCoOccurrencePwr(getResourceWordCoOccurrencePwr(resource));
		result.setRwCoOccurrencePmi(getResourceWordCoOccurrencePmi(resource));
		
		this.close();
		return result;
	}
	
	public LinkedHashMap<String, Double> getLabelResourceSensePlr(String resource) throws IOException{
		LinkedHashMap<String, Double> lrsPlrMap = labelResourceSenseSearcher.searchPlrByResource(resource, resultNum);
		if((lrsPlrMap == null) || (lrsPlrMap.size() == 0)){
			ChangeCase c = new ChangeCase();
			lrsPlrMap = labelResourceSenseSearcher.searchPlrByResource(c.changeCase(resource), resultNum);
		}
		if((lrsPlrMap == null) || (lrsPlrMap.size() == 0))
			return null;
		System.out.println("lrsPlrMap      ~"+ lrsPlrMap.size());
		return lrsPlrMap;
	}
	
	public LinkedHashMap<Resource, Double> getLabelResourceSensePmi(String resource) throws IOException{
		LinkedHashMap<String, Double> tmp = labelResourceSenseSearcher.searchPmiByResource(resource, resultNum);
		if((tmp == null) || (tmp.size() == 0)){
			ChangeCase c = new ChangeCase();
			tmp = labelResourceSenseSearcher.searchPmiByResource(c.changeCase(resource), resultNum);
		}
		if((tmp == null) || (tmp.size() == 0))
			return null;
		
		Iterator<Entry<String, Double>> iter = tmp.entrySet().iterator();
		LinkedHashMap<Resource, Double> lrsPmiMap = new LinkedHashMap<Resource, Double>();
		while(iter.hasNext()){
			Entry<String, Double> entry = iter.next();
			String key = entry.getKey();
			double value = entry.getValue();
			Resource r = new Resource();
			r.setTitle(key);
			r.setUrl("");
			lrsPmiMap.put(r, value);
		}
		System.out.println("lrsPmiMap      ~"+ lrsPmiMap.size());
		return lrsPmiMap;
	}
	
	public LinkedHashMap<String, Double> getResourceLabelCoOccurrencePlr(String resource) throws IOException{
		LinkedHashMap<String, Double> lrcoplrMap = resourceLabelCoOccurrenceSearcher.searchPlrByResource(resource, resultNum);
		if((lrcoplrMap == null) || (lrcoplrMap.size() == 0)){
			ChangeCase c = new ChangeCase();
			lrcoplrMap = resourceLabelCoOccurrenceSearcher.searchPlrByResource(c.changeCase(resource), resultNum);
		}
		if((lrcoplrMap == null) || (lrcoplrMap.size() == 0))
			return null;
		System.out.println("lrcoplrMap      ~"+ lrcoplrMap.size());
		return lrcoplrMap;
	}
	
	public LinkedHashMap<Resource, Double> getResourceLabelCoOccurrencePmi(String resource) throws IOException{
		LinkedHashMap<String, Double> tmp = resourceLabelCoOccurrenceSearcher.searchPmiByResource(resource, resultNum);
		if((tmp == null) || (tmp.size() == 0)){
			ChangeCase c = new ChangeCase();
			tmp = resourceLabelCoOccurrenceSearcher.searchPmiByResource(c.changeCase(resource), resultNum);
		}
		if((tmp == null) || (tmp.size() == 0))
			return null;
		
		Iterator<Entry<String, Double>> iter = tmp.entrySet().iterator();
		LinkedHashMap<Resource, Double> lrcopmiMap = new LinkedHashMap<Resource, Double>();
		while(iter.hasNext()){
			Entry<String, Double> entry = iter.next();
			String key = entry.getKey();
			double value = entry.getValue();
			Resource r = new Resource();
			r.setTitle(key);
			r.setUrl("");
			lrcopmiMap.put(r, value);
		}
		System.out.println("lrcopmiMap      ~"+ lrcopmiMap.size());
		return lrcopmiMap;
	}
	
	public LinkedHashMap<String, Double> getResourceWordCoOccurrencePwr(String resource) throws IOException{
		LinkedHashMap<String, Double> rwcopwrMap = resourceWordCoOccurrenceSearcher.searchPwrByResource(resource, resultNum);
		if((rwcopwrMap == null) || (rwcopwrMap.size() == 0)){
			ChangeCase c = new ChangeCase();
			rwcopwrMap = resourceWordCoOccurrenceSearcher.searchPwrByResource(c.changeCase(resource), resultNum);
		}
		if((rwcopwrMap == null) || (rwcopwrMap.size() == 0))
			return null;
		System.out.println("rwcopwrMap      ~"+ rwcopwrMap.size());
		return rwcopwrMap;
	}
	
	public LinkedHashMap<Resource, Double> getResourceWordCoOccurrencePmi(String resource) throws IOException{
		LinkedHashMap<String, Double> tmp = resourceWordCoOccurrenceSearcher.searchPmiByResource(resource, resultNum);
		if((tmp == null) || (tmp.size() == 0)){
			ChangeCase c = new ChangeCase();
			tmp = resourceWordCoOccurrenceSearcher.searchPmiByResource(c.changeCase(resource), resultNum);
		}
		if((tmp == null) || (tmp.size() == 0))
			return null;
		
		Iterator<Entry<String, Double>> iter = tmp.entrySet().iterator();
		LinkedHashMap<Resource, Double> rwcopmiMap = new LinkedHashMap<Resource, Double>();
		while(iter.hasNext()){
			Entry<String, Double> entry = iter.next();
			String key = entry.getKey();
			double value = entry.getValue();
			Resource r = new Resource();
			r.setTitle(key);
			r.setUrl("");
			rwcopmiMap.put(r, value);
		}
		System.out.println("rwcopmiMap      ~"+ rwcopmiMap.size());
		return rwcopmiMap;
	}
}
