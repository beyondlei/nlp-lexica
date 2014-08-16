package edu.kit.aifb.gwifi.lexica.index.lucene;

import java.io.File;
import java.io.IOException;

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

import edu.kit.aifb.gwifi.model.Article;
import edu.kit.aifb.gwifi.model.Page;
import edu.kit.aifb.gwifi.model.Page.PageType;
import edu.kit.aifb.gwifi.model.Wikipedia;
import edu.kit.aifb.gwifi.util.PageIterator;

public class ResourceIndexer {

	public static String PAGE_ID_FIELD = "id";
	public static String PAGE_TITLE_FIELD = "title";
	public static String LINK_OCC_COUNT_FIELD = "linkOccCount";
	public static String LINK_DOC_COUNT_FIELD = "linkDocCount";

	// "configs/wikipedia-template-en.xml" "/index/Resource"
	public static void main(String[] args) throws Exception {
		File databaseDirectory = new File(args[0]);
		Wikipedia wikipedia = new Wikipedia(databaseDirectory, false);
		System.out.println("The Wikipedia environment has been initialized.");
		PageIterator iter = wikipedia.getPageIterator(PageType.article);

		File indexDir = new File(args[1]);
		if (!indexDir.exists()) {
			boolean created = indexDir.mkdirs();
			if(!created) {
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
				System.out.println(j + " resources have been processed!");
			
			Page page = iter.next();
			if (!page.getType().equals(PageType.article))
				continue;
			
			String title = page.getTitle();
			if (title == null || title.equals(""))
				continue;

			Article article = wikipedia.getArticleByTitle(title);
			
			if (article == null) {
				System.out.println("Could not find exact match.");
				continue;
			} 
			else {
				createDocument(article, indexWriter);
			}

		}
		iter.close();
		indexWriter.close();
	}

	public static void createDocument(Article article, IndexWriter indexWriter)
			throws CorruptIndexException, IOException {

		Document doc = new Document();

		Field id_field = new IntField(PAGE_ID_FIELD, article.getId(), Field.Store.YES);
		Field title_field = new StringField(PAGE_TITLE_FIELD, article.getTitle(), Field.Store.YES);
		Field linkOccCount_field = new IntField(LINK_OCC_COUNT_FIELD, article.getTotalLinksInCount(), Field.Store.YES);
		Field linkDocCount_field = new IntField(LINK_DOC_COUNT_FIELD, article.getDistinctLinksInCount(), Field.Store.YES);

		doc.add(id_field);
		doc.add(title_field);
		doc.add(linkOccCount_field);
		doc.add(linkDocCount_field);

		indexWriter.addDocument(doc);
	}

}
