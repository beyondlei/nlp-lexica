package edu.kit.aifb.ui.util;

import java.util.LinkedHashMap;
import java.util.List;

public class CompareResult {

	private LinkedHashMap<String, Double> rlComparePlr;
	private LinkedHashMap<Resource, Double> rlComparePmi;
	private LinkedHashMap<Resource, Double> rlComparePrl;
	private List<Resource> rwCompare;
	public LinkedHashMap<String, Double> getRlComparePlr() {
		return rlComparePlr;
	}
	public void setRlComparePlr(LinkedHashMap<String, Double> rlComparePlr) {
		this.rlComparePlr = rlComparePlr;
	}
	public LinkedHashMap<Resource, Double> getRlComparePmi() {
		return rlComparePmi;
	}
	public void setRlComparePmi(LinkedHashMap<Resource, Double> rlComparePmi) {
		this.rlComparePmi = rlComparePmi;
	}
	public LinkedHashMap<Resource, Double> getRlComparePrl() {
		return rlComparePrl;
	}
	public void setRlComparePrl(LinkedHashMap<Resource, Double> rlComparePrl) {
		this.rlComparePrl = rlComparePrl;
	}
	public List<Resource> getRwCompare() {
		return rwCompare;
	}
	public void setRwCompare(List<Resource> rwCompare) {
		this.rwCompare = rwCompare;
	}
	
	
}
