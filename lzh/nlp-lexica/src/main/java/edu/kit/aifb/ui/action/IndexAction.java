package edu.kit.aifb.ui.action;

import com.opensymphony.xwork2.ActionSupport;

import edu.kit.aifb.gwifi.wiki.mongodb.interfaceSearch.InterlingualResourceMongoDBSearch;
import edu.kit.aifb.ui.util.CompareResult;
import edu.kit.aifb.ui.util.MongoDBInfo;
import edu.kit.aifb.ui.util.Result;
import edu.kit.aifb.ui.util.SearchByLabel;
import edu.kit.aifb.ui.util.SearchByResource;
import edu.kit.aifb.ui.util.SearchByWord;
import edu.kit.aifb.ui.util.SearchForCompare;

public class IndexAction extends ActionSupport{

	private static final long serialVersionUID = 1L;
	private String inlang;
	private String outlang;
	private int resultNum;
	private int type;
	private String searcher;
	private String input;
	private int compare;
	private Result result;
	private CompareResult compResult;
	
	public String execute() throws Exception{
		
		System.out.println("inlang     ~"+inlang+"     outlang   ~"+outlang+"    searcher    ~"+ searcher+"    type    ~"+type+"    compare    ~"+compare+"      resultNum    ~"+resultNum);
		
		if(inlang.equals(outlang)){
			input = searcher;
		}else{
			InterlingualResourceMongoDBSearch langconvert = new InterlingualResourceMongoDBSearch(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getInterlingualResourceColl(), inlang, outlang);
			input = langconvert.searchTargetLanguage(searcher);
			langconvert.close();
		}
		
		if(input.equals(""))
			return "none";
		
		if(outlang.equals("en") && compare == 1){
			SearchForCompare compareSearcher = new SearchForCompare(MongoDBInfo.getHost(), MongoDBInfo.getDb(), MongoDBInfo.getResourceLabelCompareColl(), MongoDBInfo.getResourceWordCompareColl(), resultNum);
			compResult = compareSearcher.getResult(type, input);
		}
		
		if(type == 1){
			SearchByResource resourceSearcher = new SearchByResource(inlang, outlang, resultNum);
			result = resourceSearcher.getResult(input);
			if(result.getCrosslingual() != null || result.getRlCoOccurrencePlr()!=null || result.getRlCoOccurrencePmi()!=null || result.getRlSensePlr()!=null || result.getRlSensePmi()!=null || result.getRwCoOccurrencePwr()!=null || result.getRwCoOccurrencePmi()!=null){
				if(outlang.equals("en") && compare == 1)
					return "resourceWithCompare";
				else
					return "resource";
			}
		}
		else if(type == 2){
			SearchByLabel labelSearcher = new SearchByLabel(inlang, outlang, resultNum);
			result = labelSearcher.getResult(input);
			if(result.getCrosslingual() != null || result.getRlCoOccurrencePrl()!=null || result.getRlCoOccurrencePmi()!=null || result.getRlSensePrl()!=null || result.getRlSensePmi()!=null){
				if(outlang.equals("en") && compare == 1)
					return "labelWithCompare";
				else
					return "label";
			}
		}
		else if(type == 3){
			SearchByWord wordSearcher = new SearchByWord(inlang, outlang, resultNum);
			result = wordSearcher.getResult(input);
			if(result.getCrosslingual() != null || result.getRwCoOccurrencePrw()!=null || result.getRwCoOccurrencePmi()!=null){
				if(outlang.equals("en") && compare == 1)
					return "wordWithCompare";
				else
					return "word";
			}
		}
		return "none";
	 }
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public String getSearcher() {
		return searcher;
	}
	
	public void setSearcher(String searcher) {
		this.searcher = searcher;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public int getResultNum() {
		return resultNum;
	}

	public void setResultNum(int resultNum) {
		this.resultNum = resultNum;
	}

	public String getInlang() {
		return inlang;
	}

	public void setInlang(String inlang) {
		this.inlang = inlang;
	}

	public String getOutlang() {
		return outlang;
	}

	public void setOutlang(String outlang) {
		this.outlang = outlang;
	}

	public int getCompare() {
		return compare;
	}

	public void setCompare(int compare) {
		this.compare = compare;
	}

	public CompareResult getCompResult() {
		return compResult;
	}

	public void setCompResult(CompareResult compResult) {
		this.compResult = compResult;
	}

	
}
