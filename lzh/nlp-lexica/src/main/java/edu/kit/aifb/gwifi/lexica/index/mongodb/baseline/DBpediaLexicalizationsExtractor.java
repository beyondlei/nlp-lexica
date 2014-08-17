package edu.kit.aifb.gwifi.lexica.index.mongodb.baseline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class DBpediaLexicalizationsExtractor {

	static String inFile = "C:/Users/Jason/Desktop/lexicalizations_en.nq";
	static String host = "mongodb://aifb-ls3-remus.aifb.kit.edu:19000";
	
	public static String RESOURCE_FIELD = "resource";
	public static String LABEL_FIELD = "label";
	public static String PURI_FIELD = "pUri";
	public static String LABEL_PROBABILITY_FIELD = "pSfGivenUri";//p(l|r)
	public static String RESOURCE_PROBABILITY_FIELD = "pUriGivenSf";//p(r|l)
	public static String PMI_FIELD = "pmi";//p(r,l)

	private MongoClient mongoClient;
	private DBCollection coll;
	private BulkWriteOperation builder;
	
	public DBpediaLexicalizationsExtractor(String dbName, String collName)throws Exception{
		
		mongoClient = new MongoClient(new MongoClientURI(host));
		DB db = mongoClient.getDB(dbName);
		coll = db.getCollection(collName);
		builder = coll.initializeUnorderedBulkOperation();
	}
	
	public void extractInformation(String inPath) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(inPath));

		int count = 0;
		String tmp,resource="",label="";
		double pUri=0,pSfGivenUri=0,pUriGivenSf=0,pmi=0;
		while((tmp = reader.readLine()) != null){
			count++;
			
			Matcher m = Pattern.compile("(?<=/resource/).*?(?=\\>)").matcher(tmp);
			while(m.find()){
				if(count == 1)
					resource = m.group();
			}
			m = Pattern.compile("(?<=\\\").*?(?=\\\")").matcher(tmp);
			while(m.find()){
				if(count == 1)
					label = m.group();
				else if(count == 2)
					pUri = Double.parseDouble(m.group());
				else if(count == 3)
					pSfGivenUri = Double.parseDouble(m.group());
				else if(count == 4)
					pUriGivenSf = Double.parseDouble(m.group());
				else if(count == 5)
					pmi = Double.parseDouble(m.group());
			}
			
			if(count == 5){
				if((resource.length() <= 500) && (label.length() <= 500))
					createDocumentByBulk(resource, label, pUri, pSfGivenUri, pUriGivenSf, pmi);
				count = 0;
			}
		}
		if(count != 5){
			switch(count){
			case 1:
				pUri = 0;
				pSfGivenUri = 0;
				pUriGivenSf = 0;
				pmi = 0;
			case 2:
				pSfGivenUri = 0;
				pUriGivenSf = 0;
				pmi = 0;
			case 3:
				pUriGivenSf = 0;
				pmi = 0;
			case 4:
				pmi = 0;
			}
			if((resource.length() <= 500) && (label.length() <= 500))
				createDocumentByBulk(resource, label, pUri, pSfGivenUri, pUriGivenSf, pmi);
		}
		builder.execute();
		reader.close();
		System.out.println(resource +"    "+ label +"     "+ pUri + "    "+ pSfGivenUri + "    "+ pUriGivenSf+"     "+ pmi);
	}
	
	public void createDocumentByBulk(String resource, String label, double pUri, double pSfGivenUri, double pUriGivenSf, double pmi){
		BasicDBObject doc = new BasicDBObject(RESOURCE_FIELD, resource).append(LABEL_FIELD, label)
				.append(PURI_FIELD, pUri).append(LABEL_PROBABILITY_FIELD, pSfGivenUri).append(RESOURCE_PROBABILITY_FIELD, pUriGivenSf)
				.append(PMI_FIELD, pmi);
		builder.insert(doc);
	}
	
	public void createIndex() {
		coll.createIndex(new BasicDBObject(RESOURCE_FIELD, 1));
		coll.createIndex(new BasicDBObject(LABEL_FIELD, 1));
		coll.createIndex(new BasicDBObject(PURI_FIELD, -1));
		coll.createIndex(new BasicDBObject(LABEL_PROBABILITY_FIELD, -1));
		coll.createIndex(new BasicDBObject(RESOURCE_PROBABILITY_FIELD, -1));
		coll.createIndex(new BasicDBObject(PMI_FIELD, -1));
	}
	
	public void close() throws IOException{
		mongoClient.close();
	}
	
	public LinkedHashMap<String, Double> searchPlrByResource(String resource, int resultNum) throws IOException {
		LinkedHashMap<String, Double> resultMap = new LinkedHashMap<String, Double>();
		BasicDBObject query = new BasicDBObject();
		query.append(RESOURCE_FIELD, resource);
		DBCursor cursor = coll.find(query).sort(new BasicDBObject(LABEL_PROBABILITY_FIELD, -1)).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				double plr = (Double)cursor.next().get(LABEL_PROBABILITY_FIELD);
				String label = (String) cursor.curr().get(LABEL_FIELD);
				resultMap.put(label, plr);
			}
		} finally {
			cursor.close();
		}
		return resultMap;
	}

	public LinkedHashMap<String, Double> searchPmiByResource(String resource, int resultNum) throws IOException {
		LinkedHashMap<String, Double> resultMap = new LinkedHashMap<String, Double>();
		BasicDBObject query = new BasicDBObject();
		query.append(RESOURCE_FIELD, resource);
		DBCursor cursor = coll.find(query).sort(new BasicDBObject(PMI_FIELD, -1)).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				double pmi = (Double)cursor.next().get(PMI_FIELD);
				String label = (String) cursor.curr().get(LABEL_FIELD);
				resultMap.put(label, pmi);
			}
		} finally {
			cursor.close();
		}
		return resultMap;
	}
	
	public LinkedHashMap<String, Double> searchPrlByLabel(String label, int resultNum) throws IOException {
		LinkedHashMap<String, Double> resultMap = new LinkedHashMap<String, Double>();
		BasicDBObject query = new BasicDBObject();
		query.append(LABEL_FIELD, label);
		DBCursor cursor = coll.find(query).sort(new BasicDBObject(RESOURCE_PROBABILITY_FIELD, -1)).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				double prl = (Double)cursor.next().get(RESOURCE_PROBABILITY_FIELD);
				String resource = (String) cursor.curr().get(RESOURCE_FIELD);
				resultMap.put(resource, prl);
			}
		} finally {
			cursor.close();
		}
		return resultMap;
	}
	
	public LinkedHashMap<String, Double> searchPmiByLabel(String label, int resultNum) throws IOException {
		LinkedHashMap<String, Double> resultMap = new LinkedHashMap<String, Double>();
		BasicDBObject query = new BasicDBObject();
		query.append(LABEL_FIELD, label);
		DBCursor cursor = coll.find(query).sort(new BasicDBObject(PMI_FIELD, -1)).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				double pmi = (Double)cursor.next().get(PMI_FIELD);
				String resource = (String) cursor.curr().get(RESOURCE_FIELD);
				resultMap.put(resource, pmi);
			}
		} finally {
			cursor.close();
		}
		return resultMap;
	}
	
	public static void main(String[] args) {
		try{
			DBpediaLexicalizationsExtractor lee = new DBpediaLexicalizationsExtractor("lexica", "ResourceLabelCompare");
			
			lee.extractInformation(inFile);
			lee.createIndex();
			
			/*LinkedHashMap<String, Double> r = lee.searchPmiByResource("Individual_reclamation", 2);
			System.out.println(r.size());
			Iterator<Entry<String, Double>> iter = r.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<String, Double> entry = (Map.Entry<String, Double>) iter.next();
				String label = (String) entry.getKey();
				double plr = (Double) entry.getValue();
				System.out.println(label+"     "+ plr);
			}*/
			
			lee.close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

}
