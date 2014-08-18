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

import edu.kit.aifb.gwifi.lexica.Environment;
import edu.kit.aifb.gwifi.lexica.index.lucene.ResourceWordCoOccurrenceIndexer;
import edu.kit.aifb.gwifi.model.Article;
import edu.kit.aifb.gwifi.model.Wikipedia;
import edu.kit.aifb.gwifi.util.nlp.Language;

public class ResourceWordCoOccurrenceMongoDBConverter {

	public static String WORD_FIELD = "word";
	public static String RESOURCE_FIELD = "resource";
	public static String RESOURCE_PROBABILITY_FIELD = "P(r|w)";
	public static String WORD_PROBABILITY_FIELD = "P(w|r)";
	public static String PMI_FIELD = "PMI(w,r)";

	private Wikipedia wikipedia;
	private IndexReader reader;

	private BufferedReader wordFreqReader;
	private BufferedReader resFreqReader;

	private HashMap<String, Integer> word2freq;
	private HashMap<String, Integer> res2freq;

	private MongoClient mongoClient;
	private DBCollection coll;
	private BulkWriteOperation builder;

	private Language lang;
	private long totalCoOccurrenceFreq;

	// private static final Map<Language, Long> lang2totalCoOccurrenceFreq;
	// static {
	// Map<Language, Long> map = new HashMap<Language, Long>();
	// map.put(Language.EN, 972776610l);
	// map.put(Language.DE, 497777722l);
	// map.put(Language.ES, 354800561l);
	// map.put(Language.ZH, 29241789l);
	// map.put(Language.CA, 74715579l);
	// map.put(Language.SL, 81636520l);
	// lang2totalCoOccurrenceFreq = Collections.unmodifiableMap(map);
	// }

	public ResourceWordCoOccurrenceMongoDBConverter(String config, String langLabel, String inputPath, String freqPath,
			String dbName, String collName, String host, int port) throws Exception {
		File databaseDirectory = new File(config);
		wikipedia = new Wikipedia(databaseDirectory, false);
		reader = DirectoryReader.open(FSDirectory.open(new File(inputPath)));
		lang = Language.getLanguage(langLabel);

		// totalCoOccurrenceFreq = lang2totalCoOccurrenceFreq.get(lang);
		BufferedReader totalFreqReader = new BufferedReader(new InputStreamReader(new FileInputStream(freqPath
				+ "/totalResourceWordCoOccurrenceFreq_" + lang.toString() + ".txt"), "UTF-8"));
		totalCoOccurrenceFreq = Long.valueOf(totalFreqReader.readLine());
		totalFreqReader.close();

		wordFreqReader = new BufferedReader(new InputStreamReader(new FileInputStream(freqPath + "/wordFreqWithRes_"
				+ lang.toString() + ".txt"), "UTF-8"));
		resFreqReader = new BufferedReader(new InputStreamReader(new FileInputStream(freqPath + "/resFreqWithWord_"
				+ lang.toString() + ".txt"), "UTF-8"));

		mongoClient = new MongoClient(host, port);
		DB db = mongoClient.getDB(dbName);
		coll = db.getCollection(collName + "_" + lang.toString());
		builder = coll.initializeUnorderedBulkOperation();

		word2freq = new HashMap<String, Integer>();
		res2freq = new HashMap<String, Integer>();
	}

	public void prepare() throws IOException {
		String lineLabelFreq;
		while ((lineLabelFreq = wordFreqReader.readLine()) != null) {
			String[] parts = lineLabelFreq.split("\t\t");
			if (parts.length == 2) {
				word2freq.put(parts[0], Integer.parseInt(parts[1]));
			} else {
				System.out.println("Parse error: " + lineLabelFreq);
			}
		}
		wordFreqReader.close();

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
				String word = doc.get(ResourceWordCoOccurrenceIndexer.WORD_FIELD);
				int freq = Integer.parseInt(doc.get(ResourceWordCoOccurrenceIndexer.FREQUENCY_FIELD));
				int pageId = Integer.parseInt(doc.get(ResourceWordCoOccurrenceIndexer.PAGE_ID_FIELD));

				Article article = wikipedia.getArticleById(pageId);
				String title = article.getTitle();
				
				if (title == null || title.equals("") || title.length() >= Environment.INDEX_LENGTH_THRESHOLD)
					continue;

				if (freq == 0)
					freq = 1;
				
				int freqRes = res2freq.get(article.getTitle());
				if (freqRes == 0)
					freqRes = 1;
				
				int freqWord = word2freq.get(word);
				if (freqWord == 0)
					freqWord = 1;
				
				double resProbGivenWord = ((double) freq) / freqWord;
				double wordProbGivenRes = ((double) freq) / freqRes;
				double pmi = Math.log((freq * totalCoOccurrenceFreq) / (freqRes * freqWord));

				createDocumentByBulk(word, title, resProbGivenWord, wordProbGivenRes, pmi);

				if ((i + 1) % 100000 == 0) {
					builder.execute();
					builder = coll.initializeUnorderedBulkOperation();

					System.out.println(i + 1 + " resource word co-occurrence relations have been processed!");
				}
			}
		}
		builder.execute();
	}

	public void createDocumentByBulk(String label, String resource, double resProb, double labelProb, double pmi) {

		BasicDBObject doc = new BasicDBObject(WORD_FIELD, label).append(RESOURCE_FIELD, resource)
				.append(RESOURCE_PROBABILITY_FIELD, resProb).append(WORD_PROBABILITY_FIELD, labelProb)
				.append(PMI_FIELD, pmi);
		builder.insert(doc);
	}

	public void createIndex() {
		coll.createIndex(new BasicDBObject(WORD_FIELD, 1));
		coll.createIndex(new BasicDBObject(RESOURCE_FIELD, 1));
		coll.createIndex(new BasicDBObject(RESOURCE_PROBABILITY_FIELD, -1));
		coll.createIndex(new BasicDBObject(WORD_PROBABILITY_FIELD, -1));
		coll.createIndex(new BasicDBObject(PMI_FIELD, -1));
	}

	public void close() throws IOException {
		reader.close();
		mongoClient.close();
	}

	// "configs/wikipedia-template-en.xml" "en"
	// "/index/ResourceWordCoOccurrence"
	// "lexica" "ResourceWordCoOccurrence" "localhost" "19000"
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		ResourceWordCoOccurrenceMongoDBConverter converter = new ResourceWordCoOccurrenceMongoDBConverter(args[0],
				args[1], args[2], args[3], args[4], args[5], args[6], Integer.parseInt(args[7]));
		converter.prepare();
		converter.convert();
		converter.createIndex();
		converter.close();
		long endTime = System.currentTimeMillis();
		System.out.println("Time: " + (endTime - startTime) / 1000 + "s");
	}
}

// en
// Number of word resource co-occurrence pairs: 313266917

// de
// Number of word resource co-occurrence pairs: 172033719

// es
// Number of word resource co-occurrence pairs: 106951335

// zh
// Number of word resource co-occurrence pairs: 19851666

// ca
// Number of word resource co-occurrence pairs: 29753250

// sl
// Number of word resource co-occurrence pairs: 25249677

