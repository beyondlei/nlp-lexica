package edu.kit.aifb.gwifi.lexica.index.lucene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.index.CorruptIndexException;

import edu.kit.aifb.gwifi.lexica.Environment;
import edu.kit.aifb.gwifi.model.Article;
import edu.kit.aifb.gwifi.model.Wikipedia;
import edu.kit.aifb.gwifi.util.nlp.ITokenStream;
import edu.kit.aifb.gwifi.util.nlp.Language;
import edu.kit.aifb.gwifi.util.nlp.MultiLingualAnalyzer;
import edu.kit.aifb.gwifi.util.nlp.TextDocument;

public class ResourceWordCoOccurrenceTest {

	public static String PAGE_ID_FIELD = "id";
	public static String WORD_FIELD = "word";
	public static String FREQUENCY_FIELD = "frequency";

	private BufferedWriter output;
	private Wikipedia wikipedia;
	private MultiLingualAnalyzer multilingualAnalyzer;
	private Language language;
	
	private ChineseSegmenter ChineseSegmenter;

	// "configs/wikipedia-template-en.xml" "res/"
	// "en" "stopwords/en-stopwords.txt"
	public static void main(String[] args) throws Exception {
		ResourceWordCoOccurrenceTest aw = new ResourceWordCoOccurrenceTest(args[0], args[1], args[2], args[3]);
		aw.process();
	}

	public ResourceWordCoOccurrenceTest(String configPath, String outputPath, String lang, String stopwords)
			throws Exception {
		File databaseDirectory = new File(configPath);
		wikipedia = new Wikipedia(databaseDirectory, false);
		System.out.println("The Wikipedia environment has been initialized.");

		output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath
				+ "/resourceWordCoOccurrence.txt", true), "UTF-8"));

		language = Language.getLanguage(lang);
		multilingualAnalyzer = new MultiLingualAnalyzer();
		multilingualAnalyzer.setStopwordFile(language.toString() + ":" + stopwords);
		multilingualAnalyzer.setStemming(false);
		
		if(language.equals(Language.ZH))
			ChineseSegmenter = new ChineseSegmenter("configs/NLPConfig.properties");
	}

	public void process() throws CorruptIndexException, IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			System.out.println("\nEnter article title (or enter to quit): ");
			String s_title = in.readLine();

			if (s_title == null || s_title.equals("") || s_title.equals("exit"))
				break;

			Article s_article = wikipedia.getArticleByTitle(s_title);

			if (s_article == null) {
				System.out.println("Could not find exact match.");
				continue;
			} else {
				Map<String, Integer> wordsMap = new HashMap<String, Integer>();

				for (Article a : s_article.getLinksIn()) {
					for (int i : a.getSentenceIndexesMentioning(s_article)) {
						int pre = i - Environment.NUM_SORROUNDING_SENTENCES < 0 ? 0 : i - Environment.NUM_SORROUNDING_SENTENCES;
						int sub = i + Environment.NUM_SORROUNDING_SENTENCES;
						for (; pre <= sub; pre++) {
							String sentence = a.getSentenceMarkup(i);
							if(language.equals(Language.ZH))
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
				print(s_title, wordsMap);
			}
		}
		output.close();
	}

	public void print(String articleTitle, Map<String, Integer> wordMap) throws CorruptIndexException, IOException {
		output.write("============== Resource: " + articleTitle + " ==============");
		output.newLine();
		
		wordMap = sortByValues(wordMap);
		
		Iterator<Entry<String, Integer>> it = wordMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();
			String word = entry.getKey();
			int frequency = entry.getValue();
			output.write("Word: " + word + ",\t\t" + "Frequency: " + frequency);
			output.newLine();
		}
		output.newLine();
		output.flush();
	}

	public static <K extends Comparable, V extends Comparable> Map<K, V> sortByValues(Map<K, V> map) {
		List<Map.Entry<K, V>> entries = new LinkedList<Map.Entry<K, V>>(map.entrySet());

		Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {

			@Override
			public int compare(Entry<K, V> o1, Entry<K, V> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		Map<K, V> sortedMap = new LinkedHashMap<K, V>();

		for (Map.Entry<K, V> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

}
