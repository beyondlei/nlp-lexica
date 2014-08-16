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
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import edu.kit.aifb.gwifi.lexica.index.lucene.LabelResourceSenseIndexer;

public class LabelResourceSenseSearcher {

	private IndexReader reader;
	private IndexSearcher searcher;

	public LabelResourceSenseSearcher(String path, int resultNum) throws Exception {
		reader = DirectoryReader.open(FSDirectory.open(new File(path)));
		searcher = new IndexSearcher(reader);
	}

	public LinkedHashMap<String, Double> getLabels(int id, int number, boolean sorted) throws IOException {
		LinkedHashMap<String, Double> resultMap = new LinkedHashMap<String, Double>();

		Query query = NumericRangeQuery.newIntRange(LabelResourceSenseIndexer.PAGE_ID_FIELD, id, id, true, true);

		TopDocs result;
		if (sorted) {
			Sort sort = new Sort(new SortField(LabelResourceSenseIndexer.SCORE_FIELD, Type.DOUBLE, true));
			result = searcher.search(query, number, sort);
		} else {
			result = searcher.search(query, number);
		}
		
		for (int i = 0; i < result.scoreDocs.length; i++) {
			Document firstHit = searcher.doc(result.scoreDocs[i].doc);

			double score = Double.parseDouble(firstHit.get(LabelResourceSenseIndexer.SCORE_FIELD));
			String label = firstHit.get(LabelResourceSenseIndexer.LABEL_FIELD);
			resultMap.put(label, score);
		}
		return resultMap;
	}

	public LinkedHashMap<Integer, Double> getArticles(String label, int number, boolean sorted) throws IOException {
		LinkedHashMap<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();

		Term term = new Term(LabelResourceSenseIndexer.LABEL_FIELD, label);
		Query query = new TermQuery(term);

		TopDocs result;
		if (sorted) {
			Sort sort = new Sort(new SortField(LabelResourceSenseIndexer.SCORE_FIELD, Type.DOUBLE, true));
			result = searcher.search(query, number, sort);
		} else {
			result = searcher.search(query, number);
		}
		
		for (int i = 0; i < result.scoreDocs.length; i++) {
			Document firstHit = searcher.doc(result.scoreDocs[i].doc);

			double score = Double.parseDouble(firstHit.get(LabelResourceSenseIndexer.SCORE_FIELD));
			int articleID = Integer.parseInt(firstHit.get(LabelResourceSenseIndexer.PAGE_ID_FIELD));
			resultMap.put(articleID, score);
		}
		return resultMap;
	}

	public void close() throws IOException {
		reader.close();
	}

	// "/index/Resource" "/index/LabelResourceSense"
	public static void main(String[] args) throws Exception {

		ResourceSearcher resourcesearcher = new ResourceSearcher(args[0]);
		LabelResourceSenseSearcher lr = new LabelResourceSenseSearcher(args[0], Integer.parseInt(args[1]));

		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("Please input the title as format \"title:xx\" or the label as format \"label:xx\":");
			String text = scanner.nextLine();
			if (text.startsWith("exit"))
				break;
			String pre = text.split(":")[0];
			if (pre.equals("title")) {
				String title = text.split(":")[1];
				int id = resourcesearcher.getIDfromTitle(title);

				LinkedHashMap<String, Double> rs = lr.getLabels(id, 1000, true);
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
				LinkedHashMap<Integer, Double> rs = lr.getArticles(label, 1000, true);
				if (rs == null)
					continue;
				if (rs.size() == 0)
					System.out.println("There is no matched result!");

				Iterator<Entry<Integer, Double>> iter = rs.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) iter.next();
					int articleID = entry.getKey();
					String title = resourcesearcher.getTitlefromID(articleID);
					double score = entry.getValue();
					System.out.println("article:	" + title + "	score:	" + score);
				}
			}
		}
		resourcesearcher.close();
		lr.close();
		scanner.close();
	}

}
