package edu.kit.aifb.ui.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import edu.kit.aifb.gwifi.lexica.search.mongodb.ResourceLabelCoOccurrenceMongoDBSearcher;
import edu.kit.aifb.gwifi.lexica.search.mongodb.ResourceLabelSenseMongoDBSearcher;

public class SearchByLabel {
	
	private static ResourceLabelSenseMongoDBSearcher labelResourceSenseSearcher;
	private static ResourceLabelCoOccurrenceMongoDBSearcher resourceLabelCoOccurrenceSearcher;
	
	private int resultNum;
	private String lang;
	
	public SearchByLabel(String lang, int resultNum) throws Exception{
		this.resultNum = resultNum;
		this.lang = lang;
		labelResourceSenseSearcher = new ResourceLabelSenseMongoDBSearcher(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getLabelResourceSenseColl(),lang);
		resourceLabelCoOccurrenceSearcher = new ResourceLabelCoOccurrenceMongoDBSearcher(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getResourceLabelCoOccurrenceColl(),lang);
	}

	public void close() throws IOException{
		labelResourceSenseSearcher.close();
		resourceLabelCoOccurrenceSearcher.close();
	}
	
	public Result getResult(String label) throws IOException{
		Result result = new Result();
		
		result.setRlCoOccurrencePrl(getResourceLabelCoOccurrencePrl(label));
		result.setRlCoOccurrencePmi(getResourceLabelCoOccurrencePmi(label));
		result.setRlSensePrl(getLabelResourceSensePrl(label));
		result.setRlSensePmi(getLabelResourceSensePmi(label));
		
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
}
