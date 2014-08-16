package edu.kit.aifb.ui.util;

import java.util.LinkedHashMap;

public class Result {

	private LinkedHashMap<String, Double> rlSensePlr;
	private LinkedHashMap<Resource, Double> rlSensePrl;
	private LinkedHashMap<Resource, Double> rlSensePmi;
	private LinkedHashMap<String, Double> rlCoOccurrencePlr;
	private LinkedHashMap<Resource, Double> rlCoOccurrencePrl;
	private LinkedHashMap<Resource, Double> rlCoOccurrencePmi;
	private LinkedHashMap<Resource, Double> rwCoOccurrencePrw;
	private LinkedHashMap<String, Double> rwCoOccurrencePwr;
	private LinkedHashMap<Resource, Double> rwCoOccurrencePmi;
	public LinkedHashMap<String, Double> getRlSensePlr() {
		return rlSensePlr;
	}
	public void setRlSensePlr(LinkedHashMap<String, Double> rlSensePlr) {
		this.rlSensePlr = rlSensePlr;
	}
	public LinkedHashMap<Resource, Double> getRlSensePrl() {
		return rlSensePrl;
	}
	public void setRlSensePrl(LinkedHashMap<Resource, Double> rlSensePrl) {
		this.rlSensePrl = rlSensePrl;
	}
	public LinkedHashMap<Resource, Double> getRlSensePmi() {
		return rlSensePmi;
	}
	public void setRlSensePmi(LinkedHashMap<Resource, Double> rlSensePmi) {
		this.rlSensePmi = rlSensePmi;
	}
	public LinkedHashMap<String, Double> getRlCoOccurrencePlr() {
		return rlCoOccurrencePlr;
	}
	public void setRlCoOccurrencePlr(LinkedHashMap<String, Double> rlCoOccurrencePlr) {
		this.rlCoOccurrencePlr = rlCoOccurrencePlr;
	}
	public LinkedHashMap<Resource, Double> getRlCoOccurrencePrl() {
		return rlCoOccurrencePrl;
	}
	public void setRlCoOccurrencePrl(
			LinkedHashMap<Resource, Double> rlCoOccurrencePrl) {
		this.rlCoOccurrencePrl = rlCoOccurrencePrl;
	}
	public LinkedHashMap<Resource, Double> getRlCoOccurrencePmi() {
		return rlCoOccurrencePmi;
	}
	public void setRlCoOccurrencePmi(
			LinkedHashMap<Resource, Double> rlCoOccurrencePmi) {
		this.rlCoOccurrencePmi = rlCoOccurrencePmi;
	}
	public LinkedHashMap<Resource, Double> getRwCoOccurrencePrw() {
		return rwCoOccurrencePrw;
	}
	public void setRwCoOccurrencePrw(
			LinkedHashMap<Resource, Double> rwCoOccurrencePrw) {
		this.rwCoOccurrencePrw = rwCoOccurrencePrw;
	}
	public LinkedHashMap<String, Double> getRwCoOccurrencePwr() {
		return rwCoOccurrencePwr;
	}
	public void setRwCoOccurrencePwr(LinkedHashMap<String, Double> rwCoOccurrencePwr) {
		this.rwCoOccurrencePwr = rwCoOccurrencePwr;
	}
	public LinkedHashMap<Resource, Double> getRwCoOccurrencePmi() {
		return rwCoOccurrencePmi;
	}
	public void setRwCoOccurrencePmi(
			LinkedHashMap<Resource, Double> rwCoOccurrencePmi) {
		this.rwCoOccurrencePmi = rwCoOccurrencePmi;
	}
	
		
}