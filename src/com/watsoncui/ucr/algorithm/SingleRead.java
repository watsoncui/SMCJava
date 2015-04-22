package com.watsoncui.ucr.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
		String bChain = String.valueOf(charList);
		String reverse = (new StringBuilder(content)).reverse().toString();
		String bChainReverse = (new StringBuilder(bChain)).reverse().toString();
		this.transCountList = countSubstr(content, permutationList);
		List<Integer> temp = countSubstr(bChain, permutationList);
		this.transCountwithReverseList = new ArrayList<Integer>(permutationList.size());
		for (int i = 0; i < permutationList.size(); i++) {
			this.transCountwithReverseList.add(temp.get(i) + this.transCountList.get(i));
		}
		
	}
	
	private List<Integer> countSubstr(String str, List<String> permutationList) {
		List<Integer> integerList = new ArrayList<Integer>(permutationList.size());
		Map<String, Integer> countMap = new TreeMap<String, Integer>();
		for (String permutation:permutationList) {
			countMap.put(permutation, 0);
		}
		for (int i = 0; i < str.length() - this.transOrder + 1; i++) {
			String key = str.substring(i, i + this.transOrder);
			countMap.put(key, countMap.get(key) + 1);
		}
		for (int i = 0; i < permutationList.size(); i++) {
			integerList.add(countMap.get(permutationList.get(i)));
		}
		return integerList;
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
