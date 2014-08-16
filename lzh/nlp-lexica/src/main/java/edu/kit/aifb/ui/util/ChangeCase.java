package edu.kit.aifb.ui.util;

public class ChangeCase {

	public String changeCase(String str){
		String out = "";
		char i = str.charAt(0);
		if(Character.isUpperCase(i)){
			out = str.substring(0,1).toLowerCase() + str.substring(1, str.length());
		}else if(Character.isLowerCase(i)){
			out = str.substring(0,1).toUpperCase() + str.substring(1, str.length());
		}
		return out;
	}
}
