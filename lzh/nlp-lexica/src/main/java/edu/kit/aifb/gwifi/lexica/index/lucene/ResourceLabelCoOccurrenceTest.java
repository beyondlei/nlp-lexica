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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.index.CorruptIndexException;

import edu.kit.aifb.gwifi.lexica.Environment;
import edu.kit.aifb.gwifi.model.Article;
import edu.kit.aifb.gwifi.model.Wikipedia;

public class ResourceLabelCoOccurrenceTest {

	public static String PAGE_ID_FIELD = "id";
	public static String LABEL_FIELD = "label";
	public static String FREQUENCY_FIELD = "frequency";

	private Wikipedia wikipedia;
	private BufferedWriter output;

	// "configs/wikipedia-template-en.xml" "res/"
	public static void main(String[] args) throws Exception {
		ResourceLabelCoOccurrenceTest ap = new ResourceLabelCoOccurrenceTest(args[0], args[1]);
		ap.process();
	}

	public ResourceLabelCoOccurrenceTest(String configPath, String outputPath) throws Exception {
		File databaseDirectory = new File(configPath);
		wikipedia = new Wikipedia(databaseDirectory, false);
		System.out.println("The Wikipedia environment has been initialized.");

		output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath
				+ "/resourceLabelCoOccurrence.txt", true), "UTF-8"));
	}

	public void process() throws IOException {
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
				Map<String, Integer> labelMap = new HashMap<String, Integer>();

				for (Article a : s_article.getLinksIn()) {
					for (int i : a.getSentenceIndexesMentioning(s_article)) {
						int pre = i - Environment.NUM_SORROUNDING_SENTENCES < 0 ? 0 : i
								- Environment.NUM_SORROUNDING_SENTENCES;
						int sub = i + Environment.NUM_SORROUNDING_SENTENCES;
						for (; pre <= sub; pre++) {
							String sentence = a.getSentenceMarkup(pre);

							Matcher m = Pattern.compile("((?<=\\u005B\\u005B).*?(?=\\u005D\\u005D))").matcher(sentence);
							while (m.find()) {
								String label = m.group();

								if (label.contains("|"))
									label = label.substring(label.indexOf("|") + 1, label.length());
								if (label == null || label.equals(""))
									continue;

								if (labelMap.containsKey(label)) {
									int frequency = labelMap.get(label);
									frequency++;
									labelMap.put(label, frequency);
								} else
									labelMap.put(label, 1);
							}
						}

					}
				}
				print(s_title, labelMap);
			}
		}
		output.close();
	}

	public void print(String articleTitle, Map<String, Integer> labelMap) throws CorruptIndexException, IOException {
		output.write("============== Resource: " + articleTitle + " ==============");
		output.newLine();
		
		labelMap = sortByValues(labelMap);
		
		Iterator<Entry<String, Integer>> it = labelMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();
			String label = entry.getKey();
			int frequency = entry.getValue();
			output.write("Label: " + label + ",\t\t" + "Frequency: " + frequency);
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
