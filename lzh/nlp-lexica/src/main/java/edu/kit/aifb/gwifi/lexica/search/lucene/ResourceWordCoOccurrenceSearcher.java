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

import edu.kit.aifb.gwifi.lexica.index.lucene.ResourceWordCoOccurrenceIndexer;
import edu.kit.aifb.gwifi.util.nlp.ITokenStream;
import edu.kit.aifb.gwifi.util.nlp.Language;
import edu.kit.aifb.gwifi.util.nlp.MultiLingualAnalyzer;
import edu.kit.aifb.gwifi.util.nlp.TextDocument;

public class ResourceWordCoOccurrenceSearcher {

	private IndexReader reader;
	private IndexSearcher searcher;
	private MultiLingualAnalyzer multilingualAnalyzer;

	public ResourceWordCoOccurrenceSearcher(String path, int resultNum, String lang, String stopwords) throws Exception {
		reader = DirectoryReader.open(FSDirectory.open(new File(path)));
		searcher = new IndexSearcher(reader);
			
		multilingualAnalyzer = new MultiLingualAnalyzer();
		multilingualAnalyzer.setStopwordFile(lang + ":" + stopwords);
	}

	public LinkedHashMap<String, Integer> getWords(int id, int number, boolean sorted) throws IOException {
		LinkedHashMap<String, Integer> resultMap = new LinkedHashMap<String, Integer>();

		Query query = NumericRangeQuery.newIntRange(ResourceWordCoOccurrenceIndexer.PAGE_ID_FIELD, id, id, true, true);

		TopDocs result;
		if (sorted) {
			Sort sort = new Sort(new SortField(ResourceWordCoOccurrenceIndexer.FREQUENCY_FIELD, Type.INT, true));
			result = searcher.search(query, number, sort);
		} else {
			result = searcher.search(query, number);
		}
		
		for (int i = 0; i < result.scoreDocs.length; i++) {
			Document firstHit = searcher.doc(result.scoreDocs[i].doc);

			int frequency = Integer.parseInt(firstHit.get(ResourceWordCoOccurrenceIndexer.FREQUENCY_FIELD));
			String word = firstHit.get(ResourceWordCoOccurrenceIndexer.WORD_FIELD);
			resultMap.put(word, frequency);
		}
		return resultMap;
	}

	public LinkedHashMap<Integer, Integer> getArticles(String word, int number, boolean sorted) throws IOException {
		LinkedHashMap<Integer, Integer> resultMap = new LinkedHashMap<Integer, Integer>();

		if (word == null) {
			System.out.println("Could not find exact match. Please try another title!");
			return null;
		} else {
			TextDocument doc = new TextDocument("word");
			doc.setText("en", Language.EN, word);

			ITokenStream ts = multilingualAnalyzer.getAnalyzedTokenStream(doc.getTokens());
			if (ts.next()) {
				word = ts.getToken();
			} else {
				word = "";
			}

			Term term = new Term(ResourceWordCoOccurrenceIndexer.WORD_FIELD, word);
			Query query = new TermQuery(term);

			TopDocs result;
			if (sorted) {
				Sort sort = new Sort(new SortField(ResourceWordCoOccurrenceIndexer.FREQUENCY_FIELD, Type.INT, true));
				result = searcher.search(query, number, sort);
			} else {
				result = searcher.search(query, number);
			}
			
			for (int i = 0; i < result.scoreDocs.length; i++) {
				Document firstHit = searcher.doc(result.scoreDocs[i].doc);

				int frequency = Integer.parseInt(firstHit.get(ResourceWordCoOccurrenceIndexer.FREQUENCY_FIELD));
				int articleID = Integer.parseInt(firstHit.get(ResourceWordCoOccurrenceIndexer.PAGE_ID_FIELD));
				resultMap.put(articleID, frequency);
			}
		}
		return resultMap;
	}

	public void close() throws IOException {
		reader.close();
	}

	// "/index/Resource" "/index/ResourceWordCoOccurrence" "en" "res/stopwords/en-stopwords.txt"
	public static void main(String[] args) throws Exception {
		
		ResourceSearcher resourcesearcher = new ResourceSearcher(args[0]);
		ResourceWordCoOccurrenceSearcher aw = new ResourceWordCoOccurrenceSearcher(args[0], Integer.parseInt(args[1]), args[2], args[3]);

		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("Please input the title as format \"title:xx\" or the word as format \"word:xx\":");
			String text = scanner.nextLine();
			if (text.startsWith("exit"))
				break;
			String pre = text.split(":")[0];
			if (pre.equals("title")) {
				String title = text.split(":")[1];
				int id = resourcesearcher.getIDfromTitle(title);

				LinkedHashMap<String, Integer> rs = aw.getWords(id, 1000, true);
				if (rs == null)
					continue;
				if (rs.size() == 0)
					System.out.println("There is no matched result!");

				Iterator<Entry<String, Integer>> iter = rs.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
					String word = entry.getKey();
					int frequency = entry.getValue();
					System.out.println("word:	" + word + "	frequency:	" + frequency);
				}
			} else if (pre.equals("word")) {
				String word = text.split(":")[1];
				LinkedHashMap<Integer, Integer> rs = aw.getArticles(word, 1000, true);
				if (rs == null)
					continue;
				if (rs.size() == 0)
					System.out.println("There is no matched result!");

				Iterator<Entry<Integer, Integer>> iter = rs.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iter.next();
					int articleID = entry.getKey();
					String t_title = resourcesearcher.getTitlefromID(articleID);
					int frequency = entry.getValue();
					System.out.println("article:	" + t_title + "	frequency:	" + frequency);
				}
			}
		}
		resourcesearcher.close();
		aw.close();
		scanner.close();
	}

}
