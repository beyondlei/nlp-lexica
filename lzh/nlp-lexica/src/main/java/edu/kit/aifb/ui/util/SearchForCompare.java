package edu.kit.aifb.ui.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import edu.kit.aifb.gwifi.lexica.index.mongodb.baseline.DBpediaLexicalizationsExtractor;
import edu.kit.aifb.gwifi.lexica.index.mongodb.baseline.DBpediaTopicSignaturesExtractor;

public class SearchForCompare {
	
	private int resultNum;
	
	private MongoClient mongoClient;
	private DBCollection rlcoll;
	private DBCollection rwcoll;
	
	public SearchForCompare(String host, String dbName, String resourceLabelCollName, String resourceWordCollName, int resultNum)throws Exception{
		this.resultNum = resultNum;
		
		mongoClient = new MongoClient(new MongoClientURI(host));
		DB db = mongoClient.getDB(dbName);
		rlcoll = db.getCollection(resourceLabelCollName);
		rwcoll = db.getCollection(resourceWordCollName);
	}

	public void close() throws IOException{
		mongoClient.close();
	}
	
	public CompareResult getResult(int type, String text) throws IOException{
		CompareResult result = new CompareResult();
		
		if(type == 1){
			result.setRlComparePlr(searchPlrByResource(text));
			result.setRlComparePmi(searchPmiByResource(text));
			result.setRwCompare(searchWordByResource(text));
		}else if(type == 2){
			result.setRlComparePrl(searchPrlByLabel(text));
			result.setRlComparePmi(searchPmiByLabel(text));
		}else if(type == 3){
			result.setRwCompare(searchResourceByWord(text));
		}
		
		this.close();
		return result;
	}
	
