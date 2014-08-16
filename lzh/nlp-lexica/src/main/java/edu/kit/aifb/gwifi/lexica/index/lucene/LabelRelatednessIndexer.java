package edu.kit.aifb.gwifi.lexica.index.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.kit.aifb.gwifi.model.Article;
import edu.kit.aifb.gwifi.model.Label;
import edu.kit.aifb.gwifi.model.Wikipedia;
import edu.kit.aifb.gwifi.util.LabelIterator;

public class LabelRelatednessIndexer {

	public static String SOURCE_LABEL_FIELD = "s_label";
	public static String TARGET_LABEL_FIELD = "t_label";
	public static String SCORE_FIELD = "score";

	// "configs/wikipedia-template-en.xml" "/index/LabelResourceSense"
	// "/index/ResourceRelatedness" "/index/LabelRelatedness"
	public static void main(String[] args) throws Exception {
		File databaseDirectory = new File(args[0]);
		Wikipedia wikipedia = new Wikipedia(databaseDirectory, false);
		System.out.println("The Wikipedia environment has been initialized.");
		LabelIterator iter = wikipedia.getLabelIterator(null);

		File indexDir = new File(args[3]);
		Directory index = FSDirectory.open(indexDir);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		iwc.setRAMBufferSizeMB(256.0);
		IndexWriter indexWriter = new IndexWriter(index, iwc);

		IndexReader l2aReader = DirectoryReader.open(FSDirectory.open(new File(args[1])));
		IndexReader a2aReader = DirectoryReader.open(FSDirectory.open(new File(args[2])));
		IndexSearcher a2aSearcher = new IndexSearcher(a2aReader);
		IndexSearcher l2aSearcher = new IndexSearcher(l2aReader);

		int j = 0;
		while (iter.hasNext()) {

			if (++j % 1000 == 0)
				System.out.println(j + " labels have been processed!");

			Label label = iter.next();
			String s_labelText = label.getText();
			if(s_labelText == null || s_labelText.equals(""))
				continue;

			for (Label.Sense sense : label.getSenses()) {

				String title = sense.getTitle();

				if (title == null || title.equals(""))
					continue;

				Article article = wikipedia.getArticleByTitle(title);

				if (article == null) {
					continue;
				} else {
					double label2articleScore = sense.getPriorProbability();

					Term a2aTerm = new Term(ResourceRelatednessIndexer.SOURCE_PAGE_ID_FIELD, String.valueOf(article
							.getId()));
					Query a2aQuery = new TermQuery(a2aTerm);
					TopDocs a2aResult = a2aSearcher.search(a2aQuery, null, Integer.MAX_VALUE);
					// System.out.println(a2aResult.totalHits + "		" +
					// a2aResult.scoreDocs.length );

					for (int i = 0; i < a2aResult.scoreDocs.length; i++) {
						Document a2aFirstHit = a2aSearcher.doc(a2aResult.scoreDocs[i].doc);

						double article2articleScore = Double.parseDouble(a2aFirstHit.get("score"));

						Term l2aTerm = new Term(LabelResourceSenseIndexer.PAGE_ID_FIELD,
								a2aFirstHit.get(ResourceRelatednessIndexer.TARGET_PAGE_ID_FIELD));
						Query l2aQuery = new TermQuery(l2aTerm);
						TopDocs l2aResult = l2aSearcher.search(l2aQuery, null, Integer.MAX_VALUE);
						// System.out.println(l2aResult.totalHits + "          "
						// + l2aResult.scoreDocs.length );

						for (int m = 0; m < l2aResult.scoreDocs.length; m++) {
							Document l2aFirstHit = l2aSearcher.doc(l2aResult.scoreDocs[m].doc);

							double article2labelScore = Double.parseDouble(l2aFirstHit.get("score"));

							String t_labelText = l2aFirstHit.get("label");
							if(s_labelText == null || s_labelText.equals(""))
								continue;
							double score = label2articleScore * article2articleScore * article2labelScore;
							createDocument(s_labelText, t_labelText, score, indexWriter);
						}
					}
				}
			}
		}
		iter.close();
		a2aReader.close();
		l2aReader.close();
		indexWriter.close();
	}

	public static void createDocument(String sourceLabel, String targetLabel, double relatedness,
			IndexWriter indexWriter) throws CorruptIndexException, IOException {
		Document doc = new Document();

		Field s_l = new StringField(SOURCE_LABEL_FIELD, sourceLabel, Field.Store.YES);
		Field t_l = new StringField(TARGET_LABEL_FIELD, targetLabel, Field.Store.YES);
		Field score_field = new DoubleField(SCORE_FIELD, relatedness, Field.Store.YES);

		doc.add(s_l);
		doc.add(t_l);
		doc.add(score_field);

		indexWriter.addDocument(doc);
	}
}
