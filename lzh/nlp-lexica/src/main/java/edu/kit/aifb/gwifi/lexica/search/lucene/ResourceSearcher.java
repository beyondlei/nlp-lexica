package edu.kit.aifb.gwifi.lexica.search.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import edu.kit.aifb.gwifi.lexica.index.lucene.ResourceIndexer;

public class ResourceSearcher {

	private IndexReader reader;
	private IndexSearcher searcher;

	public ResourceSearcher(String path) throws Exception {
		reader = DirectoryReader.open(FSDirectory.open(new File(path)));
		searcher = new IndexSearcher(reader);
	}

	public int getIDfromTitle(String title) throws IOException {
		
		int articleID = -1;
		Term term = new Term(ResourceIndexer.PAGE_TITLE_FIELD, title);
		Query query = new TermQuery(term);

		TopDocs result = searcher.search(query, 1);
		if(result.scoreDocs.length < 1)
			return -1;
		
		Document firstHit = searcher.doc(result.scoreDocs[0].doc);
		articleID = Integer.parseInt(firstHit.get(ResourceIndexer.PAGE_ID_FIELD));
		
		return articleID;
	}

	public String getTitlefromID(int id) throws IOException {

		Query query = NumericRangeQuery.newIntRange(ResourceIndexer.PAGE_ID_FIELD, id, id, true, true);

		TopDocs result = searcher.search(query, 1);

		if(result.scoreDocs.length == 0)
			return "";
		Document firstHit = searcher.doc(result.scoreDocs[0].doc);
		String title = firstHit.get(ResourceIndexer.PAGE_TITLE_FIELD);

		return title;
	}

	public void close() throws IOException {
		reader.close();
	}

	// "/index/Resource"
	public static void main(String[] args) throws Exception {

		ResourceSearcher rs = new ResourceSearcher(args[0]);

//		Scanner scanner = new Scanner(System.in);
//		while (true) {
//			System.out.println("Please input the title as format \"title:xx\" or the article ID as format \"ID:xx\":");
//			String text = scanner.nextLine();
//			if (text.startsWith("exit"))
//				break;
//			String pre = text.split(":")[0];
//			if (pre.equals("title")) {
//				String title = text.split(":")[1];
//				int id = rs.getIDfromTitle(title);
//				
//				if (id == -1)
//					System.out.println("There is no matched result!");
//				else
//					System.out.println("title:	" + title + "	article ID:	" + id);
//			} else if (pre.equals("ID")) {
//				int id = Integer.parseInt(text.split(":")[1]);
//				String title = rs.getTitlefromID(id);
//				if (title == null)
//					continue;
//				if (title.equals(""))
//					System.out.println("There is no matched result!");
//				else
//					System.out.println("article ID:	" + id + "	title:	" + title);
//			}
//		}
//		rs.close();
//		scanner.close();
		
			
		String title = "法国";
		//int id = 1097979;
		int id = rs.getIDfromTitle(title);
		if (id == -1)
			System.out.println("There is no matched result!");
		else
			System.out.println("title:	" + title + "	article ID:	" + id);
		rs.close();
	}

}
