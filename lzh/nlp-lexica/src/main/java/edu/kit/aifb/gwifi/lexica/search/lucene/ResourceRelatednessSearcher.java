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
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import edu.kit.aifb.gwifi.lexica.index.lucene.ResourceRelatednessIndexer;

public class ResourceRelatednessSearcher {

	private IndexReader reader;
	private IndexSearcher searcher;

	public ResourceRelatednessSearcher(String path) throws Exception {
		reader = DirectoryReader.open(FSDirectory.open(new File(path)));
		searcher = new IndexSearcher(reader);
	}

	public LinkedHashMap<Integer, Double> getRelatedArticles(int id, int number, boolean sorted) throws IOException {
		LinkedHashMap<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();

		Query query = NumericRangeQuery.newIntRange(ResourceRelatednessIndexer.SOURCE_PAGE_ID_FIELD, id, id, true, true);
		
		TopDocs result;
		if (sorted) {
			Sort sort = new Sort(new SortField(ResourceRelatednessIndexer.SCORE_FIELD, Type.DOUBLE, true));
			result = searcher.search(query, number, sort);
		} else {
			result = searcher.search(query, number);
		}
		
		for (int i = 0; i < result.scoreDocs.length; i++) {
			Document firstHit = searcher.doc(result.scoreDocs[i].doc);

			double score = Double.parseDouble(firstHit.get(ResourceRelatednessIndexer.SCORE_FIELD));
			int t_id = Integer.parseInt(firstHit.get(ResourceRelatednessIndexer.TARGET_PAGE_ID_FIELD));
			
			resultMap.put(t_id, score);
		}
		return resultMap;
	}

	public double getRelatedness(int s_id, int t_id) throws IOException {

		Query s_tq = NumericRangeQuery.newIntRange(ResourceRelatednessIndexer.SOURCE_PAGE_ID_FIELD, s_id, s_id, true, true);
		
		Query t_tq = NumericRangeQuery.newIntRange(ResourceRelatednessIndexer.TARGET_PAGE_ID_FIELD, t_id, t_id, true, true);
		
		BooleanQuery query = new BooleanQuery();
		query.add(s_tq, Occur.MUST);
		query.add(t_tq, Occur.MUST);

		TopDocs result = searcher.search(query, 1);

		Document firstHit = searcher.doc(result.scoreDocs[0].doc);
		double score = Integer.parseInt(firstHit.get(ResourceRelatednessIndexer.SCORE_FIELD));

		return score;
	}

	public void close() throws IOException {
		reader.close();
	}

	// "/index/Resource" "/index/ResourceRelatedness"
	public static void main(String[] args) throws Exception {

		ResourceSearcher resourcesearcher = new ResourceSearcher(args[0]);
		ResourceRelatednessSearcher ac = new ResourceRelatednessSearcher(args[0]);
		
		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("Please input the title:");
			String title = scanner.nextLine();
			if (title.startsWith("exit"))
				break;

			int id = resourcesearcher.getIDfromTitle(title);

			LinkedHashMap<Integer, Double> rs = ac.getRelatedArticles(id, 1000, true);
			if (rs == null)
				continue;
			if (rs.size() == 0)
				System.out.println("There is no matched result!");

			Iterator<Entry<Integer, Double>> iter = rs.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) iter.next();
				int t_id = entry.getKey();
				String t_title = resourcesearcher.getTitlefromID(t_id);
				double score = entry.getValue();
				System.out.println("article:		" + t_title + "		score:		" + score);
			}
		}
		resourcesearcher.close();
		ac.close();
		scanner.close();
	}

}
