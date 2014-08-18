package edu.kit.aifb.gwifi.lexica.index.mongodb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import edu.kit.aifb.gwifi.lexica.index.lucene.ResourceWordCoOccurrenceIndexer;
import edu.kit.aifb.gwifi.model.Article;
import edu.kit.aifb.gwifi.model.Wikipedia;
import edu.kit.aifb.gwifi.util.nlp.Language;

public class ResourceWordCoOccurrenceMongoDBPreparer {

	public static String WORD_FIELD = "word";
	public static String RESOURCE_FIELD = "resource";
	public static String RESOURCE_PROBABILITY_FIELD = "P(r|w)";
	public static String WORD_PROBABILITY_FIELD = "P(w|r)";
	public static String PMI_FIELD = "PMI(w,r)";

	private Wikipedia wikipedia;
	private IndexReader reader;

	private BufferedWriter wordFreqOutput;
	private BufferedWriter resFreqOutput;
	private BufferedWriter totalFreqOutput;

	private HashMap<String, Integer> word2freq;
	private HashMap<String, Integer> res2freq;

	private Language lang;
	private long totalCoOccurrenceFreq;

	public ResourceWordCoOccurrenceMongoDBPreparer(String config, String langLabel, String inputPath, String outputPath)
			throws Exception {
		File databaseDirectory = new File(config);
		wikipedia = new Wikipedia(databaseDirectory, false);
		reader = DirectoryReader.open(FSDirectory.open(new File(inputPath)));
		lang = Language.getLanguage(langLabel);
		
		word2freq = new HashMap<String, Integer>();
		res2freq = new HashMap<String, Integer>();

		wordFreqOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath
				+ "/wordFreqWithRes_" + lang.toString() + ".txt"), "UTF-8"));
		resFreqOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath + "/resFreqWithWord_"
				+ lang.toString() + ".txt"), "UTF-8"));
		totalFreqOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath
				+ "/totalResourceWordCoOccurrenceFreq_" + lang.toString() + ".txt"), "UTF-8"));
	}

	public void prepare() throws IOException {
		for (int i = 0; i < reader.maxDoc(); i++) {

			if ((i + 1) % 100000 == 0)
				System.out.println(i + 1 + " resource word co-occurrence relations have been processed!");

			Document doc = reader.document(i);
			if (doc != null) {
				int id = Integer.parseInt(doc.get(ResourceWordCoOccurrenceIndexer.PAGE_ID_FIELD));
				String word = doc.get(ResourceWordCoOccurrenceIndexer.WORD_FIELD);
				int freq = Integer.parseInt(doc.get(ResourceWordCoOccurrenceIndexer.FREQUENCY_FIELD));
				totalCoOccurrenceFreq += freq;

				if (word == null || word.equals("") || word.length() >= 500)
					continue;

				Article article = wikipedia.getArticleById(id);
				String title = article.getTitle();
				if (title == null || title.equals("") || title.length() >= 500)
					continue;

				Integer wordFreq = word2freq.get(word);
				if (wordFreq == null) {
					wordFreq = 0;
				}
				word2freq.put(word, wordFreq + freq);

				Integer resFreq = res2freq.get(title);
				if (resFreq == null) {
					resFreq = 0;
				}
				res2freq.put(title, resFreq + freq);
			}
		}
		reader.close();
		System.out.println("Resource word co-occurrence relations procesing finished!");

		for (String word : word2freq.keySet()) {
			wordFreqOutput.write(word + "\t\t" + word2freq.get(word));
			wordFreqOutput.newLine();
		}
		wordFreqOutput.close();

		for (String res : res2freq.keySet()) {
			resFreqOutput.write(res + "\t\t" + res2freq.get(res));
			resFreqOutput.newLine();
		}
		resFreqOutput.close();

		totalFreqOutput.write(String.valueOf(totalCoOccurrenceFreq));
		totalFreqOutput.close();
		System.out.println("total resource word co-occurrence freqencey: " + totalCoOccurrenceFreq);
	}

	// "configs/wikipedia-template-en.xml" "en"
	// "/index/ResourceWordCoOccurrence" "/index"
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		ResourceWordCoOccurrenceMongoDBPreparer converter = new ResourceWordCoOccurrenceMongoDBPreparer(args[0],
				args[1], args[2], args[3]);
		converter.prepare();
		long endTime = System.currentTimeMillis();
		System.out.println("Time: " + (endTime - startTime) / 1000 + "s");
	}
}

	// en
	//Total resource word co-occurrence freqencey: 972776610

	// de
	//Total resource word co-occurrence freqencey: 497777722 

	// es
	//Total resource word co-occurrence freqencey: 354800561

	// zh	
	//Total resource word co-occurrence freqencey: 29241789

	// ca
	//Total resource word co-occurrence freqencey: 74715579

	// sl 
	// Total resource word co-occurrence freqencey: 81636520
