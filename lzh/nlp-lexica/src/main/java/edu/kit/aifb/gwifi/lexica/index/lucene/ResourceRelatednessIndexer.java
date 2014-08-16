package edu.kit.aifb.gwifi.lexica.index.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.kit.aifb.gwifi.comparison.ArticleComparer;
import edu.kit.aifb.gwifi.model.Article;
import edu.kit.aifb.gwifi.model.Page;
import edu.kit.aifb.gwifi.model.Page.PageType;
import edu.kit.aifb.gwifi.model.Wikipedia;
import edu.kit.aifb.gwifi.util.PageIterator;

public class ResourceRelatednessIndexer {

	public static String SOURCE_PAGE_ID_FIELD = "s_id";
	public static String TARGET_PAGE_ID_FIELD = "t_id";
	public static String SCORE_FIELD = "score";
	
	// "configs/wikipedia-template-en.xml" "/index/ResourceRelatedness"	
	public static void main(String[] args) throws Exception {
		File databaseDirectory = new File(args[0]);
		Wikipedia wikipedia = new Wikipedia(databaseDirectory, false);
		System.out.println("The Wikipedia environment has been initialized.");
		ArticleComparer comparer = new ArticleComparer(wikipedia);
		PageIterator iter = wikipedia.getPageIterator(PageType.article);
		
		File indexDir = new File(args[1]);
		Directory index = FSDirectory.open(indexDir);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		iwc.setRAMBufferSizeMB(256.0);
		IndexWriter indexWriter = new IndexWriter(index, iwc);
		
		int j = 0;
		while (iter.hasNext()) {
			
			if(++j % 1000 == 0)
				System.out.println(j + " articles have been processed!"); 
			Page page = iter.next();
			
			if (!page.getType().equals(PageType.article))
				continue;

			String title = page.getTitle();

			if (title == null || title.equals(""))
				continue;

			Article article = wikipedia.getArticleByTitle(title);
			
			if (article == null) {
				System.out.println("Could not find exact match. Searching through anchors instead");
				continue;
			} 
			else {
				int idS = article.getId();
				
				for (Article tOUT : article.getLinksOut()) {
					int idT = tOUT.getId();
					if(idS >= idT)
						continue;
					double relatedness = comparer.getRelatedness(article, tOUT);
					createDocument(article, tOUT, relatedness, indexWriter);
				}
				
				for (Article tIN : article.getLinksIn()) {
					int idT = tIN.getId();
					if(idS >= idT)
						continue;
					double relatedness = comparer.getRelatedness(article, tIN);
					createDocument(article, tIN, relatedness, indexWriter);
				}
				
			}
		}
		iter.close();
		indexWriter.close();
	}
	
	public static void createDocument(Article sourceArticle, Article targetArticle, double relatedness, IndexWriter indexWriter) throws CorruptIndexException, IOException {
		Document doc = new Document();

		Field s_id = new IntField(SOURCE_PAGE_ID_FIELD, sourceArticle.getId(),Field.Store.YES);
		Field t_id = new IntField(TARGET_PAGE_ID_FIELD, targetArticle.getId(), Field.Store.YES);
		Field score_field = new DoubleField(SCORE_FIELD, relatedness, Field.Store.YES);
		
		doc.add(s_id);
		doc.add(t_id);
		doc.add(score_field);

		indexWriter.addDocument(doc);
	}
}
