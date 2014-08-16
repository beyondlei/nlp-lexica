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

import edu.kit.aifb.gwifi.lexica.index.mongodb.ResourceLabelCoOccurrenceMongoDBConverter;


public class ResourceLabelCoOccurrenceMongoDBSearcher {
	
	static String host = "mongodb://aifb-ls3-remus.aifb.kit.edu:19000";
	
	private MongoClient mongoClient;
	private DBCollection coll;

	public ResourceLabelCoOccurrenceMongoDBSearcher(String host, String dbName, String collName, String lang)throws Exception{
		
		mongoClient = new MongoClient(new MongoClientURI(host));
		DB db = mongoClient.getDB(dbName);
		coll = db.getCollection(collName + "_" + lang);
	}

	public LinkedHashMap<String, Double> searchPlrByResource(String resource, int resultNum) throws IOException {
		LinkedHashMap<String, Double> resultMap = new LinkedHashMap<String, Double>();

		Pattern pattern = Pattern.compile("^"+resource+"$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject(ResourceLabelCoOccurrenceMongoDBConverter.RESOURCE_FIELD,resource);
		DBCursor cursor = coll.find(query).sort(new BasicDBObject(ResourceLabelCoOccurrenceMongoDBConverter.LABEL_PROBABILITY_FIELD, -1)).limit(resultNum);
		
		try {
			while (cursor.hasNext()) {
				double plr = (Double)cursor.next().get(ResourceLabelCoOccurrenceMongoDBConverter.LABEL_PROBABILITY_FIELD);
				String label = (String) cursor.curr().get(ResourceLabelCoOccurrenceMongoDBConverter.LABEL_FIELD);
				resultMap.put(label, plr);
			}
		} finally {
			cursor.close();
		}
		return resultMap;
	}

	public LinkedHashMap<String, Double> searchPmiByResource(String resource, int resultNum) throws IOException {
		LinkedHashMap<String, Double> resultMap = new LinkedHashMap<String, Double>();

		Pattern pattern = Pattern.compile("^"+resource+"$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject(ResourceLabelCoOccurrenceMongoDBConverter.RESOURCE_FIELD, resource);
		DBCursor cursor = coll.find(query).sort(new BasicDBObject(ResourceLabelCoOccurrenceMongoDBConverter.PMI_FIELD, -1)).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				double pmi =(Double)cursor.next().get(ResourceLabelCoOccurrenceMongoDBConverter.PMI_FIELD);
				String label = (String) cursor.curr().get(ResourceLabelCoOccurrenceMongoDBConverter.LABEL_FIELD);
				resultMap.put(label, pmi);
			}
		} finally {
			cursor.close();
		}
		return resultMap;
	}
	
	public LinkedHashMap<String, Double> searchPrlByLabel(String label, int resultNum) throws IOException {
		LinkedHashMap<String, Double> resultMap = new LinkedHashMap<String, Double>();

		Pattern pattern = Pattern.compile("^"+label+"$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject(ResourceLabelCoOccurrenceMongoDBConverter.LABEL_FIELD, label);
		DBCursor cursor = coll.find(query).sort(new BasicDBObject(ResourceLabelCoOccurrenceMongoDBConverter.RESOURCE_PROBABILITY_FIELD, -1)).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				double prl = (Double)cursor.next().get(ResourceLabelCoOccurrenceMongoDBConverter.RESOURCE_PROBABILITY_FIELD);
				String resource = (String) cursor.curr().get(ResourceLabelCoOccurrenceMongoDBConverter.RESOURCE_FIELD);
				resultMap.put(resource, prl);
			}
		} finally {
			cursor.close();
		}
		return resultMap;
	}
	
	public LinkedHashMap<String, Double> searchPmiByLabel(String label, int resultNum) throws IOException {
		LinkedHashMap<String, Double> resultMap = new LinkedHashMap<String, Double>();

		Pattern pattern = Pattern.compile("^"+label+"$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject(ResourceLabelCoOccurrenceMongoDBConverter.LABEL_FIELD, label);
		DBCursor cursor = coll.find(query).sort(new BasicDBObject(ResourceLabelCoOccurrenceMongoDBConverter.PMI_FIELD, -1)).limit(resultNum);
		try {
			while (cursor.hasNext()) {
				double pmi = (Double)cursor.next().get(ResourceLabelCoOccurrenceMongoDBConverter.PMI_FIELD);
				String resource = (String) cursor.curr().get(ResourceLabelCoOccurrenceMongoDBConverter.RESOURCE_FIELD);
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

		ResourceLabelCoOccurrenceMongoDBSearcher rlc = new ResourceLabelCoOccurrenceMongoDBSearcher(host,"lexica", "ResourceLabelCoOccurrence", "en");

		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("Please input the title as format \"title:xx\" or the label as format \"label:xx\":");
			String text = scanner.nextLine();
			if (text.startsWith("exit"))
				break;
			String pre = text.split(":")[0];
			if (pre.equals("title")) {
				String title = text.split(":")[1];

				LinkedHashMap<String, Double> rs = rlc.searchPmiByResource(title, 10);
				if (rs == null)
					continue;
				if (rs.size() == 0)
					System.out.println("There is no matched result!");

				Iterator<Entry<String, Double>> iter = rs.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, Double> entry = (Map.Entry<String, Double>) iter.next();
					String label = entry.getKey();
					double score = entry.getValue();
					System.out.println("label:	" + label + "	score:	" + score);
				}
			} else if (pre.equals("label")) {
				String label = text.split(":")[1];
				LinkedHashMap<String, Double> rs = rlc.searchPmiByLabel(label, 10);
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
		rlc.close();
		scanner.close();
	}

}
