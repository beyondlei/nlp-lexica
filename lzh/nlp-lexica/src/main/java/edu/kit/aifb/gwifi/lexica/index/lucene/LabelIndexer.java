package edu.kit.aifb.gwifi.lexica.index.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.kit.aifb.gwifi.model.Label;
import edu.kit.aifb.gwifi.model.Wikipedia;
import edu.kit.aifb.gwifi.util.LabelIterator;


public class LabelIndexer {

	public static String LABEL_FIELD = "label";
	public static String LINK_DOC_COUNT_FIELD = "link_doc_count";
	public static String TEXT_DOC_COUNT_FIELD = "text_doc_count";
	public static String LINK_OCC_COUNT_FIELD = "link_occ_count";
	public static String TEXT_OCC_COUNT_FIELD = "text_occ_count";

	// "configs/wikipedia-template-en.xml" "/index/Label"
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
			
			createDocuments(label, indexWriter);
		}
		iter.close();
		indexWriter.close();
	}

	public static void createDocuments(Label label, IndexWriter indexWriter) throws CorruptIndexException, IOException {
		Document doc = new Document();

		Field label_field = new StringField(LABEL_FIELD, label.getText(), Field.Store.YES);
		Field linkDocCount_field = new LongField(LINK_DOC_COUNT_FIELD, label.getLinkDocCount(), Field.Store.YES);
		Field textDocCount_field = new LongField(TEXT_DOC_COUNT_FIELD, label.getDocCount(), Field.Store.YES);
		Field linkOccCount_field = new LongField(LINK_OCC_COUNT_FIELD, label.getLinkOccCount(), Field.Store.YES);
		Field textOccCount_field = new LongField(TEXT_OCC_COUNT_FIELD, label.getOccCount(), Field.Store.YES);

		doc.add(label_field);
		doc.add(linkDocCount_field);
		doc.add(textDocCount_field);
		doc.add(linkOccCount_field);
		doc.add(textOccCount_field);

		indexWriter.addDocument(doc);
	}
}
