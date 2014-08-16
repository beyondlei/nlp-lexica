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

import edu.kit.aifb.gwifi.lexica.index.lucene.ResourceCoOccurrenceIndexer;

public class ResourceCoOccurrenceSearcher {

	private IndexReader reader;
	private IndexSearcher searcher;

	public ResourceCoOccurrenceSearcher(String path) throws Exception {
		reader = DirectoryReader.open(FSDirectory.open(new File(path)));
		searcher = new IndexSearcher(reader);
	}

	public LinkedHashMap<Integer, Integer> getCoOccurrences(int id, int number, boolean sorted) throws IOException {
		LinkedHashMap<Integer, Integer> resultMap = new LinkedHashMap<Integer, Integer>();

		Query query = NumericRangeQuery.newIntRange(ResourceCoOccurrenceIndexer.SOURCE_PAGE_ID_FIELD, id, id, true, true);

		TopDocs result;
		if(sorted) {
			Sort sort = new Sort(new SortField(ResourceCoOccurrenceIndexer.FREQUENCY_FIELD, Type.INT, true));
			result = searcher.search(query, number, sort);
		} else {
			result = searcher.search(query, number);
		}


		for (int i = 0; i < result.scoreDocs.length; i++) {
			Document firstHit = searcher.doc(result.scoreDocs[i].doc);

			int frequency = Integer.parseInt(firstHit.get(ResourceCoOccurrenceIndexer.FREQUENCY_FIELD));
			int t_id = Integer.parseInt(firstHit.get(ResourceCoOccurrenceIndexer.TARGET_PAGE_ID_FIELD));
			resultMap.put(t_id, frequency);
		}
		return resultMap;
	}

	public int getCoOccurrence(int s_id, int t_id) throws IOException {

		Query s_tq = NumericRangeQuery.newIntRange(ResourceCoOccurrenceIndexer.SOURCE_PAGE_ID_FIELD, s_id, s_id, true, true);
		Query t_tq = NumericRangeQuery.newIntRange(ResourceCoOccurrenceIndexer.TARGET_PAGE_ID_FIELD, t_id, t_id, true, true);
		
		BooleanQuery query = new BooleanQuery();
		query.add(s_tq, Occur.MUST);
		query.add(t_tq, Occur.MUST);

		TopDocs result = searcher.search(query, 1);

		Document firstHit = searcher.doc(result.scoreDocs[0].doc);
		int frequency = Integer.parseInt(firstHit.get(ResourceCoOccurrenceIndexer.FREQUENCY_FIELD));

		return frequency;
	}

	public void close() throws IOException {
		reader.close();
	}

	// "/index/Resource" "/index/ResourceCoOccurrence"
	public static void main(String[] args) throws Exception {

		ResourceSearcher resourcesearcher = new ResourceSearcher(args[0]);
		ResourceCoOccurrenceSearcher ac = new ResourceCoOccurrenceSearcher(args[0]);

		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("Please input the title:");
			String title = scanner.nextLine();
			if (title.startsWith("exit"))
				break;

			int id = resourcesearcher.getIDfromTitle(title);

			LinkedHashMap<Integer, Integer> rs = ac.getCoOccurrences(id, 1000, true);
			if (rs == null)
				continue;
			if (rs.size() == 0)
				System.out.println("There is no matched result!");

			Iterator<Entry<Integer, Integer>> iter = rs.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iter.next();
				int t_id = entry.getKey();
				String t_title = resourcesearcher.getTitlefromID(t_id);
				int frequency = entry.getValue();
				System.out.println("article:		" + t_title + "		frequency:		" + frequency);
			}
		}
		resourcesearcher.close();
		ac.close();
		scanner.close();
	}

}
