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

import edu.kit.aifb.gwifi.lexica.index.lucene.ResourceLabelCoOccurrenceIndexer;
import edu.kit.aifb.gwifi.model.Article;
import edu.kit.aifb.gwifi.model.Wikipedia;
import edu.kit.aifb.gwifi.util.nlp.Language;

public class ResourceLabelCoOccurrenceMongoDBPreparer {

	public static String LABEL_FIELD = "label";
	public static String RESOURCE_FIELD = "resource";
	public static String RESOURCE_PROBABILITY_FIELD = "P(r|l)";
	public static String LABEL_PROBABILITY_FIELD = "P(l|r)";
	public static String PMI_FIELD = "PMI(l,r)";

	private Wikipedia wikipedia;
	private IndexReader reader;

	private BufferedWriter labelFreqOutput;
	private BufferedWriter resFreqOutput;
	private BufferedWriter totalFreqOutput;

	private HashMap<String, Integer> label2freq;
	private HashMap<String, Integer> res2freq;

	private Language lang;
	private long totalCoOccurrenceFreq;

	public ResourceLabelCoOccurrenceMongoDBPreparer(String config, String langLabel, String inputPath, String outputPath)
			throws Exception {
		File databaseDirectory = new File(config);
		wikipedia = new Wikipedia(databaseDirectory, false);
		reader = DirectoryReader.open(FSDirectory.open(new File(inputPath)));
		lang = Language.getLanguage(langLabel);

		label2freq = new HashMap<String, Integer>();
		res2freq = new HashMap<String, Integer>();

		labelFreqOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath
				+ "/labelFreqWithRes_" + lang.toString() + ".txt"), "UTF-8"));
		resFreqOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath
				+ "/resFreqWithLabel_" + lang.toString() + ".txt"), "UTF-8"));
		totalFreqOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath
				+ "/totalResourceLabelCoOccurrenceFreq_" + lang.toString() + ".txt"), "UTF-8"));
	}

	public void prepare() throws IOException {
		for (int i = 0; i < reader.maxDoc(); i++) {

			if ((i + 1) % 100000 == 0)
				System.out.println(i + 1 + " resource label co-occurrence relations have been processed!");

			Document doc = reader.document(i);
			if (doc != null) {
				int id = Integer.parseInt(doc.get(ResourceLabelCoOccurrenceIndexer.PAGE_ID_FIELD));
				String labelText = doc.get(ResourceLabelCoOccurrenceIndexer.LABEL_FIELD);
				int freq = Integer.parseInt(doc.get(ResourceLabelCoOccurrenceIndexer.FREQUENCY_FIELD));
				totalCoOccurrenceFreq += freq;

				if (labelText == null || labelText.equals(""))
					continue;

				Article article = wikipedia.getArticleById(id);
				String title = article.getTitle();
				if (title == null || title.equals(""))
					continue;

				Integer labelFreq = label2freq.get(labelText);
				if (labelFreq == null) {
					labelFreq = 0;
				}
				label2freq.put(labelText, labelFreq + freq);

				Integer resFreq = res2freq.get(title);
				if (resFreq == null) {
					resFreq = 0;
				}
				res2freq.put(title, resFreq + freq);
			}
		}
		reader.close();
		System.out.println("Resource label co-occurrence relations procesing finished!");

		for (String label : label2freq.keySet()) {
			labelFreqOutput.write(label + "\t\t" + label2freq.get(label));
			labelFreqOutput.newLine();
		}
		labelFreqOutput.close();

		for (String res : res2freq.keySet()) {
			resFreqOutput.write(res + "\t\t" + res2freq.get(res));
			resFreqOutput.newLine();
		}
		resFreqOutput.close();

		totalFreqOutput.write(String.valueOf(totalCoOccurrenceFreq));
		totalFreqOutput.close();
		System.out.println("total resource label co-occurrence freqencey: " + totalCoOccurrenceFreq);
	}

	// "configs/wikipedia-template-en.xml" "en"
	// "/index/ResourceLabelCoOccurrence" "/index"
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		ResourceLabelCoOccurrenceMongoDBPreparer converter = new ResourceLabelCoOccurrenceMongoDBPreparer(args[0],
				args[1], args[2], args[3]);
		converter.prepare();
		long endTime = System.currentTimeMillis();
		System.out.println("Time: " + (endTime - startTime) / 1000 + "s");
	}
}


	// en
	// Total resource label co-occurrence freqencey: 230459102

	// de
	// Total resource label co-occurrence freqencey: 93931841

	// es
	// Total resource label co-occurrence freqencey: 88140920

	// zh
	// Total resource label co-occurrence freqencey: 30966726

	// ca
	// Total resource label co-occurrence freqencey: 1022815

	// sl
	// Total resource label co-occurrence freqencey: 45091892

