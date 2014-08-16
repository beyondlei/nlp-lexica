package edu.kit.aifb.gwifi.lexica.index.mongodb;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import edu.kit.aifb.gwifi.lexica.index.lucene.LabelResourceSenseIndexer;
import edu.kit.aifb.gwifi.model.Article;
import edu.kit.aifb.gwifi.model.Label;
import edu.kit.aifb.gwifi.model.Wikipedia;
import edu.kit.aifb.gwifi.util.nlp.Language;

public class LabelResourceSenseMongoDBConverter {

	public static String LABEL_FIELD = "label";
	public static String RESOURCE_FIELD = "resource";
	public static String RESOURCE_PROBABILITY_FIELD = "P(r|l)";
	public static String LABEL_PROBABILITY_FIELD = "P(l|r)";
	public static String PMI_FIELD = "PMI(l,r)";

	private Wikipedia wikipedia;
	private IndexReader reader;

	private MongoClient mongoClient;
	private DBCollection coll;
	private BulkWriteOperation builder;

	private Language lang;
	private long totalLinks;

	private static final Map<Language, Integer> lang2totalLinks;
	static {
		Map<Language, Integer> map = new HashMap<Language, Integer>();
		map.put(Language.EN, 92159538);
		map.put(Language.DE, 36042422);
		map.put(Language.ES, 22551993);
		map.put(Language.ZH, 10299531);
		map.put(Language.CA, 7086936);
		map.put(Language.SL, 2165944);
		lang2totalLinks = Collections.unmodifiableMap(map);
	}

	public LabelResourceSenseMongoDBConverter(String config, String langLabel, String inputPath, String dbName,
			String collName, int port) throws Exception {
		File databaseDirectory = new File(config);
		wikipedia = new Wikipedia(databaseDirectory, false);
		lang = Language.getLanguage(langLabel);
		totalLinks = lang2totalLinks.get(lang);
		reader = DirectoryReader.open(FSDirectory.open(new File(inputPath)));

		mongoClient = new MongoClient("localhost", port);
		DB db = mongoClient.getDB(dbName);
		coll = db.getCollection(collName + "_" + langLabel.toString());
		builder = coll.initializeUnorderedBulkOperation();

	}

	public void convert() throws IOException {
		int j = 0;
		for (int i = 0; i < reader.maxDoc(); i++) {
			Document doc = reader.document(i);
			if (doc != null) {
				
				if (++j % 1000 == 0)
					System.out.println(j + " labels have been processed!");
				
				String labelText = doc.get(LabelResourceSenseIndexer.LABEL_FIELD);
				if(labelText == null || labelText.equals("") || labelText.length() >= 500) 
					continue;
				double score = Double.parseDouble(doc.get(LabelResourceSenseIndexer.SCORE_FIELD));
				int pageId = Integer.parseInt(doc.get(LabelResourceSenseIndexer.PAGE_ID_FIELD));
				int senseLinkOccCount = Integer.parseInt(doc.get(LabelResourceSenseIndexer.SENSE_LINK_OCC_COUNT_FIELD));

				Article article = wikipedia.getArticleById(pageId);
				String title = article.getTitle();
				if (title == null || title.equals("") || title.length() >= 500)
					continue;
				int resLinksCount = article.getTotalLinksInCount();
				if(resLinksCount == 0)
					resLinksCount = 1;

				Label label = wikipedia.getLabel(labelText, null);
				long labelLinksCount = label.getLinkOccCount();
				if(labelLinksCount == 0)
					labelLinksCount = 1;

				double resProb = score;
				double labelProb = ((double) senseLinkOccCount) / labelLinksCount;
				double pmi = Math.log((senseLinkOccCount * totalLinks) / (resLinksCount * labelLinksCount));

				createDocumentByBulk(labelText, article.getTitle(), resProb, labelProb, pmi);
			}
		}
		builder.execute();
	}

	public void createDocumentByBulk(String label, String resource, double resProb, double labelProb, double pmi) {

		BasicDBObject doc = new BasicDBObject(LABEL_FIELD, label).append(RESOURCE_FIELD, resource)
				.append(RESOURCE_PROBABILITY_FIELD, resProb).append(LABEL_PROBABILITY_FIELD, labelProb)
				.append(PMI_FIELD, pmi);
		builder.insert(doc);
	}

	public void createIndex() {
		coll.createIndex(new BasicDBObject(LABEL_FIELD, 1));
		coll.createIndex(new BasicDBObject(RESOURCE_FIELD, 1));
		coll.createIndex(new BasicDBObject(RESOURCE_PROBABILITY_FIELD, -1));
		coll.createIndex(new BasicDBObject(LABEL_PROBABILITY_FIELD, -1));
		coll.createIndex(new BasicDBObject(PMI_FIELD, -1));
	}

	public void close() throws IOException {
		reader.close();
		mongoClient.close();
	}

	// "configs/wikipedia-template-en.xml" "en" "/index/LabelResourceSense"
	// "lexica" "LabelResourceSense" "19000"
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		LabelResourceSenseMongoDBConverter converter = new LabelResourceSenseMongoDBConverter(args[0], args[1],
				args[2], args[3], args[4], Integer.parseInt(args[5]));
		converter.convert();
		converter.createIndex();
		converter.close();
		long endTime = System.currentTimeMillis();
		System.out.println("Time: " + (endTime - startTime) / 1000 + "s");
	}
}

	// en
	// Number of label resource sense pairs: 15237596

	// de
	// Number of label resource sense pairs: 5342851

	// es
	// Number of label resource sense pairs: 3563379

	// zh
	// Number of label resource sense pairs: 1425827

	// ca
	// Number of label resource sense pairs: 1022815	

	// sl
	// Number of label resource sense pairs: 380517
