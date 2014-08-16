package edu.kit.aifb.gwifi.lexica.index.lucene;

import java.io.File;

import edu.kit.aifb.gwifi.model.Label;
import edu.kit.aifb.gwifi.model.Wikipedia;
import edu.kit.aifb.gwifi.util.LabelIterator;

public class LabelStatistics {

	public static void main(String[] args) throws Exception {
		File databaseDirectory = new File("configs/wikipedia-template-en.xml");
		Wikipedia wikipedia = new Wikipedia(databaseDirectory, false);
		System.out.println("The Wikipedia environment has been initialized.");
		LabelIterator iter = wikipedia.getLabelIterator(null);

		int i = 0;
		while (iter.hasNext()) {
			Label label = iter.next();
			i++;
//			System.out.println(i + ": " + label.getText());
		}
		System.out.println("There are " + i + " labels.");
		iter.close();
	}

}


// en
// There are 13270871 labels.

