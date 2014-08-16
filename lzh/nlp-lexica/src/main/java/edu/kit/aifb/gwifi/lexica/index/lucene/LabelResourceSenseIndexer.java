package edu.kit.aifb.gwifi.lexica.index.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.kit.aifb.gwifi.model.Article;
import edu.kit.aifb.gwifi.model.Label;
import edu.kit.aifb.gwifi.model.Wikipedia;
import edu.kit.aifb.gwifi.util.LabelIterator;

public class LabelResourceSenseIndexer {

	public static String LABEL_FIELD = "label";
	public static String PAGE_ID_FIELD = "id";
	public static String SENSE_LINK_OCC_COUNT_FIELD = "senselinkOccCount";
	public static String SENSE_LINK_DOC_COUNT_FIELD = "senselinkDocCount";
	public static String SCORE_FIELD = "score";

	// "configs/wikipedia-template-en.xml" "/index/LabelResourceSense"
	public static void main(String[] args) throws Exception {
		File databaseDirectory = new File(args[0]);
		Wikipedia wikipedia = new Wikipedia(databaseDirectory, false);
		System.out.println("The Wikipedia environment has been initialized.");
		LabelIterator iter = wikipedia.getLabelIterator(null);

		File indexDir = new File(args[1]);
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
		IndexWriter indexWriter = new IndexWriter(index, iwc);

		int j = 0;
		while (iter.hasNext()) {

			if (++j % 1000 == 0)
				System.out.println(j + " labels have been processed!");

			Label label = iter.next();
			String labelText = label.getText();
			if(labelText == null || labelText.equals(""))
				continue;

			for (Label.Sense sense : label.getSenses()) {

				String title = sense.getTitle();

				if (title == null || title.equals(""))
					continue;

				Article article = wikipedia.getArticleByTitle(title);

				if (article == null) {
					System.out.println("Could not find exact match.");
					continue;
				} else {
					long linkOccCount = sense.getLinkOccCount();
					long linkDocCount = sense.getLinkDocCount();
					double score = sense.getPriorProbability();
					createDocuments(label, article, linkOccCount, linkDocCount, score, indexWriter);
				}
			}
		}
		iter.close();
		indexWriter.close();
	}

	public static void createDocuments(Label label, Article article, long linkOccCount, long linkDocCount,
			double score, IndexWriter indexWriter) throws CorruptIndexException, IOException {
		Document doc = new Document();

		Field label_field = new StringField(LABEL_FIELD, label.getText(), Field.Store.YES);
		Field articleId_field = new IntField(PAGE_ID_FIELD, article.getId(), Field.Store.YES);
		Field linkOccCount_field = new LongField(SENSE_LINK_OCC_COUNT_FIELD, linkOccCount, Field.Store.YES);
		Field linkDocCount_field = new LongField(SENSE_LINK_DOC_COUNT_FIELD, linkDocCount, Field.Store.YES);
		Field score_field = new DoubleField(SCORE_FIELD, score, Field.Store.YES);

		doc.add(label_field);
		doc.add(articleId_field);
		doc.add(linkOccCount_field);
		doc.add(linkDocCount_field);
		doc.add(score_field);

		indexWriter.addDocument(doc);
	}
}
