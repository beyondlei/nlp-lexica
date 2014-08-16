package edu.kit.aifb.gwifi.lexica.index.mongodb.baseline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class DBpediaTopicSignaturesExtractor {

	static String inFile = "/home/ws/oi1299/lexica/topic_signatures_en.tsv";
	static String host = "mongodb://aifb-ls3-remus.aifb.kit.edu:19000";
	
	public static String RESOURCE_FIELD = "resource";
	public static String WORD_FIELD = "word";

	private MongoClient mongoClient;
	private DBCollection coll;
	private BulkWriteOperation builder;
	
	public DBpediaTopicSignaturesExtractor(String dbName, String collName)throws Exception{
		
		mongoClient = new MongoClient(new MongoClientURI(host));
		DB db = mongoClient.getDB(dbName);
		coll = db.getCollection(collName);
		builder = coll.initializeUnorderedBulkOperation();
	}
	
	public void extractInformation(String inPath) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(inPath));
		int count = 0;
		String tmp,resource="",word="";
		while((tmp = reader.readLine()) != null){
			System.out.println("~~~~~~~~~~~~~~"+ ++count);
			Matcher m = Pattern.compile("^.*(?=\\t\\+\\\")").matcher(tmp);
			while(m.find())
					resource = m.group();
			if(resource.length() > 500)
				continue;
			
			m = Pattern.compile("(?<=\\\" )((?!\\+\\\").*)").matcher(tmp);
			while(m.find()){
				word = m.group();
				if(word.length() != 0){
					String[] words = word.split(" ");
					for(int i=0;i<words.length;i++)
						if(words[i].length() <= 500)
							createDocumentByBulk(resource, words[i]);	
				}
			}
		}	
		builder.execute();
		reader.close();
	}
	
	public void createDocumentByBulk(String resource, String word){
		BasicDBObject doc = new BasicDBObject(RESOURCE_FIELD, resource).append(WORD_FIELD, word);
		builder.insert(doc);
	}
	
	public void createIndex() {
		
		System.out.println("Start creating Index!");
		coll.createIndex(new BasicDBObject(RESOURCE_FIELD, 1));
		coll.createIndex(new BasicDBObject(WORD_FIELD, 1));
		System.out.println("finish creating Index!");
	}
	
	public void close() throws IOException{
		mongoClient.close();
	}
	
	public List<String> searchWordByResource(String resource, int resultNum) throws IOException {
		List<String> resultList = new ArrayList<String>();
		BasicDBObject query = new BasicDBObject();
		query.append(RESOURCE_FIELD, resource);
		DBCursor cursor = coll.find(query).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				String word = (String) cursor.next().get(WORD_FIELD);
				resultList.add(word);
			}
		} finally {
			cursor.close();
		}
		return resultList;
	}
	
	public List<String> searchResourceByWord(String word, int resultNum) throws IOException {
		List<String> resultList = new ArrayList<String>();
		BasicDBObject query = new BasicDBObject();
		query.append(WORD_FIELD, word);
		DBCursor cursor = coll.find(query).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				String resource = (String) cursor.next().get(RESOURCE_FIELD);
				resultList.add(resource);
			}
		} finally {
			cursor.close();
		}
		return resultList;
	}
	
	public static void main(String[] args) {
		try{
			DBpediaTopicSignaturesExtractor tse = new DBpediaTopicSignaturesExtractor("lexica", "ResourceWordCompare");
			tse.extractInformation(inFile);
			tse.createIndex();
			
			/*List<String> r = tse.searchResourceByWord("", 10);
			System.out.println(r.size());
			Iterator<String> iter = r.iterator();
			while(iter.hasNext()){
				String resource = (String) iter.next();
				System.out.println(resource);
			}*/
			
			tse.close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

}
