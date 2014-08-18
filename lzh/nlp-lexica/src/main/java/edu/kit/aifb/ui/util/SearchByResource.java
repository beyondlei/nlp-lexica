package edu.kit.aifb.ui.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import edu.kit.aifb.gwifi.lexica.search.mongodb.InterlingualResourceMongoDBSearch;
import edu.kit.aifb.gwifi.lexica.search.mongodb.ResourceLabelCoOccurrenceMongoDBSearcher;
import edu.kit.aifb.gwifi.lexica.search.mongodb.ResourceLabelSenseMongoDBSearcher;
import edu.kit.aifb.gwifi.lexica.search.mongodb.ResourceWordCoOccurrenceMongoDBSearcher;
import edu.kit.aifb.ui.util.ChangeCase;
import edu.kit.aifb.ui.util.MongoDBInfo;
import edu.kit.aifb.ui.util.Resource;
import edu.kit.aifb.ui.util.Result;

public class SearchByResource {
	
	
	private static ResourceLabelSenseMongoDBSearcher labelResourceSenseSearcher;
	private static ResourceLabelCoOccurrenceMongoDBSearcher resourceLabelCoOccurrenceSearcher;
	private static ResourceWordCoOccurrenceMongoDBSearcher resourceWordCoOccurrenceSearcher;
	private static InterlingualResourceMongoDBSearch interlingualResourceSearcher;
	
	private int resultNum;
	private String lang;
	
	public SearchByResource(String inlang, String outlang, int resultNum) throws Exception{
		this.resultNum = resultNum;
		this.lang = outlang;
		
		labelResourceSenseSearcher = new ResourceLabelSenseMongoDBSearcher(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getLabelResourceSenseColl(),outlang);
		resourceLabelCoOccurrenceSearcher = new ResourceLabelCoOccurrenceMongoDBSearcher(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getResourceLabelCoOccurrenceColl(),outlang);
		resourceWordCoOccurrenceSearcher = new ResourceWordCoOccurrenceMongoDBSearcher(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getResourceWordCoOccurrenceColl(),outlang);
		interlingualResourceSearcher = new InterlingualResourceMongoDBSearch(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getInterlingualResourceColl(),inlang,outlang);
	}

	public void close() throws IOException{
		labelResourceSenseSearcher.close();
		resourceLabelCoOccurrenceSearcher.close();
		resourceWordCoOccurrenceSearcher.close();
		interlingualResourceSearcher.close();
	}
	
	public Result getResult(String resource) throws IOException{
		Result result = new Result();
		
		result.setRlCoOccurrencePlr(getResourceLabelCoOccurrencePlr(resource));
		result.setRlCoOccurrencePmi(getResourceLabelCoOccurrencePmi(resource));
		result.setRlSensePlr(getLabelResourceSensePlr(resource));
		result.setRlSensePmi(getLabelResourceSensePmi(resource));
		result.setRwCoOccurrencePwr(getResourceWordCoOccurrencePwr(resource));
		result.setRwCoOccurrencePmi(getResourceWordCoOccurrencePmi(resource));
		
		result.setCrosslingual(getCrossLingual(resource));
		
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
	
	public LinkedHashMap<String, Resource> getCrossLingual(String source) throws IOException{
		LinkedHashMap<String, String> tmp = interlingualResourceSearcher.searchCrossLanguage(source);
		
		if((tmp == null) || (tmp.size() == 0))
			return null;
		
		Iterator<Entry<String, String>> iter = tmp.entrySet().iterator();
		LinkedHashMap<String, Resource> clMap = new LinkedHashMap<String, Resource>();
		while(iter.hasNext()){
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String value = entry.getValue();
			Resource r = new Resource();
			r.setTitle(value);
			String newkey = value.replaceAll(" ", "_");
			if(key.equals("en")){
				r.setUrl("http://"+MongoDBInfo.getDbpeidaURL()+newkey);
			}else{
				r.setUrl("http://" + key +"." + MongoDBInfo.getDbpeidaURL() + newkey);
			}
			clMap.put(key, r);
		}
		return clMap;
	}
}
