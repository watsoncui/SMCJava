package com.watsoncui.ucr.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DoubleRead {

	private String readA;
	private String readB;
	private int transOrder;
	private List<Integer> countList;
	
	public DoubleRead(String readA, String readB, int transOrder, List<String> permutationList) {
		this.readA = readA;
		this.readB = readB;
		this.transOrder = transOrder;
		fillCountList(permutationList);
	}
	
	private String getTransReverse(String origin) {
		char[] charList = origin.toUpperCase().toCharArray();
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
		return (new StringBuilder(String.valueOf(charList))).reverse().toString();
	}

	private void countMerForElement(String data, Map<String, Integer> countMap, List<String> permutationList) {
		for (int i = 0; i < data.length() - transOrder + 1; i++) {
			String key = data.substring(i, i + transOrder);
			countMap.put(key, countMap.get(key) + 1);
		}
	}
	
	private List<Integer> countMerForList(String[] dataList, List<String> permutationList) {
		List<Integer> integerList = new ArrayList<Integer>(permutationList.size());
		Map<String, Integer> countMap = new TreeMap<String, Integer>();
			for (String permutation:permutationList) {
				countMap.put(permutation, 0);
			}
			for (int i = 0; i < dataList.length; i++) {
				countMerForElement(dataList[i], countMap, permutationList);
			}
			for (int i = 0; i < permutationList.size(); i++) {
				integerList.add(countMap.get(permutationList.get(i)));
			}
		return integerList;
	}
	
	private void fillCountList(List<String> permutationList) {
		String[] dataList = {readA, readB, getTransReverse(readA), getTransReverse(readB)};
		countList = countMerForList(dataList, permutationList);
	}
	
	public String getReadA() {
		return readA;
	}

	public void setReadA(String readA) {
		this.readA = readA;
	}

	public String getReadB() {
		return readB;
	}

	public void setReadB(String readB) {
		this.readB = readB;
	}

	public int getTransOrder() {
		return transOrder;
	}

	public void setTransOrder(int transOrder) {
		this.transOrder = transOrder;
	}

	public List<Integer> getCountList() {
		return countList;
	}

	public void setCountList(List<Integer> countList) {
		this.countList = countList;
	}
}
