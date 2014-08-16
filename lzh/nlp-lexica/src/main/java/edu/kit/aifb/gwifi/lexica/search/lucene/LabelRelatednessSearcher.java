package edu.kit.aifb.gwifi.lexica.search.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import edu.kit.aifb.gwifi.lexica.index.lucene.LabelRelatednessIndexer;

public class LabelRelatednessSearcher {

	private IndexReader reader;
	private IndexSearcher searcher;
	private int resultnumber = 1000;

	public LabelRelatednessSearcher(String path, int resultNum) throws Exception {
		reader = DirectoryReader.open(FSDirectory.open(new File(path)));
		searcher = new IndexSearcher(reader);
		resultnumber = resultNum;
	}

	public LinkedHashMap<String, Double> getScoreList(String label) throws IOException {
		LinkedHashMap<String, Double> resultMap = new LinkedHashMap<String, Double>();

		Term term = new Term(LabelRelatednessIndexer.SOURCE_LABEL_FIELD, label);
		Query query = new TermQuery(term);

		Sort sort = new Sort(new SortField(LabelRelatednessIndexer.SCORE_FIELD, Type.DOUBLE, true));
		TopDocs result = searcher.search(query, resultnumber, sort);

		for (int i = 0; i < result.scoreDocs.length; i++) {
			Document firstHit = searcher.doc(result.scoreDocs[i].doc);

			double score = Double.parseDouble(firstHit.get(LabelRelatednessIndexer.SCORE_FIELD));
			String t_label = firstHit.get(LabelRelatednessIndexer.TARGET_LABEL_FIELD);
			resultMap.put(t_label, score);
		}
		return resultMap;
	}

	public void close() throws IOException {
		reader.close();
	}

	// "/index/LabelRelatedness"
	public static void main(String[] args) throws Exception {

		LabelRelatednessSearcher lr = new LabelRelatednessSearcher(args[0], Integer.parseInt(args[1]));

		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("Please input the label:");
			String label = scanner.nextLine();
			if (label.startsWith("exit"))
				break;

			LinkedHashMap<String, Double> rs = lr.getScoreList(label);
			if (rs == null)
				continue;
			if (rs.size() == 0)
				System.out.println("There is no matched result!");

			Iterator<Entry<String, Double>> iter = rs.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, Double> entry = (Map.Entry<String, Double>) iter.next();
				String t_label = entry.getKey();
				double score = entry.getValue();
				System.out.println("label:	" + t_label + "	score:	" + score);
			}
		}
		lr.close();
		scanner.close();
	}

}
