package edu.kit.aifb.gwifi.lexica.index.lucene;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
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
import edu.kit.aifb.gwifi.util.nlp.MultiLingualAnalyzer;
import edu.kit.aifb.gwifi.util.nlp.ITokenStream;
import edu.kit.aifb.gwifi.util.nlp.Language;
import edu.kit.aifb.gwifi.util.nlp.TextDocument;

public class ResourceWordCoOccurrenceIndexer {

	public static String PAGE_ID_FIELD = "id";
	public static String WORD_FIELD = "word";
	public static String FREQUENCY_FIELD = "frequency";

	private PageIterator iter;
	private IndexWriter indexWriter;
	private Wikipedia wikipedia;
	private MultiLingualAnalyzer multilingualAnalyzer;
	private Language language;

	private ChineseSegmenter ChineseSegmenter;

	// "configs/wikipedia-template-en.xml" "/index/ResourceWordCoOccurrence"
	// "en" "res/stopwords/en-stopwords.txt"
	
	// "configs/wikipedia-template-zh.xml" "/index/ResourceWordCoOccurrence"
	// "zh" "res/stopwords/zh-stopwords.txt"  "configs/NLPConfig.properties"
	public static void main(String[] args) throws Exception {
		ResourceWordCoOccurrenceIndexer aw = new ResourceWordCoOccurrenceIndexer(args[0], args[1], args[2], args[3]);
		aw.process();
	}

	public ResourceWordCoOccurrenceIndexer(String configPath, String indexPath, String lang, String stopwords,
			String nlpConfigPath) throws Exception {
		this(configPath, indexPath, lang, stopwords);
		if (language.equals(Language.ZH))
			ChineseSegmenter = new ChineseSegmenter(nlpConfigPath);
	}

	public ResourceWordCoOccurrenceIndexer(String configPath, String indexPath, String lang, String stopwords)
			throws Exception {
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

		language = Language.getLanguage(lang);
		multilingualAnalyzer = new MultiLingualAnalyzer();
		multilingualAnalyzer.setStopwordFile(language.toString() + ":" + stopwords);
		multilingualAnalyzer.setStemming(false);
	}

	public void process() throws CorruptIndexException, IOException {
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
				Map<String, Integer> wordsMap = new HashMap<String, Integer>();
				int s_id = s_article.getId();

				for (Article a : s_article.getLinksIn()) {
					for (int i : a.getSentenceIndexesMentioning(s_article)) {
						int pre = i - Environment.NUM_SORROUNDING_SENTENCES < 0 ? 0 : i
								- Environment.NUM_SORROUNDING_SENTENCES;
						int sub = i + Environment.NUM_SORROUNDING_SENTENCES;
						for (; pre <= sub; pre++) {
							String sentence = a.getSentenceMarkup(i);
							if (language.equals(Language.ZH))
								sentence = ChineseSegmenter.segmentation(sentence);

							TextDocument doc = new TextDocument("context");
							doc.setText(language.toString(), language, sentence);

							ITokenStream ts = multilingualAnalyzer.getAnalyzedTokenStream(doc.getTokens());
							while (ts.next()) {
								String word = ts.getToken();
								if (word == null || word.equals(""))
									continue;
								if (wordsMap.containsKey(word)) {
									int frequency = wordsMap.get(word);
									frequency++;
									wordsMap.put(word, frequency);
								} else
									wordsMap.put(word, 1);
							}

							/*
							 * Matcher m = Pattern.compile(
							 * "((?<=\\u005B\\u005B).*?(?=\\u005D\\u005D))"
							 * ).matcher(s_sentence); while(m.find()){ String
							 * title = m.group();
							 * 
							 * if(title.contains("|")) title =
							 * title.substring(0, title.indexOf("|"));
							 * 
							 * Article article =
							 * wikipedia.getArticleByTitle(title);
							 * 
							 * if (article == null) continue; if(article.getId()
							 * == s_id){ String sentence =
							 * s_sentence.replace("|", " "); String sentence_af
							 * =
							 * sentence.substring(0,m.start()).trim().replaceAll
							 * (
							 * "[\\\"'\\[\\]\\-()<>{}=/|!,.:;*^_~`+#$@&?\\\\]",
							 * "").trim() + " " +
							 * sentence.substring(m.end(),sentence
							 * .length()).trim().replaceAll
							 * ("[\\\"'\\[\\]\\-()<>{}=/|!,.:;*^_~`+#$@&?\\\\]",
							 * "").trim(); String[] words =
							 * sentence_af.split("[\\p{Space}]+");
							 * 
							 * break; } }
							 */
						}
					}
				}
				createDocument(s_id, wordsMap);
			}
		}
		iter.close();
		indexWriter.close();
	}

	public void createDocument(int s_id, Map<String, Integer> articleMap) throws CorruptIndexException, IOException {

		Iterator<Entry<String, Integer>> it = articleMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();
			String word = entry.getKey();
			int frequency = entry.getValue();

			Document doc = new Document();

			Field id_field = new IntField(PAGE_ID_FIELD, s_id, Field.Store.YES);
			Field word_field = new StringField(WORD_FIELD, word, Field.Store.YES);
			Field frequency_field = new IntField(FREQUENCY_FIELD, frequency, Field.Store.YES);

			doc.add(id_field);
			doc.add(word_field);
			doc.add(frequency_field);

			indexWriter.addDocument(doc);
		}
	}

}
