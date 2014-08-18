package edu.kit.aifb.ui.util;

public class MongoDBInfo {

	private static String dbpeidaURL = "dbpedia.org/resource/";
	
	private static String host = "mongodb://aifb-ls3-remus.aifb.kit.edu:19000";
	private static String db = "lexica";
	
	private static String LabelResourceSenseColl = "LabelResourceSense_new";
	private static String ResourceLabelCoOccurrenceColl = "ResourceLabelCoOccurrence";
	private static String ResourceWordCoOccurrenceColl = "ResourceWordCoOccurrence";
	private static String InterlingualResourceColl = "InterlingualResource_all";
	private static String ResourceLabelCompareColl = "ResourceLabelCompare";
	private static String ResourceWordCompareColl = "ResourceWordCompare";
	
	
	public static String getDbpeidaURL() {
		return dbpeidaURL;
	}
	public static void setDbpeidaURL(String dbpeidaURL) {
		MongoDBInfo.dbpeidaURL = dbpeidaURL;
	}
	public static String getHost() {
		return host;
	}
	public static void setHost(String host) {
		MongoDBInfo.host = host;
	}
	public static String getDb() {
		return db;
	}
	public static void setDb(String db) {
		MongoDBInfo.db = db;
	}
	public static String getLabelResourceSenseColl() {
		return LabelResourceSenseColl;
	}
	public static void setLabelResourceSenseColl(String labelResourceSenseColl) {
		LabelResourceSenseColl = labelResourceSenseColl;
	}
	public static String getResourceLabelCoOccurrenceColl() {
		return ResourceLabelCoOccurrenceColl;
	}
	public static void setResourceLabelCoOccurrenceColl(
			String resourceLabelCoOccurrenceColl) {
		ResourceLabelCoOccurrenceColl = resourceLabelCoOccurrenceColl;
	}
	public static String getResourceWordCoOccurrenceColl() {
		return ResourceWordCoOccurrenceColl;
	}
	public static void setResourceWordCoOccurrenceColl(
			String resourceWordCoOccurrenceColl) {
		ResourceWordCoOccurrenceColl = resourceWordCoOccurrenceColl;
	}
	public static String getInterlingualResourceColl() {
		return InterlingualResourceColl;
	}
	public static void setInterlingualResourceColl(String interlingualResourceColl) {
		InterlingualResourceColl = interlingualResourceColl;
	}
	public static String getResourceLabelCompareColl() {
		return ResourceLabelCompareColl;
	}
	public static void setResourceLabelCompareColl(String resourceLabelCompareColl) {
		ResourceLabelCompareColl = resourceLabelCompareColl;
	}
	public static String getResourceWordCompareColl() {
		return ResourceWordCompareColl;
	}
	public static void setResourceWordCompareColl(String resourceWordCompareColl) {
		ResourceWordCompareColl = resourceWordCompareColl;
	}
	
	
}
