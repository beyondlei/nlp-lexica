package edu.kit.aifb.gwifi.lexica.index.mongodb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

import edu.kit.aifb.gwifi.lexica.index.lucene.ResourceLabelCoOccurrenceIndexer;
import edu.kit.aifb.gwifi.model.Article;
import edu.kit.aifb.gwifi.model.Wikipedia;
import edu.kit.aifb.gwifi.util.nlp.Language;

public class ResourceLabelCoOccurrenceMongoDBConverter {

	public static String LABEL_FIELD = "label";
	public static String RESOURCE_FIELD = "resource";
	public static String RESOURCE_PROBABILITY_FIELD = "P(r|l)";
	public static String LABEL_PROBABILITY_FIELD = "P(l|r)";
	public static String PMI_FIELD = "PMI(l,r)";

	private Wikipedia wikipedia;
	private IndexReader reader;

	private BufferedReader labelFreqReader;
	private BufferedReader resFreqReader;
	
	private HashMap<String, Integer> label2freq;
	private HashMap<String, Integer> res2freq;

	private MongoClient mongoClient;
	private DBCollection coll;
	private BulkWriteOperation builder;

	private Language lang;
	private long totalCoOccurrenceFreq;

	private static final Map<Language, Long> lang2totalCoOccurrenceFreq;
	static {
		Map<Language, Long> map = new HashMap<Language, Long>();
		map.put(Language.EN, 230459102l);
		map.put(Language.DE, 93931841l);
		map.put(Language.ES, 88140920l);
		map.put(Language.ZH, 30966726l);
		map.put(Language.CA, 1022815l);
		map.put(Language.SL, 45091892l);
		lang2totalCoOccurrenceFreq = Collections.unmodifiableMap(map);
	}

	public ResourceLabelCoOccurrenceMongoDBConverter(String config, String langLabel, String inputPath,
			String freqPath, String dbName, String collName, int port) throws Exception {
		File databaseDirectory = new File(config);
		wikipedia = new Wikipedia(databaseDirectory, false);
		reader = DirectoryReader.open(FSDirectory.open(new File(inputPath)));
		lang = Language.getLanguage(langLabel);
		totalCoOccurrenceFreq = lang2totalCoOccurrenceFreq.get(lang);

		labelFreqReader = new BufferedReader(new InputStreamReader(new FileInputStream(freqPath + "/labelFreqWithRes_"
				+ lang.toString() + ".txt"), "UTF8"));
		resFreqReader = new BufferedReader(new InputStreamReader(new FileInputStream(freqPath + "/resFreqWithLabel_"
				+ lang.toString() + ".txt"), "UTF8"));

		mongoClient = new MongoClient("localhost", port);
		DB db = mongoClient.getDB(dbName);
		coll = db.getCollection(collName + "_" + lang.toString());
		builder = coll.initializeUnorderedBulkOperation();

		label2freq = new HashMap<String, Integer>();
		res2freq = new HashMap<String, Integer>();
	}

	public void prepare() throws IOException {
		String lineLabelFreq;
		while ((lineLabelFreq = labelFreqReader.readLine()) != null) {
			String[] parts = lineLabelFreq.split("\t\t");
			if (parts.length == 2) {
				label2freq.put(parts[0], Integer.parseInt(parts[1]));
			} else {
				System.out.println("Parse error: " + lineLabelFreq);
			}
		}
		labelFreqReader.close();
		
		String lineResFreq;
		while ((lineResFreq = resFreqReader.readLine()) != null) {
			String[] parts = lineResFreq.split("\t\t");
			if (parts.length == 2) {
				res2freq.put(parts[0], Integer.parseInt(parts[1]));
			} else {
				System.out.println("Parse error: " + lineResFreq);
			}
		}
		resFreqReader.close();
	}

	public void convert() throws IOException {
		for (int i = 0; i < reader.maxDoc(); i++) {
			Document doc = reader.document(i);
			if (doc != null) {
				String labelText = doc.get(ResourceLabelCoOccurrenceIndexer.LABEL_FIELD);
				int freq = Integer.parseInt(doc.get(ResourceLabelCoOccurrenceIndexer.FREQUENCY_FIELD));
				int pageId = Integer.parseInt(doc.get(ResourceLabelCoOccurrenceIndexer.PAGE_ID_FIELD));

				Article article = wikipedia.getArticleById(pageId);
				String title = article.getTitle();
				
				if (labelText == null || labelText.equals("") || labelText.length() >= 500)
					continue;
				
				if (title == null || title.equals("") || title.length() >= 500)
					continue;

				int freqRes = res2freq.get(title);
				if (freqRes == 0)
					freqRes = 1;
				double resProb = ((double) freq) / freqRes;
				int freqLabel = label2freq.get(labelText);
				if (freqLabel == 0)
					freqLabel = 1;
				double labelProb = ((double) freq) / freqLabel;
				double pmi = Math.log((freq * totalCoOccurrenceFreq) / (freqRes * freqLabel));

				createDocumentByBulk(labelText, article.getTitle(), resProb, labelProb, pmi);
				
				if((i + 1) % 100000 == 0) {
					builder.execute();
					builder = coll.initializeUnorderedBulkOperation();
					
					System.out.println(i + 1 + " resource label co-occurrence relations have been processed!");
				}
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

	// "configs/wikipedia-template-en.xml" "en"
	// "/index/ResourceLabelCoOccurrence" "/index"
	// "lexica" "ResourceLabelCoOccurrence" "19000"
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		ResourceLabelCoOccurrenceMongoDBConverter converter = new ResourceLabelCoOccurrenceMongoDBConverter(args[0],
				args[1], args[2], args[3], args[4], args[5], Integer.parseInt(args[6]));
		converter.prepare();
		converter.convert();
		converter.createIndex();
		converter.close();
		long endTime = System.currentTimeMillis();
		System.out.println("Time: " + (endTime - startTime) / 1000 + "s");
	}
}

	// en
	// Number of label resource co-occurrence pairs: 104560077

	// de
	// Number of label resource co-occurrence pairs: 42316145

	// es
	// Number of label resource co-occurrence pairs: 34404641

	// zh
	// Number of label resource co-occurrence pairs: 16286187

	// ca
	// Number of label resource co-occurrence pairs: 8161564	

	// sl
	// Number of label resource co-occurrence pairs: 26638003
