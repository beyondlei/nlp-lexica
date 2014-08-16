package edu.kit.aifb.gwifi.lexica.index.mongodb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import edu.kit.aifb.gwifi.model.Page;
import edu.kit.aifb.gwifi.model.Page.PageType;
import edu.kit.aifb.gwifi.model.Wikipedia;
import edu.kit.aifb.gwifi.util.nlp.Language;

public class InterlingualResourceMongoDBIndexer {

	private static String SPLITTER = ",\t";

	private Wikipedia wikipedia;
	private BufferedReader inputReader;

	private MongoClient mongoClient;
	private DBCollection coll;
	private BulkWriteOperation builder;

	private Map<Integer, Map<String, String>> id2resources;
	
	private boolean allLanguages;

	private static final Set<Language> languages;
	static {
		Set<Language> set = new HashSet<Language>();
		set.add(Language.EN);
		set.add(Language.DE);
		set.add(Language.ES);
		set.add(Language.ZH);
		set.add(Language.CA);
		set.add(Language.SL);
		set.add(Language.FR);
		set.add(Language.IT);
		set.add(Language.PT);
		set.add(Language.RU);
		set.add(Language.SR);
		set.add(Language.EU);
		languages = Collections.unmodifiableSet(set);
	}

	public InterlingualResourceMongoDBIndexer(String config, String inputFilePath, String dbName, String collName,
			int port, boolean allLanguages) throws Exception {
		File databaseDirectory = new File(config);
		wikipedia = new Wikipedia(databaseDirectory, false);
		inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilePath), "UTF-8"));

		mongoClient = new MongoClient("localhost", port);
		DB db = mongoClient.getDB(dbName);
		coll = db.getCollection(collName);
		builder = coll.initializeUnorderedBulkOperation();
		
		id2resources = new LinkedHashMap<Integer, Map<String, String>>();
		this.allLanguages = allLanguages;
	}

	public void index() throws Exception {
		int i = 0;
		String line;
		while ((line = inputReader.readLine()) != null) {
			if (++i % 100000 == 0)
				System.out.println(i + " langlinks have been processed.");
			String[] splits = line.split(SPLITTER);

			if (splits.length != 3)
				continue;

			String targetTitle = splits[2].trim();
			if (targetTitle.equals("") || targetTitle.length() >= 1000)
				continue;

			String langLabel = splits[1].trim();
			Language lang = Language.getLanguage(langLabel);
			if (!languages.contains(lang) && allLanguages == false)
				continue;
			
			int sourcePageId = Integer.parseInt(splits[0]);
			Page page = wikipedia.getPageById(sourcePageId);
			if (page == null || !page.getType().equals(PageType.article))
				continue;
			
			String title = page.getTitle();
			if (title == null || title.equals("") || title.length() >= 1000)
				continue;

			Map<String, String> resources = id2resources.get(sourcePageId);
			if (resources == null) {
				resources = new LinkedHashMap<String, String>();
				resources.put(Language.EN.toString(), title);
				id2resources.put(sourcePageId, resources);
			}
			resources.put(langLabel, targetTitle);
		}

		for (int id : id2resources.keySet()) {
			createDocumentByBulk(id2resources.get(id));
		}

		builder.execute();
	}

	public void createDocumentByBulk(Map<String, String> resources) {
		BasicDBObject doc = new BasicDBObject();
		for (String lang : resources.keySet()) {
			doc.append(lang.toString(), resources.get(lang));
		}
		builder.insert(doc);
	}

	public void createIndex() {
		for (Language lang : languages) {
			coll.createIndex(new BasicDBObject(lang.toString(), 1));
		}
	}

	public void close() throws IOException {
		inputReader.close();
		mongoClient.close();
	}

	// "configs/wikipedia-template-en.xml" "/data/langlinks_en.csv"
	// "lexica" "InterlingualResource" "19000", "false"
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		InterlingualResourceMongoDBIndexer indexer = new InterlingualResourceMongoDBIndexer(args[0], args[1], args[2],
				args[3], Integer.parseInt(args[4]), Boolean.parseBoolean(args[5]));
		indexer.index();
		indexer.createIndex();
		indexer.close();
		long endTime = System.currentTimeMillis();
		System.out.println("Time: " + (endTime - startTime) / 1000 + "s");
	}
	
	// 12 languages
	// 1700034 records
	
	// all langauges
	// 2255885 records

}