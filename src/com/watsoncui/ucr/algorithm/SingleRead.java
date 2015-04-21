package com.watsoncui.ucr.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SingleRead {
	private String content;
	private int sampleLength;
	private int transOrder;
	private List<Integer> transCountList;
	private List<Integer> transCountwithReverseList;
	
	public SingleRead(String content, int sampleLengthParam, int transOrderParam, List<String> permutationList) {
		this.content = content;
		this.sampleLength = sampleLengthParam;
		this.transOrder = transOrderParam;
		fillLists(permutationList);
	}
	
	private void fillLists(List<String> permutationList) {
		char[] charList = content.toUpperCase().toCharArray();
		for (int i = 0; i < charList.length; i++) {
			switch (charList[i]) {
			case 'A':
				charList[i] = 'T';
				break;
			case 'C':
				charList[i] = 'G';
				break;
			case 'G':
				charList[i] = 'C';
				break;
			case 'T':
				charList[i] = 'A';
				break;
			default:
				break;	
			}
		}
		String reverse = String.valueOf(charList);
		this.transCountList = new ArrayList<Integer>();
		this.transCountwithReverseList = new ArrayList<Integer>();
		for (int i = 0; i < permutationList.size(); i++) {
			transCountList.add(countSubstr(content, permutationList.get(i)));
			transCountwithReverseList.add(transCountList.get(i) + countSubstr(reverse, permutationList.get(i)));
		}
	}
	
	private int countSubstr(String str, String substr) {
		int count = 0;
		Pattern p = Pattern.compile(substr);
		Matcher m = p.matcher(str);
		while (m.find()) {
			count++;
		}
		return count;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getSampleLength() {
		return sampleLength;
	}

	public void setSampleLength(int sampleLength) {
		this.sampleLength = sampleLength;
	}

	public int getTransOrder() {
		return transOrder;
	}

	public void setTransOrder(int transOrder) {
		this.transOrder = transOrder;
	}

	public List<Integer> getTransCountList() {
		return transCountList;
	}

	public void setTransCountList(List<Integer> transCountList) {
		this.transCountList = transCountList;
	}

	public List<Integer> getTransCountwithReverseList() {
		return transCountwithReverseList;
	}

	public void setTransCountwithReverseList(List<Integer> transCountwithReverseList) {
		this.transCountwithReverseList = transCountwithReverseList;
	}
	
	
}
