package edu.kit.aifb.ui.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import edu.kit.aifb.gwifi.lexica.search.mongodb.InterlingualResourceMongoDBSearch;
import edu.kit.aifb.gwifi.lexica.search.mongodb.ResourceLabelCoOccurrenceMongoDBSearcher;
import edu.kit.aifb.gwifi.lexica.search.mongodb.ResourceLabelSenseMongoDBSearcher;
import edu.kit.aifb.ui.util.ChangeCase;
import edu.kit.aifb.ui.util.MongoDBInfo;
import edu.kit.aifb.ui.util.Resource;
import edu.kit.aifb.ui.util.Result;

public class SearchByLabel {
	
	private static ResourceLabelSenseMongoDBSearcher labelResourceSenseSearcher;
	private static ResourceLabelCoOccurrenceMongoDBSearcher resourceLabelCoOccurrenceSearcher;
	private static InterlingualResourceMongoDBSearch interlingualResourceSearcher;
	
	private int resultNum;
	private String lang;
	
	public SearchByLabel(String inlang, String outlang, int resultNum) throws Exception{
		this.resultNum = resultNum;
		this.lang = outlang;
		labelResourceSenseSearcher = new ResourceLabelSenseMongoDBSearcher(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getLabelResourceSenseColl(),lang);
		resourceLabelCoOccurrenceSearcher = new ResourceLabelCoOccurrenceMongoDBSearcher(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getResourceLabelCoOccurrenceColl(),lang);
		interlingualResourceSearcher = new InterlingualResourceMongoDBSearch(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getInterlingualResourceColl(),inlang,outlang);
	}

	public void close() throws IOException{
		labelResourceSenseSearcher.close();
		resourceLabelCoOccurrenceSearcher.close();
		interlingualResourceSearcher.close();
	}
	
	public Result getResult(String label) throws IOException{
		Result result = new Result();
		
		result.setRlCoOccurrencePrl(getResourceLabelCoOccurrencePrl(label));
		result.setRlCoOccurrencePmi(getResourceLabelCoOccurrencePmi(label));
		result.setRlSensePrl(getLabelResourceSensePrl(label));
		result.setRlSensePmi(getLabelResourceSensePmi(label));
		result.setCrosslingual(getCrossLingual(label));
		
		this.close();
		return result;
	}
	
	public LinkedHashMap<Resource, Double> getLabelResourceSensePrl(String label) throws IOException{
		LinkedHashMap<String, Double> tmp = labelResourceSenseSearcher.searchPrlByLabel(label, resultNum);
		if((tmp == null) || (tmp.size() == 0)){
			ChangeCase c = new ChangeCase();
			tmp = labelResourceSenseSearcher.searchPrlByLabel(c.changeCase(label), resultNum);
		}
		if((tmp == null) || (tmp.size() == 0))
			return null;
		
		Iterator<Entry<String, Double>> iter = tmp.entrySet().iterator();
		LinkedHashMap<Resource, Double> lrsPrlMap = new LinkedHashMap<Resource, Double>();
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
			lrsPrlMap.put(r, value);
		}
		System.out.println("lrsPrlMap      ~"+ lrsPrlMap.size());
		return lrsPrlMap;
	}
	
	public LinkedHashMap<Resource, Double> getLabelResourceSensePmi(String label) throws IOException{		
		LinkedHashMap<String, Double> tmp = labelResourceSenseSearcher.searchPmiByLabel(label, resultNum);
		if((tmp == null) || (tmp.size() == 0)){
			ChangeCase c = new ChangeCase();
			tmp = labelResourceSenseSearcher.searchPmiByLabel(c.changeCase(label), resultNum);
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
			String newkey = key.replaceAll(" ", "_");
			if(lang.equals("en")){
				r.setUrl("http://"+MongoDBInfo.getDbpeidaURL()+newkey);
			}else{
				r.setUrl("http://" + lang +"." + MongoDBInfo.getDbpeidaURL() + newkey);
			}
			lrsPmiMap.put(r, value);
		}
		System.out.println("lrsPmiMap      ~"+ lrsPmiMap.size());
		return lrsPmiMap;
	}
	
	public LinkedHashMap<Resource, Double> getResourceLabelCoOccurrencePrl(String label) throws IOException{
		LinkedHashMap<String, Double> tmp = resourceLabelCoOccurrenceSearcher.searchPrlByLabel(label, resultNum);
		if((tmp == null) || (tmp.size() == 0)){
			ChangeCase c = new ChangeCase();
			tmp = resourceLabelCoOccurrenceSearcher.searchPrlByLabel(c.changeCase(label), resultNum);
		}
		if((tmp == null) || (tmp.size() == 0))
			return null;
		
		Iterator<Entry<String, Double>> iter = tmp.entrySet().iterator();
		LinkedHashMap<Resource, Double> lrcoprlMap = new LinkedHashMap<Resource, Double>();
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
			lrcoprlMap.put(r, value);
		}
		System.out.println("lrcoprlMap      ~"+ lrcoprlMap.size());
		return lrcoprlMap;
	}
	
	public LinkedHashMap<Resource, Double> getResourceLabelCoOccurrencePmi(String label) throws IOException{
		LinkedHashMap<String, Double> tmp = resourceLabelCoOccurrenceSearcher.searchPmiByLabel(label, resultNum);
		if((tmp == null) || (tmp.size() == 0)){
			ChangeCase c = new ChangeCase();
			tmp = resourceLabelCoOccurrenceSearcher.searchPmiByLabel(c.changeCase(label), resultNum);
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
			String newkey = key.replaceAll(" ", "_");
			if(lang.equals("en")){
				r.setUrl("http://"+MongoDBInfo.getDbpeidaURL()+newkey);
			}else{
				r.setUrl("http://" + lang +"." + MongoDBInfo.getDbpeidaURL() + newkey);
			}
			lrcopmiMap.put(r, value);
		}
		System.out.println("lrcopmiMap      ~"+ lrcopmiMap.size());
		return lrcopmiMap;
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
