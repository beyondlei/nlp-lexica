package edu.kit.aifb.gwifi.lexica.index.lucene;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.kit.aifb.gwifi.lexica.Environment;
import edu.kit.aifb.gwifi.model.Article;
import edu.kit.aifb.gwifi.model.Page;
import edu.kit.aifb.gwifi.model.Page.PageType;
import edu.kit.aifb.gwifi.model.Wikipedia;
import edu.kit.aifb.gwifi.util.PageIterator;

public class ResourceCoOccurrenceIndexer {

	public static String SOURCE_PAGE_ID_FIELD = "s_id";
	public static String TARGET_PAGE_ID_FIELD = "t_id";
	public static String FREQUENCY_FIELD = "frequency";

	private PageIterator iter;
	private IndexWriter indexWriter;
	private Wikipedia wikipedia;

	// "configs/wikipedia-template-en.xml" "/index/ResourceCoOccurrence"
	public static void main(String[] args) throws Exception {
		ResourceCoOccurrenceIndexer ap = new ResourceCoOccurrenceIndexer(args[0], args[1]);
		ap.process();
	}

	public ResourceCoOccurrenceIndexer(String configPath, String indexPath) throws Exception {
		File databaseDirectory = new File(configPath);
		wikipedia = new Wikipedia(databaseDirectory, false);
		System.out.println("The Wikipedia environment has been initialized.");
		iter = wikipedia.getPageIterator(PageType.article);

		File indexDir = new File(indexPath);
		if (!indexDir.exists()) {
			boolean created = indexDir.mkdirs();
			if (!created) {
				System.out.print("Cannot create the index directory");
			}
		}
		Directory index = FSDirectory.open(indexDir);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		iwc.setRAMBufferSizeMB(256.0);
		indexWriter = new IndexWriter(index, iwc);
	}

	public void process() throws IOException {
		int j = 0;
		while (iter.hasNext()) {

			if (++j % 1000 == 0)
				System.out.println(j + " articles have been processed!");

			Page page = iter.next();

			if (!page.getType().equals(PageType.article))
				continue;

			String s_title = page.getTitle();

			if (s_title == null || s_title.equals(""))
				continue;

			Article s_article = wikipedia.getArticleByTitle(s_title);

			if (s_article == null) {
				System.out.println("Could not find exact match.");
				continue;
			} else {
				Map<Integer, Integer> articleMap = new HashMap<Integer, Integer>();
				int s_id = s_article.getId();

				for (Article a : s_article.getLinksIn()) {
					for (int i : a.getSentenceIndexesMentioning(s_article)) {
						int pre = i - Environment.NUM_SORROUNDING_SENTENCES < 0 ? 0 : i
								- Environment.NUM_SORROUNDING_SENTENCES;
						int sub = i + Environment.NUM_SORROUNDING_SENTENCES;
						for (; pre <= sub; pre++) {
							String sentence = a.getSentenceMarkup(i);

							Matcher m = Pattern.compile("((?<=\\u005B\\u005B).*?(?=\\u005D\\u005D))").matcher(sentence);
							while (m.find()) {
								String t_title = m.group();

								if (t_title.contains("|"))
									t_title = t_title.substring(0, t_title.indexOf("|"));

								Article t_article = wikipedia.getArticleByTitle(t_title);

								if (t_article == null || t_title.equals(""))
									continue;

								int t_id = t_article.getId();
								// if(s_id >= t_id)
								// continue;

								if (articleMap.containsKey(t_id)) {
									int frequency = articleMap.get(t_id);
									frequency++;
									articleMap.put(t_id, frequency);
								} else
									articleMap.put(t_id, 1);
							}
						}
					}
				}
				createDocument(s_id, articleMap);
			}
		}
		iter.close();
		indexWriter.close();
	}

	public void createDocument(int s_articleID, Map<Integer, Integer> articleMap) throws CorruptIndexException,
			IOException {

		Iterator<Entry<Integer, Integer>> it = articleMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) it.next();
			int t_articleId = entry.getKey();
			int frequency = entry.getValue();

			Document doc = new Document();

			Field sid_field = new IntField(SOURCE_PAGE_ID_FIELD, s_articleID, Field.Store.YES);
			Field tid_field = new IntField(TARGET_PAGE_ID_FIELD, t_articleId, Field.Store.YES);
			Field freq_field = new IntField(FREQUENCY_FIELD, frequency, Field.Store.YES);

			doc.add(sid_field);
			doc.add(tid_field);
			doc.add(freq_field);

			indexWriter.addDocument(doc);
		}
	}

}
