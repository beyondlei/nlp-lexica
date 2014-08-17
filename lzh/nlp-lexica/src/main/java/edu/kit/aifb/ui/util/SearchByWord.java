package edu.kit.aifb.ui.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import edu.kit.aifb.gwifi.lexica.search.mongodb.ResourceWordCoOccurrenceMongoDBSearcher;
import edu.kit.aifb.gwifi.wiki.mongodb.interfaceSearch.InterlingualResourceMongoDBSearch;
import edu.kit.aifb.ui.util.ChangeCase;
import edu.kit.aifb.ui.util.MongoDBInfo;
import edu.kit.aifb.ui.util.Resource;
import edu.kit.aifb.ui.util.Result;

public class SearchByWord {
	
	private static ResourceWordCoOccurrenceMongoDBSearcher resourceWordCoOccurrenceSearcher;
	private static InterlingualResourceMongoDBSearch interlingualResourceSearcher;
	
	private int resultNum;
	private String lang;
	
	public SearchByWord(String inlang, String outlang, int resultNum) throws Exception{
		
		this.resultNum = resultNum;
		this.lang = outlang;
		resourceWordCoOccurrenceSearcher = new ResourceWordCoOccurrenceMongoDBSearcher(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getResourceWordCoOccurrenceColl(),outlang);
		interlingualResourceSearcher = new InterlingualResourceMongoDBSearch(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getInterlingualResourceColl(),inlang,outlang);
	}

	public void close() throws IOException{
		resourceWordCoOccurrenceSearcher.close();
		interlingualResourceSearcher.close();
	}
	
	public Result getResult(String word) throws IOException{
		Result result = new Result();
		
		result.setRwCoOccurrencePrw(getResourceWordCoOccurrencePrw(word));
		result.setRwCoOccurrencePmi(getResourceWordCoOccurrencePmi(word));
		result.setCrosslingual(getCrossLingual(word));
		this.close();
		return result;
	}
	
	public LinkedHashMap<Resource, Double> getResourceWordCoOccurrencePrw(String word) throws IOException{
		LinkedHashMap<String, Double> tmp = resourceWordCoOccurrenceSearcher.searchPrwByWord(word, resultNum);
		if((tmp == null) || (tmp.size() == 0)){
			ChangeCase c = new ChangeCase();
			tmp = resourceWordCoOccurrenceSearcher.searchPrwByWord(c.changeCase(word), resultNum);
		}
		if((tmp == null) || (tmp.size() == 0))
			return null;
		
		Iterator<Entry<String, Double>> iter = tmp.entrySet().iterator();
		LinkedHashMap<Resource, Double> rwcoprwMap = new LinkedHashMap<Resource, Double>();
		while(iter.hasNext()){
			Entry<String, Double> entry = iter.next();
			String key = entry.getKey();
			double value = entry.getValue();
			Resource r = new Resource();
			r.setTitle(key);
			String newkey = key.replaceAll(" ", "_");
			if(lang.equals("en")){
				r.setUrl("http://"+MongoDBInfo.getDbpeidaURL()+newkey);
			}else{
				r.setUrl("http://" + lang +"." + MongoDBInfo.getDbpeidaURL() + newkey);
			}
			rwcoprwMap.put(r, value);
		}
		System.out.println("rwcoprwMap      ~"+ rwcoprwMap.size());
		return rwcoprwMap;
	}
	
	public LinkedHashMap<Resource, Double> getResourceWordCoOccurrencePmi(String word) throws IOException{
		LinkedHashMap<String, Double> tmp = resourceWordCoOccurrenceSearcher.searchPmiByWord(word, resultNum);
		if((tmp == null) || (tmp.size() == 0)){
			ChangeCase c = new ChangeCase();
			tmp = resourceWordCoOccurrenceSearcher.searchPmiByWord(c.changeCase(word), resultNum);
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
			String newkey = key.replaceAll(" ", "_");
			if(lang.equals("en")){
				r.setUrl("http://"+MongoDBInfo.getDbpeidaURL()+newkey);
			}else{
				r.setUrl("http://" + lang +"." + MongoDBInfo.getDbpeidaURL() + newkey);
			}
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