	public LinkedHashMap<String, Double> searchPlrByResource(String resource) throws IOException {
		LinkedHashMap<String, Double> resultMap = new LinkedHashMap<String, Double>();
		
		resource = resource.replaceAll(" ", "_");
		
		Pattern pattern = Pattern.compile("^"+resource+"$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject(DBpediaLexicalizationsExtractor.RESOURCE_FIELD, pattern);
		DBCursor cursor = rlcoll.find(query).sort(new BasicDBObject(DBpediaLexicalizationsExtractor.LABEL_PROBABILITY_FIELD, -1)).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				double plr = (Double)cursor.next().get(DBpediaLexicalizationsExtractor.LABEL_PROBABILITY_FIELD);
				String label = (String) cursor.curr().get(DBpediaLexicalizationsExtractor.LABEL_FIELD);
				resultMap.put(label, plr);
			}
		} finally {
			cursor.close();
		}
		if((resultMap == null) || (resultMap.size() == 0))
			return null;
		System.out.println("ResourcePlr      ~"+ resultMap.size());
		return resultMap;
	}

	public LinkedHashMap<Resource, Double> searchPmiByResource(String resource) throws IOException {
		LinkedHashMap<Resource, Double> resultMap = new LinkedHashMap<Resource, Double>();
		
		resource = resource.replaceAll(" ", "_");
		
		Pattern pattern = Pattern.compile("^"+resource+"$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject(DBpediaLexicalizationsExtractor.RESOURCE_FIELD, pattern);
		DBCursor cursor = rlcoll.find(query).sort(new BasicDBObject(DBpediaLexicalizationsExtractor.PMI_FIELD, -1)).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				double pmi = (Double)cursor.next().get(DBpediaLexicalizationsExtractor.PMI_FIELD);
				String label = (String) cursor.curr().get(DBpediaLexicalizationsExtractor.LABEL_FIELD);
				Resource r = new Resource();
				r.setTitle(label);
				r.setUrl("");
				resultMap.put(r, pmi);
			}
		} finally {
			cursor.close();
		}
		if((resultMap == null) || (resultMap.size() == 0))
			return null;
		System.out.println("ResourcePmi      ~"+ resultMap.size());
		return resultMap;
	}
	
	public LinkedHashMap<Resource, Double> searchPrlByLabel(String label) throws IOException {
		LinkedHashMap<Resource, Double> resultMap = new LinkedHashMap<Resource, Double>();
		
		label = label.replaceAll(" ", "_");
		
		Pattern pattern = Pattern.compile("^"+label+"$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject(DBpediaLexicalizationsExtractor.LABEL_FIELD, pattern);
		DBCursor cursor = rlcoll.find(query).sort(new BasicDBObject(DBpediaLexicalizationsExtractor.RESOURCE_PROBABILITY_FIELD, -1)).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				double prl = (Double)cursor.next().get(DBpediaLexicalizationsExtractor.RESOURCE_PROBABILITY_FIELD);
				String resource = (String) cursor.curr().get(DBpediaLexicalizationsExtractor.LABEL_FIELD);
				Resource r =  new Resource();
				r.setTitle(resource);
				String newkey = resource.replaceAll(" ", "_");
				r.setUrl("http://"+MongoDBInfo.getDbpeidaURL()+newkey);
				resultMap.put(r, prl);
			}
		} finally {
			cursor.close();
		}
		if((resultMap == null) || (resultMap.size() == 0))
			return null;
		System.out.println("LabelPrl      ~"+ resultMap.size());
		return resultMap;
	}
	
	public LinkedHashMap<Resource, Double> searchPmiByLabel(String label) throws IOException {
		LinkedHashMap<Resource, Double> resultMap = new LinkedHashMap<Resource, Double>();
		
		label = label.replaceAll(" ", "_");
		
		Pattern pattern = Pattern.compile("^"+label+"$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject(DBpediaLexicalizationsExtractor.LABEL_FIELD, pattern);
		DBCursor cursor = rlcoll.find(query).sort(new BasicDBObject(DBpediaLexicalizationsExtractor.PMI_FIELD, -1)).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				double pmi = (Double)cursor.next().get(DBpediaLexicalizationsExtractor.PMI_FIELD);
				String resource = (String) cursor.curr().get(DBpediaLexicalizationsExtractor.LABEL_FIELD);
				Resource r =  new Resource();
				r.setTitle(resource);
				String newkey = resource.replaceAll(" ", "_");
				r.setUrl("http://"+MongoDBInfo.getDbpeidaURL()+newkey);
				resultMap.put(r, pmi);
			}
		} finally {
			cursor.close();
		}
		if((resultMap == null) || (resultMap.size() == 0))
			return null;
		System.out.println("LabelPmi      ~"+ resultMap.size());
		return resultMap;
	}
	
	public List<Resource> searchWordByResource(String resource) throws IOException {
		List<Resource> resultList = new ArrayList<Resource>();
		
		resource = resource.replaceAll(" ", "_");
		
		Pattern pattern = Pattern.compile("^"+resource+"$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject(DBpediaTopicSignaturesExtractor.RESOURCE_FIELD, pattern);
		DBCursor cursor = rwcoll.find(query).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				String word = (String) cursor.next().get(DBpediaTopicSignaturesExtractor.WORD_FIELD);
				Resource r =  new Resource();
				r.setTitle(word);
				r.setUrl("");
				resultList.add(r);
			}
		} finally {
			cursor.close();
		}
		if((resultList == null) || (resultList.size() == 0))
			return null;
		System.out.println("Resourceword      ~"+ resultList.size());
		return resultList;
	}
	
	public List<Resource> searchResourceByWord(String word) throws IOException {
		List<Resource> resultList = new ArrayList<Resource>();
		
		word = word.replaceAll(" ", "_");
		
		Pattern pattern = Pattern.compile("^"+word+"$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject(DBpediaTopicSignaturesExtractor.WORD_FIELD, pattern);
		DBCursor cursor = rwcoll.find(query).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				String resource = (String) cursor.next().get(DBpediaTopicSignaturesExtractor.RESOURCE_FIELD);
				Resource r =  new Resource();
				r.setTitle(resource);
				String newkey = resource.replaceAll(" ", "_");
				r.setUrl("http://"+MongoDBInfo.getDbpeidaURL()+newkey);
				resultList.add(r);
			}
		} finally {
			cursor.close();
		}
		if((resultList == null) || (resultList.size() == 0))
			return null;
		System.out.println("WordResource      ~"+ resultList.size());
		return resultList;
	}
}
