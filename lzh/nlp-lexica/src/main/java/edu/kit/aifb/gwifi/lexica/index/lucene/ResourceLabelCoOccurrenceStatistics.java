package edu.kit.aifb.gwifi.lexica.index.lucene;

import java.io.File;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class ResourceLabelCoOccurrenceStatistics {

	// "configs/wikipedia-template-en.xml" 
	public static void main(String[] args) throws Exception {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(args[0])));
		
		int totalCoOccurrenceFreq = 0;
		for (int i = 0; i < reader.maxDoc(); i++) {
			Document doc = reader.document(i);
			if (doc != null) {
				int freq = Integer.parseInt(doc.get(ResourceLabelCoOccurrenceIndexer.FREQUENCY_FIELD));
				totalCoOccurrenceFreq += freq;
			}
		}
		
		System.out.println("The co-occurrence frequency is " + totalCoOccurrenceFreq + ".");
		reader.close();
	}
	
}
