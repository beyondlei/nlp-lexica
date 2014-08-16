package edu.kit.aifb.ui.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import edu.kit.aifb.gwifi.lexica.search.mongodb.ResourceWordCoOccurrenceMongoDBSearcher;

public class SearchByWord {
	
	private static ResourceWordCoOccurrenceMongoDBSearcher resourceWordCoOccurrenceSearcher;
	
	private int resultNum;
	private String lang;
	
	public SearchByWord(String lang, int resultNum) throws Exception{
		
		this.resultNum = resultNum;
		this.lang = lang;
		resourceWordCoOccurrenceSearcher = new ResourceWordCoOccurrenceMongoDBSearcher(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getResourceWordCoOccurrenceColl(),lang);
	}

	public void close() throws IOException{
		resourceWordCoOccurrenceSearcher.close();
	}
	
	public Result getResult(String word) throws IOException{
		Result result = new Result();
		
		result.setRwCoOccurrencePrw(getResourceWordCoOccurrencePrw(word));
		result.setRwCoOccurrencePmi(getResourceWordCoOccurrencePmi(word));
		
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

}
