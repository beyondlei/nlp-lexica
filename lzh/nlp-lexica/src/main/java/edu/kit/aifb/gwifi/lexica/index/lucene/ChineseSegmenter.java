package edu.kit.aifb.gwifi.lexica.index.lucene;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import edu.kit.aifb.gwifi.util.Position;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

public class ChineseSegmenter {

	private CRFClassifier<CoreLabel> segmenter;

	public ChineseSegmenter(String configFile) {
		Properties properties = new Properties();
		try {
			FileInputStream inputFile = new FileInputStream(configFile);
			properties.load(inputFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Properties props = new Properties();
		props.setProperty("sighanCorporaDict", properties.getProperty("sighanCorporaDict"));
		props.setProperty("serDictionary", properties.getProperty("chineseDictionary"));
		props.setProperty("inputEncoding", "UTF-8");
		props.setProperty("sighanPostProcessing", "true");
		segmenter = new CRFClassifier<CoreLabel>(props);
		segmenter.loadClassifierNoExceptions(properties.getProperty("segmenterChineseClassifier"), props);
	}

	public String segmentation(String input) {
		return segmenter.classifyToString(input);
	}

	public List<Position> segmentationPosition(String input) {
		List<Position> results = new ArrayList<Position>();
		List<String> segments = segmenter.segmentString(input);

		int currentIndex = 0;
		for (String segment : segments) {
			int start = input.indexOf(segment, currentIndex);
			int end = start + segment.length();
			Position position = new Position(start, end);
			results.add(position);
			currentIndex = end;
		}

		return results;
	}

	public static void main(String args[]) {

		ChineseSegmenter pre = new ChineseSegmenter("configs/NLPConfig.properties");
		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.println("Please input the source text:");
			String source = scanner.nextLine();

			source = "中新网新加坡6月1日电 (记者 夏宇华)中国人民解放军副总参谋长王冠中中将1日上午在新加坡香格里拉对话会上围绕“大国在保持亚洲地区和平中的作用”主题作大会发言。他在发言过程中脱离讲稿，对日本首相安倍晋三和美国国防部长哈格尔本次会议期间对中国进行的攻击作出反击，称上述二人的讲话是对中国的一种挑衅。";

			if (source.startsWith("exit")) {
				break;
			}

			source = pre.segmentation(source);
			System.out.println("Segmentation: ");
			System.out.println(pre.segmentation(source));
			for (Position pos : pre.segmentationPosition(source)) {
				System.out.println(source.substring(pos.getStart(), pos.getEnd()) + " : " + pos);
			}
		}
	}

}
