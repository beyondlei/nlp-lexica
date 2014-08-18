package edu.kit.aifb.gwifi.lexica.index.mongodb.baseline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class DBpediaLexicalizationsExtractor2 {

	static String inFile = "C:/Users/Jason/Desktop/new lexi.txt";
	static String host = "mongodb://aifb-ls3-remus.aifb.kit.edu:19000";
	
	public static String RESOURCE_FIELD = "resource";
	public static String LABEL_FIELD = "label";
	public static String LABEL_PROBABILITY_FIELD = "SfGivenUri";//p(l|r)
	public static String RESOURCE_PROBABILITY_FIELD = "UriGivenSf";//p(r|l)
	public static String PMI_FIELD = "pmi";//p(r,l)

	private MongoClient mongoClient;
	private DBCollection coll;
	private BulkWriteOperation builder;
	
	public DBpediaLexicalizationsExtractor2(String dbName, String collName)throws Exception{
		
		mongoClient = new MongoClient(new MongoClientURI(host));
		DB db = mongoClient.getDB(dbName);
		coll = db.getCollection(collName);
		builder = coll.initializeUnorderedBulkOperation();
	}
	
	public void extractInformation(String inPath) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(inPath));

		String tmp,lastresource="", lastlabel="", resource="", label="";
		double sfGivenUri=0, uriGivenSf=0, pmi=0;
		while((tmp = reader.readLine()) != null){
			
			Matcher m = Pattern.compile("(?<=/spotlight/id/).*?(?=\\>)").matcher(tmp);
			while(m.find()){
					String rl = m.group();
					String[] rls = rl.split("---");
					resource = rls[0];
					label = rls[1];
			}
			m = Pattern.compile("(?<=/spotlight/score#).*?(?=\\>)").matcher(tmp);
			while(m.find()){
				String text = m.group();
				if(text.equals("uriGivenSf")){
					Matcher m1 = Pattern.compile("(?<=\\\").*?(?=\\\")").matcher(tmp);
					while(m1.find())
						uriGivenSf = Double.parseDouble(m1.group());
				}
				if(text.equals("sfGivenUri")){
					Matcher m1 = Pattern.compile("(?<=\\\").*?(?=\\\")").matcher(tmp);
					while(m1.find())
						sfGivenUri = Double.parseDouble(m1.group());
				}
				if(text.equals("pmi")){
					Matcher m1 = Pattern.compile("(?<=\\\").*?(?=\\\")").matcher(tmp);
					while(m1.find())
						pmi = Double.parseDouble(m1.group());
				}
			}
			
			if(lastresource.equals("") && lastlabel.equals("")){
				lastresource = resource;
				lastlabel = label;
			}
			
			if(!resource.equals(lastresource) || !label.equals(lastlabel)){
				if((lastresource.length() <= 500) && (lastlabel.length() <= 500)){}
					createDocumentByBulk(lastresource, lastlabel, sfGivenUri, uriGivenSf, pmi);
				lastresource = resource;
				lastlabel = label;
			}
		}
		builder.execute();
		reader.close();
		
	}
	
	public void createDocumentByBulk(String resource, String label, double pSfGivenUri, double pUriGivenSf, double pmi){
		BasicDBObject doc = new BasicDBObject(RESOURCE_FIELD, resource).append(LABEL_FIELD, label)
				.append(LABEL_PROBABILITY_FIELD, pSfGivenUri).append(RESOURCE_PROBABILITY_FIELD, pUriGivenSf)
				.append(PMI_FIELD, pmi);
		builder.insert(doc);
	}
	
	public void createIndex() {
		coll.createIndex(new BasicDBObject(RESOURCE_FIELD, 1));
		coll.createIndex(new BasicDBObject(LABEL_FIELD, 1));
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
			DBpediaLexicalizationsExtractor2 lee = new DBpediaLexicalizationsExtractor2("lexica", "ResourceLabelCompare2");
			
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
