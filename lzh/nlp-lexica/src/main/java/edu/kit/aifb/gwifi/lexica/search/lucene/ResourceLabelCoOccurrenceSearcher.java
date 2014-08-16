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
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import edu.kit.aifb.gwifi.lexica.index.lucene.ResourceLabelCoOccurrenceIndexer;

public class ResourceLabelCoOccurrenceSearcher {

	private IndexReader reader;
	private IndexSearcher searcher;

	public ResourceLabelCoOccurrenceSearcher(String path) throws Exception {
		reader = DirectoryReader.open(FSDirectory.open(new File(path)));
		searcher = new IndexSearcher(reader);
	}
	
	public ResourceLabelCoOccurrenceSearcher(IndexReader reader) throws Exception {
		this.reader = reader;
		searcher = new IndexSearcher(reader);
	}

	public LinkedHashMap<String, Integer> getCoOccurrences(int id, int number, boolean sorted) throws IOException {
		LinkedHashMap<String, Integer> resultMap = new LinkedHashMap<String, Integer>();

		Query query = NumericRangeQuery.newIntRange(ResourceLabelCoOccurrenceIndexer.PAGE_ID_FIELD, id, id, true, true);

		TopDocs result;
		if(sorted) {
			Sort sort = new Sort(new SortField(ResourceLabelCoOccurrenceIndexer.FREQUENCY_FIELD, Type.INT, true));
			result = searcher.search(query, number, sort);
		} else {
			result = searcher.search(query, number);
		}

		for (int i = 0; i < result.scoreDocs.length; i++) {
			Document firstHit = searcher.doc(result.scoreDocs[i].doc);

			String str = firstHit.get(ResourceLabelCoOccurrenceIndexer.FREQUENCY_FIELD);
			int frequency = Integer.parseInt(firstHit.get(ResourceLabelCoOccurrenceIndexer.FREQUENCY_FIELD));
			String label = firstHit.get(ResourceLabelCoOccurrenceIndexer.LABEL_FIELD);
			resultMap.put(label, frequency);
		}
		return resultMap;
	}

	public LinkedHashMap<Integer, Integer> getCoOccurrences(String label, int number, boolean sorted) throws IOException {
		LinkedHashMap<Integer, Integer> resultMap = new LinkedHashMap<Integer, Integer>();

		Term term = new Term(ResourceLabelCoOccurrenceIndexer.LABEL_FIELD, label);
		Query query = new TermQuery(term);

		TopDocs result;
		if (sorted) {
			Sort sort = new Sort(new SortField(ResourceLabelCoOccurrenceIndexer.FREQUENCY_FIELD, Type.INT, true));
			result = searcher.search(query, number, sort);
		} else {
			result = searcher.search(query, number);
		}
		
		for (int i = 0; i < result.scoreDocs.length; i++) {
			Document firstHit = searcher.doc(result.scoreDocs[i].doc);

			int frequency = Integer.parseInt(firstHit.get(ResourceLabelCoOccurrenceIndexer.FREQUENCY_FIELD));
			int id = Integer.parseInt(firstHit.get(ResourceLabelCoOccurrenceIndexer.PAGE_ID_FIELD));
			resultMap.put(id, frequency);
		}
		return resultMap;
	}
	
	public int getCoOccurrence(int s_id, String label) throws IOException {

		Query s_tq = NumericRangeQuery.newIntRange(ResourceLabelCoOccurrenceIndexer.PAGE_ID_FIELD, s_id, s_id, true, true);
		
		Term t_term = new Term(ResourceLabelCoOccurrenceIndexer.LABEL_FIELD, label);
		Query t_tq = new TermQuery(t_term);
		BooleanQuery query = new BooleanQuery();
		query.add(s_tq, Occur.MUST);
		query.add(t_tq, Occur.MUST);

		TopDocs result = searcher.search(query, 1);

		Document firstHit = searcher.doc(result.scoreDocs[0].doc);
		int frequency = Integer.parseInt(firstHit.get(ResourceLabelCoOccurrenceIndexer.FREQUENCY_FIELD));

		return frequency;
	}

	public void close() throws IOException {
		reader.close();
	}

	// "/index/Resource" "/index/ResourceLabelCoOccurrence"
	public static void main(String[] args) throws Exception {

		ResourceSearcher resourcesearcher = new ResourceSearcher(args[0]);
		ResourceLabelCoOccurrenceSearcher ac = new ResourceLabelCoOccurrenceSearcher(args[1]);

		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("Please input the title:");
			String title = scanner.nextLine();
			if (title.startsWith("exit"))
				break;

			int id = resourcesearcher.getIDfromTitle(title);

			LinkedHashMap<String, Integer> rs = ac.getCoOccurrences(id, 1000, true);
			if (rs == null)
				continue;
			if (rs.size() == 0)
				System.out.println("There is no matched result!");

			Iterator<Entry<String, Integer>> iter = rs.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
				String label = entry.getKey();
				int frequency = entry.getValue();
				System.out.println("label:		" + label + "		frequency:		" + frequency);
			}
		}
		resourcesearcher.close();
		ac.close();
		scanner.close();
	}

}
