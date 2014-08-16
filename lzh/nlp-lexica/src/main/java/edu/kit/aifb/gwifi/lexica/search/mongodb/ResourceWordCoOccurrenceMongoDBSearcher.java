package edu.kit.aifb.gwifi.lexica.search.mongodb;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.Scanner;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import edu.kit.aifb.gwifi.lexica.index.mongodb.ResourceWordCoOccurrenceMongoDBConverter;


public class ResourceWordCoOccurrenceMongoDBSearcher {
	
	static String host = "mongodb://aifb-ls3-remus.aifb.kit.edu:19000";
	
	private MongoClient mongoClient;
	private DBCollection coll;
	
	public ResourceWordCoOccurrenceMongoDBSearcher(String host, String dbName, String collName, String lang)throws Exception{

		mongoClient = new MongoClient(new MongoClientURI(host));
		DB db = mongoClient.getDB(dbName);
		coll = db.getCollection(collName + "_" + lang);
	}

	public LinkedHashMap<String, Double> searchPwrByResource(String resource, int resultNum) throws IOException {
		LinkedHashMap<String, Double> resultMap = new LinkedHashMap<String, Double>();

		Pattern pattern = Pattern.compile("^"+resource+"$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject(ResourceWordCoOccurrenceMongoDBConverter.RESOURCE_FIELD, resource);
		DBCursor cursor = coll.find(query).sort(new BasicDBObject(ResourceWordCoOccurrenceMongoDBConverter.WORD_PROBABILITY_FIELD, -1)).limit(resultNum);
		
		try {
			while (cursor.hasNext()) {
				double pwr = (Double)cursor.next().get(ResourceWordCoOccurrenceMongoDBConverter.WORD_PROBABILITY_FIELD);
				String word = (String) cursor.curr().get(ResourceWordCoOccurrenceMongoDBConverter.WORD_FIELD);
				resultMap.put(word, pwr);
			}
		} finally {
			cursor.close();
		}
		return resultMap;
	}

	public LinkedHashMap<String, Double> searchPmiByResource(String resource, int resultNum) throws IOException {
		LinkedHashMap<String, Double> resultMap = new LinkedHashMap<String, Double>();

		Pattern pattern = Pattern.compile("^"+resource+"$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject(ResourceWordCoOccurrenceMongoDBConverter.RESOURCE_FIELD, resource);
		DBCursor cursor = coll.find(query).sort(new BasicDBObject(ResourceWordCoOccurrenceMongoDBConverter.PMI_FIELD, -1)).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				double pmi =(Double)cursor.next().get(ResourceWordCoOccurrenceMongoDBConverter.PMI_FIELD);
				String word = (String) cursor.curr().get(ResourceWordCoOccurrenceMongoDBConverter.WORD_FIELD);
				resultMap.put(word, pmi);
			}
		} finally {
			cursor.close();
		}
		return resultMap;
	}
	
	public LinkedHashMap<String, Double> searchPrwByWord(String word, int resultNum) throws IOException {
		LinkedHashMap<String, Double> resultMap = new LinkedHashMap<String, Double>();

		Pattern pattern = Pattern.compile("^"+word+"$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject(ResourceWordCoOccurrenceMongoDBConverter.WORD_FIELD, word);
		DBCursor cursor = coll.find(query).sort(new BasicDBObject(ResourceWordCoOccurrenceMongoDBConverter.RESOURCE_PROBABILITY_FIELD, -1)).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				double prw = (Double)cursor.next().get(ResourceWordCoOccurrenceMongoDBConverter.RESOURCE_PROBABILITY_FIELD);
				String resource = (String) cursor.curr().get(ResourceWordCoOccurrenceMongoDBConverter.RESOURCE_FIELD);
				resultMap.put(resource, prw);
			}
		} finally {
			cursor.close();
		}
		return resultMap;
	}
	
	public LinkedHashMap<String, Double> searchPmiByWord(String word, int resultNum) throws IOException {
		LinkedHashMap<String, Double> resultMap = new LinkedHashMap<String, Double>();

		Pattern pattern = Pattern.compile("^"+word+"$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject(ResourceWordCoOccurrenceMongoDBConverter.WORD_FIELD, word);
		DBCursor cursor = coll.find(query).sort(new BasicDBObject(ResourceWordCoOccurrenceMongoDBConverter.PMI_FIELD, -1)).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				double pmi = (Double)cursor.next().get(ResourceWordCoOccurrenceMongoDBConverter.PMI_FIELD);
				String resource = (String) cursor.curr().get(ResourceWordCoOccurrenceMongoDBConverter.RESOURCE_FIELD);
				resultMap.put(resource, pmi);
			}
		} finally {
			cursor.close();
		}
		return resultMap;
	}

	public void close() throws IOException {
		mongoClient.close();
	}

	// "/index/Resource" "/index/LabelResourceSense"
	public static void main(String[] args) throws Exception {

		ResourceWordCoOccurrenceMongoDBSearcher rwc = new ResourceWordCoOccurrenceMongoDBSearcher(host, "lexica", "ResourceWordCoOccurrence", "en");

		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("Please input the title as format \"title:xx\" or the label as format \"word:xx\":");
			String text = scanner.nextLine();
			if (text.startsWith("exit"))
				break;
			String pre = text.split(":")[0];
			if (pre.equals("title")) {
				String title = text.split(":")[1];

				LinkedHashMap<String, Double> rs = rwc.searchPwrByResource(title, 10);
				if (rs == null)
					continue;
				if (rs.size() == 0)
					System.out.println("There is no matched result!");

				Iterator<Entry<String, Double>> iter = rs.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, Double> entry = (Map.Entry<String, Double>) iter.next();
					String word = entry.getKey();
					double score = entry.getValue();
					System.out.println("word:	" + word + "	score:	" + score);
				}
			} else if (pre.equals("word")) {
				String word = text.split(":")[1];
				LinkedHashMap<String, Double> rs = rwc.searchPmiByWord(word, 10);
				if (rs == null)
					continue;
				if (rs.size() == 0)
					System.out.println("There is no matched result!");

				Iterator<Entry<String, Double>> iter = rs.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, Double> entry = (Map.Entry<String, Double>) iter.next();
					String title = entry.getKey();
					double score = entry.getValue();
					System.out.println("article:	" + title + "	score:	" + score);
				}
			}
		}
		rwc.close();
		scanner.close();
	}

}
