package edu.kit.aifb.gwifi.lexica.index.mongodb;

import java.io.File;

import edu.kit.aifb.gwifi.model.Article;
import edu.kit.aifb.gwifi.model.Page;
import edu.kit.aifb.gwifi.model.Page.PageType;
import edu.kit.aifb.gwifi.model.Wikipedia;
import edu.kit.aifb.gwifi.util.PageIterator;

public class LabelResourceSenseMongoDBPreparer {

	// "configs/wikipedia-template-en.xml" 
	public static void main(String[] args) throws Exception {
		File databaseDirectory = new File("configs/wikipedia-template-sl.xml");
		Wikipedia wikipedia = new Wikipedia(databaseDirectory, false);
		System.out.println("The Wikipedia environment has been initialized.");
		PageIterator iter = wikipedia.getPageIterator(PageType.article);

		int totalArticleCount = 0;
		long totalLinksInCount = 0;
		long totalLinksOutCount = 0;
		while (iter.hasNext()) {
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
			} else {
				totalArticleCount++;
				totalLinksInCount += article.getTotalLinksInCount();
				totalLinksOutCount += article.getTotalLinksOutCount();
			}
			
			if(totalArticleCount % 100 == 0) {
				System.out.println(totalArticleCount + " articles have been processed.");
			}
			
		}
		
		System.out.println("There are " + totalArticleCount + " articles.");
		System.out.println("There are " + totalLinksInCount + " LinksIn.");
		System.out.println("There are " + totalLinksOutCount + " LinksOut.");
		iter.close();
	}
	
	// en:
	// There are 4172240 articles.
	// There are 92159538 LinksIn.
	// There are 91669453 LinksOut.
	
	// de:
	// There are 1395359 articles.
	// There are 36042422 LinksIn.
	// There are 35599005 LinksOut.
	
	// es:
	// There are 991653 articles.
	// There are 22551993 LinksIn.
	// There are 22559027 LinksOut.

	// zh:
	// There are 697699 articles.
	// There are 10299531 LinksIn.
	// There are 10308835 LinksOut.
	
	// ca:
	// There are 401066 articles.
	// There are 7086936 LinksIn.
	// There are 7062487 LinksOut.
	
	// sl:
	// There are 133905 articles.
	// There are 2165944 LinksIn.
	// There are 2141514 LinksOut
	
}
