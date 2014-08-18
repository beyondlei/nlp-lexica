package edu.kit.aifb.gwifi.lexica.search.mongodb;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;


public class InterlingualResourceMongoDBSearch {
	
	static String host = "mongodb://aifb-ls3-remus.aifb.kit.edu:19000";
	
	private MongoClient mongoClient;
	private DBCollection coll;
	
	private String sourceLanguage;
	private String targetLanguage;
	
	public InterlingualResourceMongoDBSearch(String host, String dbName, String collName, String inlang, String outlang)throws Exception{

		mongoClient = new MongoClient(new MongoClientURI(host));
		DB db = mongoClient.getDB(dbName);
		coll = db.getCollection(collName);
		
		sourceLanguage = inlang;
		targetLanguage = outlang;
	}

	public String searchTargetLanguage(String sourceResource) throws IOException {
		String targetResource = "";
		
		Pattern pattern = Pattern.compile("^"+sourceResource+"$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject(sourceLanguage, pattern);
	//	query.append(sourceLanguage, sourceResource);
		DBCursor cursor = coll.find(query);
		
		try {
			while (cursor.hasNext()) {
				if(cursor.next() != null && cursor.curr().containsField(targetLanguage))
					targetResource = (String) cursor.curr().get(targetLanguage);
			}
		} finally {
			cursor.close();
		}
		if(targetResource.equals("") || targetResource == null)
			return "";	
		return targetResource;
	}
	
	public LinkedHashMap<String, String> searchCrossLanguage(String sourceResource) throws IOException {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		
		Pattern pattern = Pattern.compile("^"+sourceResource+"$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject(sourceLanguage, sourceResource);
	//	query.append(sourceLanguage, sourceResource);
		DBCursor cursor = coll.find(query);
		
		try {
			while (cursor.hasNext()) {
				Set<String> langSet = cursor.next().keySet();
				Iterator<String> iter = langSet.iterator();
				while(iter.hasNext()){
					String lang = iter.next();
					if(lang.equals("_id"))
						continue;
					String source = (String)cursor.curr().get(lang);
					result.put(lang, source);
				}
			}
		} finally {
			cursor.close();
		}
		if(result.size() == 0|| result == null)
			return null;	
		return result;
	}

	public void close() throws IOException {
		mongoClient.close();
	}

	public static void main(String[] args) throws Exception {

		InterlingualResourceMongoDBSearch ilr = new InterlingualResourceMongoDBSearch(host, "lexica", "InterlingualResource", "en", "zh");

		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("Please input the title:");
			String text = scanner.nextLine();
			if (text.startsWith("exit"))
				break;
			
			String output = ilr.searchTargetLanguage(text);
			
			if(output.equals(""))
				System.out.println("There is no matched result!");
			else
				System.out.println(output);
		}
				
		ilr.close();
		scanner.close();
	}

}
